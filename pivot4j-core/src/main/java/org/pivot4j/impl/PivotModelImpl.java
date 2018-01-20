/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.impl;

import java.io.Serializable;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.logging.LogFactory;
import org.olap4j.Axis;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapDataSource;
import org.olap4j.OlapDatabaseMetaData;
import org.olap4j.OlapException;
import org.olap4j.OlapStatement;
import org.olap4j.Position;
import org.olap4j.Scenario;
import org.olap4j.mdx.IdentifierNode;
import org.olap4j.metadata.Catalog;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Dimension;
import org.olap4j.metadata.Dimension.Type;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.Schema;
import org.pivot4j.ModelChangeEvent;
import org.pivot4j.ModelChangeListener;
import org.pivot4j.NotInitializedException;
import org.pivot4j.PivotException;
import org.pivot4j.PivotModel;
import org.pivot4j.QueryEvent;
import org.pivot4j.QueryListener;
import org.pivot4j.el.ExpressionContext;
import org.pivot4j.el.ExpressionEvaluatorFactory;
import org.pivot4j.el.freemarker.FreeMarkerExpressionEvaluatorFactory;
import org.pivot4j.sort.SortCriteria;
import org.pivot4j.transform.Transform;
import org.pivot4j.transform.TransformFactory;
import org.pivot4j.transform.impl.TransformFactoryImpl;
import org.pivot4j.util.MemberHierarchyCache;
import org.pivot4j.util.OlapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The pivot model represents all (meta-)data for an MDX query.
 */
public class PivotModelImpl implements PivotModel {

    private Logger logger = LoggerFactory.getLogger(getClass());

    private OlapDataSource dataSource;

    private OlapConnection connection;

    private String roleName;

    private Locale locale;

    private boolean initialized = false;

    private Boolean scenarioSupported;

    private Collection<ModelChangeListener> modelListeners = new LinkedList<ModelChangeListener>();

    private Collection<QueryListener> queryListeners = new LinkedList<QueryListener>();

    private QueryAdapter queryAdapter;

    private TransformFactory transformFactory = new TransformFactoryImpl();

    private ExpressionEvaluatorFactory expressionEvaluatorFactory = new FreeMarkerExpressionEvaluatorFactory();

    private int topBottomCount = 10;

    private SortCriteria sortCriteria = SortCriteria.ASC;

    private boolean sorting = false;

    private boolean defaultNonEmpty = false;

    private List<Member> sortPosMembers;

    private String mdxQuery;

    private CellSet cellSet;

    private ExpressionContext expressionContext;

    private MemberHierarchyCache memberHierarchyCache;

    private QueryChangeListener queryChangeListener = new QueryChangeListener() {

        public void queryChanged(QueryChangeEvent e) {
            fireStructureChanged();
        }
    };

    /**
     * @param dataSource
     */
    public PivotModelImpl(OlapDataSource dataSource) {
        if (dataSource == null) {
            throw new NullArgumentException("dataSource");
        }

        this.dataSource = dataSource;
        this.expressionContext = createExpressionContext();
    }

    /**
     * @return the logger
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * Returns the current locale.
     *
     * @return Locale
     * @see org.pivot4j.PivotModel#getLocale()
     */
    public Locale getLocale() {
        if (locale == null) {
            return Locale.getDefault();
        }

        return locale;
    }

    /**
     * Sets the current locale.
     *
     * @param locale The locale to set
     * @see org.pivot4j.PivotModel#setLocale(java.util.Locale)
     */
    public void setLocale(Locale locale) {
        this.locale = locale;

        if (connection != null) {
            connection.setLocale(locale);
        }
    }

    /**
     * @return the roleName
     * @see org.pivot4j.PivotModel#getRoleName()
     */
    public String getRoleName() {
        return roleName;
    }

    /**
     * @param roleName the roleName to set
     * @see org.pivot4j.PivotModel#setRoleName(java.lang.String)
     */
    public void setRoleName(String roleName) {
        this.roleName = roleName;

        if (connection != null) {
            try {
                connection.setRoleName(roleName);
            } catch (OlapException e) {
                throw new PivotException(e);
            }
        }
    }

