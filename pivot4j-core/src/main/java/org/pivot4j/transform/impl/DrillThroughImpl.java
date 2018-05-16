/*
 * ====================================================================
 * This software is subject to the terms of the Common Publilc License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.transform.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.servlet.ServletRequest;

import org.apache.commons.lang.NullArgumentException;
import org.olap4j.Cell;
import org.olap4j.CellSet;
import org.olap4j.CellSetAxis;
import org.olap4j.OlapConnection;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Cube;
import org.olap4j.metadata.Level;
import org.olap4j.metadata.Member;
import org.olap4j.metadata.MetadataElement;
import org.pivot4j.PivotException;
import org.pivot4j.PivotModel;
import org.pivot4j.impl.QueryAdapter;
import org.pivot4j.mdx.Exp;
import org.pivot4j.transform.AbstractTransform;
import org.pivot4j.transform.DrillThrough;
import org.pivot4j.util.OlapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DrillThroughImpl extends AbstractTransform implements DrillThrough {

    private Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * @param queryAdapter
     * @param connection
     */
    public DrillThroughImpl(QueryAdapter queryAdapter, OlapConnection connection) {
        super(queryAdapter, connection);
    }

    /**
     * @return the logger
     */
    protected Logger getLogger() {
        return logger;
    }

    /**
     * @see org.pivot4j.transform.DrillThrough#drillThrough(org.olap4j.Cell)
     */
    @Override
    public ResultSet drillThrough(Cell cell) {
        return drillThrough(cell, null, 0);
    }

    /**
     * @see org.pivot4j.transform.DrillThrough#drillThrough(org.olap4j.Cell,
     * java.util.List, int)
     */
    @Override
    public ResultSet drillThrough(Cell cell, List<MetadataElement> selection,
            int maximumRows) {
        if (cell == null) {
            throw new NullArgumentException("cell");
        }

        ResultSet result;

        if (selection == null) {
            selection = Collections.emptyList();
        }

        result = performDrillThroughMdx(cell, selection, maximumRows);

        return result;
    }

    /**
     * @param cell
     * @return
     */
    protected ResultSet performDrillThrough(Cell cell) {
        try {
            return cell.drillThrough();
        } catch (OlapException e) {
            throw new PivotException(e);
        }
    }

    /**
     * @param cell
     * @param selection
     * @param maximumRows
     * @return
     */
    private ServletRequest session;

    protected ResultSet performDrillThroughMdx(Cell cell,
            List<MetadataElement> selection, int maximumRows) {
        PivotModel model = getModel();
        Cube cube = model.getCube();

        QueryAdapter query = getQueryAdapter();

        StringBuilder builder = new StringBuilder();

        builder.append("DRILLTHROUGH");

        if (maximumRows > 0) {
            builder.append(" MAXROWS ");
            builder.append(maximumRows);
        }

        builder.append(" SELECT (");

        boolean isFirst = true;

        List<Integer> coords = cell.getCoordinateList();

        CellSet cellSet = cell.getCellSet();
        List<CellSetAxis> axes = cellSet.getAxes();

        OlapUtils utils = new OlapUtils(getModel().getCube());
        utils.setMemberHierarchyCache(getQueryAdapter().getModel()
                .getMemberHierarchyCache());

        int axisOrdinal = 0;
        for (int ordinal : coords) {
            Position position = axes.get(axisOrdinal++).getPositions()
                    .get(ordinal);

            for (Member member : position.getMembers()) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }

                builder.append(utils.wrapRaggedIfNecessary(member)
                        .getUniqueName());
            }
        }

        builder.append(") ON COLUMNS FROM ");
        builder.append(cube.getUniqueName());

        Exp slicer = query.getParsedQuery().getSlicer();

        if (slicer != null) {
            builder.append(" WHERE ");
            builder.append(slicer.toMdx());
        }

        List<MetadataElement> members;
        if (selection == null) {
            members = Collections.emptyList();
        } else {
            members = new LinkedList<MetadataElement>();

            for (MetadataElement elem : selection) {
                if (elem instanceof Member) {
                    members.add(utils.wrapRaggedIfNecessary((Member) elem));
                } else if (elem instanceof Level) {
                    members.add(elem);
                }
            }
        }

        if (!members.isEmpty()) {
            builder.append(" RETURN ");

            isFirst = true;

            for (MetadataElement elem : members) {
                if (isFirst) {
                    isFirst = false;
                } else {
                    builder.append(", ");
                }

                builder.append(elem.getUniqueName());
            }
        }

        String mdx = builder.toString();

        if (logger.isDebugEnabled()) {
            logger.debug("Drill through MDX : {}", mdx);
        }

        ResultSet result;

        try {
            Statement stmt = getConnection().createStatement();
            result = stmt.executeQuery(mdx);
        } catch (SQLException e) {
            throw new PivotException(e);
        }

        return result;
    }
}