    /**
     * @see org.pivot4j.PivotModel#initialize()
     */
    public synchronized void initialize() {
        
        
        System.out.println("public synchronized void initialize");
        
        if (isInitialized()) {
            destroy();
        }

        if (mdxQuery == null) {
            throw new PivotException("Initial MDX query is null.");
        }

        try {
            this.connection = createConnection(dataSource);
        } catch (SQLException e) {
            throw new PivotException(e);
        }

        this.initialized = true;

        Logger log = LoggerFactory.getLogger(getClass());
        if (log.isDebugEnabled()) {
            log.debug("Initializing model with MDX : {}", mdxQuery);
        }

        this.queryAdapter = createQueryAdapter();

        queryAdapter.initialize();
        queryAdapter.updateQuery();
        queryAdapter.addChangeListener(queryChangeListener);

        initializeCache();

        fireModelInitialized();
    }

    /**
     * @see org.pivot4j.PivotModel#isInitialized()
     */
    public boolean isInitialized() {
        return initialized;
    }

    private void checkInitialization() {
        if (!isInitialized()) {
            throw new NotInitializedException(
                    "Model has not been initialized yet.");
        }
    }

    private void initializeCache() {
        if (!isInitialized()) {
            return;
        }

        Cube cube = getCube();

        if (cube == null) {
            this.memberHierarchyCache = null;
        } else if (memberHierarchyCache == null
                || !OlapUtils.equals(memberHierarchyCache.getCube(), cube)) {
            this.memberHierarchyCache = new MemberHierarchyCache(cube);
        }
    }

    /**
     * @param dataSource
     * @return
     * @throws SQLException
     */
    protected OlapConnection createConnection(OlapDataSource dataSource)
            throws SQLException {
        OlapConnection con = dataSource.getConnection();

        if (roleName != null) {
            con.setRoleName(roleName);
        }

        if (locale != null) {
            con.setLocale(locale);
        }

        return con;
    }

    /**
     * @see org.pivot4j.PivotModel#destroy()
     */
    public synchronized void destroy() {
        checkInitialization();

        if (queryAdapter != null) {
            queryAdapter.removeChangeListener(queryChangeListener);
            this.queryAdapter = null;
        }

        if (connection != null) {
            if (logger.isDebugEnabled()) {
                logger.debug("Closing OLAP connection {}", connection);
            }

            try {
                closeConnection(connection);
            } catch (SQLException e) {
                throw new PivotException(e);
            }

            this.connection = null;
        }

        this.sortPosMembers = null;
        this.sortCriteria = SortCriteria.ASC;
        this.sorting = false;
        this.cellSet = null;
        this.initialized = false;

        this.memberHierarchyCache = null;

        fireModelDestroyed();
    }

    /**
     * @param connection
     * @throws SQLException
     */
    protected void closeConnection(OlapConnection connection)
            throws SQLException {
        connection.close();
    }

    protected ExpressionContext createExpressionContext() {
        ExpressionContext context = new ExpressionContext();

        context.put("locale", new ExpressionContext.ValueBinding<Locale>() {

            @Override
            public Locale getValue() {
                return getLocale();
            }
        });

        context.put("roleName", new ExpressionContext.ValueBinding<String>() {

            @Override
            public String getValue() {
                return getRoleName();
            }
        });

        context.put("cube", new ExpressionContext.ValueBinding<Cube>() {

            @Override
            public Cube getValue() {
                if (!isInitialized()) {
                    return null;
                }

                return getCube();
            }
        });

        context.put("catalog", new ExpressionContext.ValueBinding<Catalog>() {

            @Override
            public Catalog getValue() {
                if (!isInitialized()) {
                    return null;
                }

                return getCatalog();
            }
        });

        context.put("cellSet", new ExpressionContext.ValueBinding<CellSet>() {

            @Override
            public CellSet getValue() {
                return cellSet;
            }
        });

        context.put("memberUtils",
                new ExpressionContext.ValueBinding<OlapUtils>() {

            @Override
            public OlapUtils getValue() {
                Cube cube = getCube();
                if (cube == null) {
                    return null;
                }

                OlapUtils utils = new OlapUtils(cube);
                utils.setMemberHierarchyCache(memberHierarchyCache);

                return utils;
            }
        });

        return context;
    }

    /**
     * Returns the connection.
     */
    protected OlapConnection getConnection() {
        return connection;
    }

    /**
     * @see org.pivot4j.PivotModel#isScenarioSupported()
     */
    @Override
    public boolean isScenarioSupported() {
        if (scenarioSupported == null) {
            Cube cube = getCube();

            Schema schema = cube.getSchema();
            Catalog catalog = schema.getCatalog();

            OlapDatabaseMetaData metadata = cube.getSchema().getCatalog()
                    .getMetaData();

            ResultSet rs = null;

            try {
                // TODO See Olap4J's issue #116
                if (metadata.getDriverName().startsWith("Mondrian")) {
                    Dimension dimension = cube.getDimensions().get("Scenario");
                    this.scenarioSupported = dimension != null
                            && dimension.isVisible();
                } else {
                    rs = metadata.getCubes(catalog.getName(), schema.getName(),
                            cube.getName());
                    if (rs.next()) {
                        scenarioSupported = rs.getBoolean("IS_WRITE_ENABLED");
                    }
                }
            } catch (SQLException e) {
                throw new PivotException(e);
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException e) {
                    }
                }
            }
        }

        return Boolean.TRUE.equals(scenarioSupported);
    }

    /**
     * @see org.pivot4j.PivotModel#createScenario()
     */
    @Override
    public Scenario createScenario() {
        checkInitialization();

        try {
            return connection.createScenario();
        } catch (OlapException e) {
            throw new PivotException(e);
        }
    }

    /**
     * @see org.pivot4j.PivotModel#getScenario()
     */
    @Override
    public Scenario getScenario() {
        checkInitialization();

        try {
            return connection.getScenario();
        } catch (OlapException e) {
            throw new PivotException(e);
        }
    }

    /**
     * @see org.pivot4j.PivotModel#setScenario(org.olap4j.Scenario)
     */
    @Override
    public void setScenario(Scenario scenario) {
        checkInitialization();

        try {
            connection.setScenario(scenario);
        } catch (OlapException e) {
            throw new PivotException(e);
        }

        refresh();

        fireModelChanged();
    }

    public OlapDataSource getDataSource() {
        return dataSource;
    }

    /**
     * @return @throws NotInitializedException
     */
    public Catalog getCatalog() {
        checkInitialization();

        try {
            return connection.getOlapCatalog();
        } catch (OlapException e) {
            throw new PivotException(e);
        }
    }

    /**
     * @see org.pivot4j.PivotModel#getMetadata()
     */
    @Override
    public OlapDatabaseMetaData getMetadata() {
        boolean initialized = (connection != null);

        try {
            if (!initialized) {
                connection = createConnection(dataSource);
            }

            return connection.getMetaData();
        } catch (SQLException e) {
            throw new PivotException(e);
        } finally {
            if (!initialized) {
                try {
                    closeConnection(connection);
                } catch (SQLException e) {
                    throw new PivotException(e);
                }
            }
        }
    }

    /**
     * @see org.pivot4j.PivotModel#getCube()
     */
    public Cube getCube() {
        checkInitialization();

        Cube cube = null;
        try {
            if (cellSet != null) {
                cube = cellSet.getMetaData().getCube();
            } else {
                String cubeName = queryAdapter.getCubeName();

                Schema schema = connection.getOlapSchema();
                cube = schema.getCubes().get(cubeName);

                if (cube == null && cubeName != null) {
                    if (logger.isWarnEnabled()) {
                        logger.warn("Cube with the specified name cannot be found : "
                                + cubeName);
                    }

                    if (logger.isDebugEnabled()) {
                        logger.debug("List of cubes in schema : "
                                + schema.getName());

                        for (Cube c : schema.getCubes()) {
                            logger.debug(c.getCaption() + " - "
                                    + c.getUniqueName());
                        }
                    }
                }
            }
        } catch (OlapException e) {
            throw new PivotException(e);
        }

        return cube;
    }

    /**
     * @see org.pivot4j.PivotModel#getCellSet()
     */
    public synchronized CellSet getCellSet() {
        checkInitialization();

        if (cellSet != null) {
            return cellSet;
        }

        if (queryAdapter == null) {
            throw new IllegalStateException("Initial MDX is not specified.");
        }

        if (expressionEvaluatorFactory != null) {
            queryAdapter.evaluate(expressionEvaluatorFactory.createEvaluator());
        }

        String mdx = normalizeMdx(getCurrentMdx(true));

        if (!queryAdapter.isValid()) {
            return null;
        }

        try {
            this.cellSet = executeMdx(connection, mdx);
        } catch (OlapException e) {
            throw new PivotException(e);
        }

        expressionContext.put("cellSet", cellSet);

        queryAdapter.afterExecute(cellSet);

        return cellSet;
    }

    /**
     * @see org.pivot4j.PivotModel#refresh()
     */
    @Override
    public void refresh() {
        this.cellSet = null;
    }

    /**
     * @see org.pivot4j.PivotModel#getExpressionContext()
     */
    @Override
    public ExpressionContext getExpressionContext() {
        return expressionContext;
    }

    /**
     * @return the memberHierarchyCache
     */
    public MemberHierarchyCache getMemberHierarchyCache() {
        return memberHierarchyCache;
    }

    /**
     * @param connection
     * @param mdx
     * @return
     * @throws OlapException
     */
    protected CellSet executeMdx(OlapConnection connection, String mdx)
            throws OlapException {
        if (logger.isDebugEnabled()) {
            logger.debug("Line 646 "+mdx);
        }

        Date start = new Date(System.currentTimeMillis());

        OlapStatement stmt = connection.createStatement();
        CellSet result = stmt.executeOlapQuery(mdx);

        long duration = System.currentTimeMillis() - start.getTime();
        if (logger.isInfoEnabled()) {
            logger.info(String.format("Query execution time : %d ms", duration));
        }

        fireQueryExecuted(start, duration, mdx);

        return result;
    }

    protected String normalizeMdx(String mdx) {
        if (mdx == null) {
            return null;
        } else {
            return mdx.replaceAll("\r", "");
        }
    }

    /**
     * @see org.pivot4j.PivotModel#getCurrentMdx()
     */
    public String getCurrentMdx() {
        return getCurrentMdx(false);
    }

    /**
     * @see org.pivot4j.PivotModel#getEvaluatedMdx()
     */
    public String getEvaluatedMdx() {
        return getCurrentMdx(true);
    }

    /**
     * @param evaluated
     * @return
     */
    protected String getCurrentMdx(boolean evaluated) {
        if (queryAdapter == null) {
            return null;
        } else {
            return queryAdapter.getCurrentMdx(evaluated);
        }
    }

    /**
     * Returns the mdxQuery.
     *
     * @return String
     */
    public String getMdx() {
        return mdxQuery;
    }

    /**
     * Sets the mdxQuery.
     *
     * @param mdxQuery The mdxQuery to set
     */
    public void setMdx(String mdxQuery) {
        if (mdxQuery == null) {
            throw new NullArgumentException("mdxQuery");
        }

        this.mdxQuery = mdxQuery;

        String mdx = normalizeMdx(mdxQuery);
        if (!mdx.equals(normalizeMdx(getCurrentMdx()))) {
            onMdxChanged(mdx);
        }
    }

    /**
     * @param mdx
     */
    protected void onMdxChanged(String mdx) {
        if (logger.isInfoEnabled()) {
            logger.info("MDX has been changed : {}", mdx);
        }

        this.cellSet = null;
        this.scenarioSupported = null;
        this.topBottomCount = 10;
        this.sortCriteria = SortCriteria.ASC;
        this.sorting = false;
        this.sortPosMembers = null;

        if (queryAdapter != null) {
            queryAdapter.initialize();
            queryAdapter.updateQuery();
        }

        initializeCache();

        fireStructureChanged();
    }

    protected QueryAdapter createQueryAdapter() {
        return new QueryAdapter(this);
    }

    /**
     * Returns the queryAdapter.
     *
     * @return QueryAdapter
     */
    protected QueryAdapter getQueryAdapter() {
        return queryAdapter;
    }

    /**
     * @return the transformFactory
     */
    public TransformFactory getTransformFactory() {
        return transformFactory;
    }

    /**
     * @param factory the transformFactory to set
     */
    public void setTransformFactory(TransformFactory factory) {
        this.transformFactory = factory;
    }

    /**
     * @return the expressionEvaluatorFactory
     * @see org.pivot4j.PivotModel#getExpressionEvaluatorFactory()
     */
    public ExpressionEvaluatorFactory getExpressionEvaluatorFactory() {
        return expressionEvaluatorFactory;
    }

    /**
     * @param factory the expressionEvaluatorFactory to set
     */
    public void setExpressionEvaluatorFactory(ExpressionEvaluatorFactory factory) {
        this.expressionEvaluatorFactory = factory;
    }

    /**
     * @see
     * org.pivot4j.PivotModel#addModelChangeListener(org.pivot4j.ModelChangeListener)
     */
    public void addModelChangeListener(ModelChangeListener listener) {
        modelListeners.add(listener);
    }

    /**
     * @see
     * org.pivot4j.PivotModel#removeModelChangeListener(org.pivot4j.ModelChangeListener)
     */
    public void removeModelChangeListener(ModelChangeListener listener) {
        modelListeners.remove(listener);
    }

    protected void fireModelInitialized() {
        ModelChangeEvent e = new ModelChangeEvent(this);

        List<ModelChangeListener> copiedListeners = new ArrayList<ModelChangeListener>(
                modelListeners);
        for (ModelChangeListener listener : copiedListeners) {
            listener.modelInitialized(e);
        }
    }

    protected void fireModelChanged() {
        this.cellSet = null;

        ModelChangeEvent e = new ModelChangeEvent(this);

        List<ModelChangeListener> copiedListeners = new ArrayList<ModelChangeListener>(
                modelListeners);
        for (ModelChangeListener listener : copiedListeners) {
            listener.modelChanged(e);
        }
    }

    protected void fireStructureChanged() {
        this.cellSet = null;

        ModelChangeEvent e = new ModelChangeEvent(this);

        List<ModelChangeListener> copiedListeners = new ArrayList<ModelChangeListener>(
                modelListeners);
        for (ModelChangeListener listener : copiedListeners) {
            listener.structureChanged(e);
        }
    }

    protected void fireModelDestroyed() {
        ModelChangeEvent e = new ModelChangeEvent(this);

        List<ModelChangeListener> copiedListeners = new ArrayList<ModelChangeListener>(
                modelListeners);
        for (ModelChangeListener listener : copiedListeners) {
            listener.modelDestroyed(e);
        }
    }

    /**
     * @see org.pivot4j.PivotModel#addQueryListener(org.pivot4j.QueryListener)
     */
    public void addQueryListener(QueryListener listener) {
        queryListeners.add(listener);
    }

    /**
     * @see
     * org.pivot4j.PivotModel#removeQueryListener(org.pivot4j.QueryListener)
     */
    public void removeQueryListener(QueryListener listener) {
        queryListeners.remove(listener);
    }

    protected void fireQueryExecuted(Date start, long duration, String mdx) {
        QueryEvent e = new QueryEvent(this, start, duration, mdx, cellSet);

        List<QueryListener> copiedListeners = new ArrayList<QueryListener>(
                queryListeners);
        for (QueryListener listener : copiedListeners) {
            listener.queryExecuted(e);
        }
    }

    /**
     * @see org.pivot4j.PivotModel#getTransform(java.lang.Class)
     */
    public <T extends Transform> T getTransform(Class<T> type) {
        if (transformFactory == null) {
            throw new IllegalStateException(
                    "No transform factory instance is available.");
        }

        return transformFactory.createTransform(type, queryAdapter, connection);
    }

    /**
     * @see org.pivot4j.PivotModel#isSorting()
     */
    public boolean isSorting() {
        return sorting;
    }

    /**
     * @param position to be checked
     * @return true, if position is the current sorting position
     * @see org.pivot4j.PivotModel#isSorting(org.olap4j.Position)
     */
    public boolean isSorting(Position position) {
        if (!isSortOnQuery()) {
            return false;
        } else {
            if (sortPosMembers.size() != position.getMembers().size()) {
                return false;
            }

            for (int i = 0; i < sortPosMembers.size(); i++) {
                Member member1 = sortPosMembers.get(i);
                Member member2 = position.getMembers().get(i);
                // any null does not compare
                if (member1 == null) {
                    return false;
                } else if (!OlapUtils.equals(member1, member2)) {
                    return false;
                }
            }
            return true;
        }
    }

    /**
     * TODO Support for natural sorting
     *
     * @see org.pivot4j.PivotModel#setSorting(boolean)
     */
    public void setSorting(boolean sorting) {
        if (sorting == this.sorting) {
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Change sorting to {}", sorting);
        }

        this.sorting = sorting;

        fireModelChanged();
    }

    /**
     * @return sort criteria (ASC,DESC,BASC,BDESC,TOPCOUNT,BOTTOMCOUNT)
     */
    public SortCriteria getSortCriteria() {
        return sortCriteria;
    }

    /**
     * @see
     * org.pivot4j.PivotModel#setSortCriteria(org.pivot4j.sort.SortCriteria)
     */
    public void setSortCriteria(SortCriteria sortCriteria) {
        if (this.sortCriteria == sortCriteria) {
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Change sort mode from {} to {}", this.sortCriteria,
                    sortCriteria);
        }

        this.sortCriteria = sortCriteria;

        if (isSortOnQuery()) {
            fireModelChanged();
        }
    }

    /**
     * returns true, if ONE of the members is a measure
     *
     * @param position the position to check for sortability
     * @return true, if the position is sortable
     */
    public boolean isSortable(Position position) {
        try {
            List<Member> members = position.getMembers();
            for (Member member : members) {
                if (member.getLevel().getHierarchy().getDimension()
                        .getDimensionType() == Type.MEASURE) {
                    return true;
                }
            }
        } catch (OlapException e) {
            throw new PivotException(e);
        }

        return false;
    }

    /**
     * @return true, if there is a sort for the query
     */
    protected boolean isSortOnQuery() {
        return sorting && sortPosMembers != null && !sortPosMembers.isEmpty();
    }

    /**
     * @see org.pivot4j.PivotModel#getSortPosMembers()
     */
    public List<Member> getSortPosMembers() {
        return Collections.unmodifiableList(sortPosMembers);
    }

    /**
     * @return top/bottom count
     * @see org.pivot4j.PivotModel#getTopBottomCount()
     */
    public int getTopBottomCount() {
        return topBottomCount;
    }

    /**
     * @see org.pivot4j.PivotModel#setTopBottomCount(int)
     */
    public void setTopBottomCount(int topBottomCount) {
        if (this.topBottomCount == topBottomCount) {
            return;
        }

        if (logger.isInfoEnabled()) {
            logger.info("Change topBottomCount from {} to {}",
                    this.topBottomCount, topBottomCount);
        }

        this.topBottomCount = topBottomCount;

        if (sorting
                && sortPosMembers != null
                && (sortCriteria == SortCriteria.TOPCOUNT || sortCriteria == SortCriteria.BOTTOMCOUNT)) {
            fireModelChanged();
        }
    }

    /**
     * @param axisToSort Axis containing the members to be sorted
     * @param position Position on "other axis" defining the members by which
     * the membersToSort are sorted
     */
    public void sort(CellSetAxis axisToSort, Position position) {
        List<Position> positions = axisToSort.getPositions();

        // if the axis to sort does not contain any positions - sorting is not
        // posssible
        if (positions.isEmpty()) {
            if (logger.isWarnEnabled()) {
                logger.warn("Reject sort, the axis to be sorted is empty.");
            }

            this.sorting = false;
            return;
        }

        this.sortPosMembers = position.getMembers();

        // find the axis to sort
        Dimension dim = positions.get(0).getMembers().get(0).getDimension();

        Quax quaxToSort = getQueryAdapter().findQuax(dim);

        if (quaxToSort == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Reject sort, the Quax is null");
            }
            this.sorting = false;
            return;
        }

        getQueryAdapter().setQuaxToSort(quaxToSort);

        if (logger.isInfoEnabled()) {
            StringBuilder builder = new StringBuilder();
            builder.append("Change Sort Position ");

            boolean first = true;

            List<Member> members = position.getMembers();
            for (Member member : members) {
                if (first) {
                    first = false;
                } else {
                    builder.append(" ");
                }
                builder.append(member.getUniqueName());
            }
            builder.append(" iAxisToSort=");
            builder.append(Integer.toString(quaxToSort.getOrdinal()));

            logger.info(builder.toString());
        }

        fireModelChanged();
    }

    /**
     * @see org.pivot4j.PivotModel#getDefaultNonEmpty()
     */
    @Override
    public boolean getDefaultNonEmpty() {
        return defaultNonEmpty;
    }

    /**
     * @see org.pivot4j.PivotModel#setDefaultNonEmpty(boolean)
     */
    @Override
    public void setDefaultNonEmpty(boolean defaultNonEmpty) {
        this.defaultNonEmpty = defaultNonEmpty;
    }

    /**
     * @see org.pivot4j.state.Bookmarkable#saveState()
     */
    @Override
    public synchronized Serializable saveState() {
        Serializable[] state = new Serializable[4];

        if (isInitialized()) {
            state[0] = getCurrentMdx(false);
        } else {
            state[0] = mdxQuery;
        }

        if (sortPosMembers == null) {
            state[1] = null;
        } else {
            Serializable[] sortState = new Serializable[4];

            String[] uniqueNames = new String[sortPosMembers.size()];
            for (int i = 0; i < uniqueNames.length; i++) {
                uniqueNames[i] = sortPosMembers.get(i).getUniqueName();
            }

            sortState[0] = uniqueNames;
            sortState[1] = getTopBottomCount();
            sortState[2] = getSortCriteria();
            sortState[3] = isSorting();

            state[1] = sortState;
        }

        state[2] = getQueryAdapter().saveState();
        state[3] = defaultNonEmpty;

        return state;
    }

    /**
     * @see org.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
     */
    public synchronized void restoreState(Serializable state) {
        if (state == null) {
            throw new NullArgumentException("state");
        }

        Serializable[] states = (Serializable[]) state;

        setMdx((String) states[0]);

        if (!isInitialized()) {
            initialize();
        }

        // sorting
        if (states[1] == null) {
            this.sortPosMembers = null;
        } else {
            try {
                Cube cube = getCube();

                Serializable[] sortStates = (Serializable[]) states[1];

                String[] sortPosUniqueNames = (String[]) sortStates[0];
                if (sortPosUniqueNames == null) {
                    this.sortPosMembers = null;
                } else {
                    this.sortPosMembers = new ArrayList<Member>(
                            sortPosUniqueNames.length);

                    for (int i = 0; i < sortPosUniqueNames.length; i++) {
                        Member member = cube.lookupMember(IdentifierNode
                                .parseIdentifier(sortPosUniqueNames[i])
                                .getSegmentList());
                        if (member == null) {
                            if (logger.isWarnEnabled()) {
                                logger.warn(
                                        "Sort position member not found {}",
                                        sortPosUniqueNames[i]);
                            }

                            break;
                        }

                        sortPosMembers.add(member);
                    }

                    this.topBottomCount = (Integer) sortStates[1];
                    this.sortCriteria = (SortCriteria) sortStates[2];
                    this.sorting = (Boolean) sortStates[3];
                }
            } catch (OlapException e) {
                throw new PivotException(e);
            }
        }

        this.cellSet = null;

        queryAdapter.restoreState(states[2]);

        this.defaultNonEmpty = (Boolean) states[3];
    }

    /**
     * @see
     * org.pivot4j.state.Configurable#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public synchronized void saveSettings(
            HierarchicalConfiguration configuration) {
        if (configuration == null) {
            throw new NullArgumentException("configuration");
        }

        if (configuration.getLogger() == null) {
            configuration.setLogger(LogFactory.getLog(getClass()));
        }

        if (isInitialized()) {
            configuration.addProperty("mdx", getCurrentMdx());
        } else {
            configuration.addProperty("mdx", mdxQuery);
        }

        if (sorting) {
            configuration.addProperty("sort[@enabled]", sorting);

            if (queryAdapter.getQuaxToSort() != null) {
                configuration.addProperty("sort[@ordinal]", queryAdapter
                        .getQuaxToSort().getOrdinal());
            }

            if (sortCriteria != null) {
                configuration.addProperty("sort[@criteria]",
                        sortCriteria.name());
                configuration.addProperty("sort[@topBottomCount]",
                        topBottomCount);
                if (isSorting() && sortPosMembers != null) {
                    int index = 0;
                    for (Member member : sortPosMembers) {
                        configuration.addProperty(
                                String.format("sort.member(%s)", index++),
                                member.getUniqueName());
                    }
                }
            }
        }

        if (queryAdapter != null && queryAdapter.isAxesSwapped()) {
            configuration.addProperty("axesSwapped", true);
        }

        // TODO This setting can potentially be present in the
        // pivot4j-config.xml, in which case, it would be saved with the report
        // thus making subsequent changing of the default value ineffective.
        // We should wait till we find a way to handle such a scenario better
        // later.
        //
        // if (defaultNonEmpty) {
        // configuration.addProperty("nonEmpty[@default]", true);
        // }
    }

    /**
     * @see
     * org.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public synchronized void restoreSettings(
            HierarchicalConfiguration configuration) {
        if (configuration == null) {
            throw new NullArgumentException("configuration");
        }

        this.defaultNonEmpty = configuration.getBoolean("nonEmpty[@default]",
                false);

        String mdx = configuration.getString("mdx");

        setMdx(mdx);

        if (mdx == null) {
            return;
        }

        if (!isInitialized()) {
            initialize();
        }

        this.sorting = configuration.getBoolean("sort[@enabled]", false);

        this.sortPosMembers = null;
        this.sortCriteria = SortCriteria.ASC;
        this.topBottomCount = 10;

        Quax quaxToSort = null;

        if (sorting) {
            List<Object> sortPosUniqueNames = configuration
                    .getList("sort.member");
            if (sortPosUniqueNames != null && !sortPosUniqueNames.isEmpty()) {
                try {
                    Cube cube = getCube();

                    this.sortPosMembers = new ArrayList<Member>(
                            sortPosUniqueNames.size());

                    for (Object uniqueName : sortPosUniqueNames) {
                        Member member = cube.lookupMember(IdentifierNode
                                .parseIdentifier(uniqueName.toString())
                                .getSegmentList());
                        if (member == null) {
                            if (logger.isWarnEnabled()) {
                                logger.warn("Sort position member not found "
                                        + uniqueName);
                            }

                            break;
                        }

                        sortPosMembers.add(member);
                    }
                } catch (OlapException e) {
                    throw new PivotException(e);
                }
            }

            this.topBottomCount = configuration.getInt("sort[@topBottomCount]",
                    10);

            String sortName = configuration.getString("sort[@criteria]");
            if (sortName != null) {
                this.sortCriteria = SortCriteria.valueOf(sortName);
            }

            int ordinal = configuration.getInt("sort[@ordinal]", -1);

            if (ordinal > 0) {
                for (Axis axis : queryAdapter.getAxes()) {
                    Quax quax = queryAdapter.getQuax(axis);
                    if (quax.getOrdinal() == ordinal) {
                        quaxToSort = quax;
                        break;
                    }
                }
            }
        }

        queryAdapter.setQuaxToSort(quaxToSort);

        boolean axesSwapped = configuration.getBoolean("axesSwapped", false);

        queryAdapter.setAxesSwapped(axesSwapped);

        this.cellSet = null;
    }
}
