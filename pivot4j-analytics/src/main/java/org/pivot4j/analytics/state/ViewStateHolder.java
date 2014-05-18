package org.pivot4j.analytics.state;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.olap4j.OlapDataSource;
import org.pivot4j.PivotModel;
import org.pivot4j.analytics.config.Settings;
import org.pivot4j.analytics.datasource.CatalogInfo;
import org.pivot4j.analytics.datasource.ConnectionInfo;
import org.pivot4j.analytics.datasource.DataSourceManager;
import org.pivot4j.impl.PivotModelImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ManagedBean(name = "viewStateHolder")
@SessionScoped
public class ViewStateHolder implements Serializable {

	private static final long serialVersionUID = -7947800606762703855L;

	private static final long MINUTE = 60;

	private Logger log = LoggerFactory.getLogger(getClass());

	@ManagedProperty(value = "#{settings}")
	private Settings settings;

	@ManagedProperty(value = "#{dataSourceManager}")
	private DataSourceManager dataSourceManager;

	private Map<String, ViewState> states = new LinkedHashMap<String, ViewState>();

	private List<ViewStateListener> viewStateListeners = new ArrayList<ViewStateListener>();

	private Timer timer;

	private long checkInterval = 1 * MINUTE;

	private long keepAliveInterval = 1 * MINUTE;

	private long expires = 5 * MINUTE;

	private String sessionId;

	@PostConstruct
	protected void initialize() {
		ExternalContext context = FacesContext.getCurrentInstance()
				.getExternalContext();
		HttpSession session = (HttpSession) context.getSession(true);
		this.sessionId = session.getId();

		if (log.isInfoEnabled()) {
			log.info("Initializing view state holder for session : "
					+ sessionId);
			log.info(String.format("Check interval : %d secs.", checkInterval));
			log.info(String.format("Keep alive interval : %d secs.",
					keepAliveInterval));
			log.info(String.format("Expires : %d secs.", expires));
		}

		// As there's a no reliable way to clean up resources on a view scoped
		// managed bean while the session is alive, we need to periodically
		// check for stale connections and close them.
		this.timer = new Timer();

		timer.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				checkAbandonedModels();
			}
		}, MINUTE * 1000, checkInterval * 1000);
	}

	@PreDestroy
	protected void destroy() {
		if (log.isInfoEnabled()) {
			log.info("Destroying view state holder for session : {}", sessionId);
		}

		this.timer.cancel();
		this.timer.purge();

		this.viewStateListeners.clear();

		clearStates();
	}

	protected synchronized void checkAbandonedModels() {
		if (log.isDebugEnabled()) {
			log.debug("Checking for abandoned view states for session : "
					+ sessionId);
			log.debug("Current view state count for session : {}",
					states.size());
		}

		Set<String> keys = new HashSet<String>(states.keySet());

		Date now = new Date();

		for (String key : keys) {
			ViewState state = states.get(key);

			long elapsed = now.getTime() - state.getLastActive().getTime();

			if (expires * 1000 <= elapsed) {
				if (log.isInfoEnabled()) {
					log.info("Found an abandoned view sate : {}", state);
				}

				unregisterState(state);
			}
		}
	}

	/**
	 * @param id
	 */
	public synchronized void keepAlive(String id) {
		ViewState state = getState(id);

		if (log.isDebugEnabled()) {
			log.debug("Received a keep alive request : {}", state);
		}

		if (state != null) {
			state.update();
		}
	}

	/**
	 * @param id
	 * @return
	 */
	public ViewState getState(String id) {
		return states.get(id);
	}

	public synchronized List<ViewState> getStates() {
		List<ViewState> list = new ArrayList<ViewState>(states.size());

		for (String id : states.keySet()) {
			list.add(states.get(id));
		}

		return list;
	}

	/**
	 * @param state
	 */
	public synchronized void registerState(ViewState state) {
		if (state == null) {
			throw new IllegalArgumentException(
					"Required argument 'state' is null.");
		}

		ViewState oldState = states.get(state.getId());
		if (oldState != null) {
			if (oldState == state) {
				return;
			}

			unregisterState(oldState);
		}

		states.put(state.getId(), state);

		fireViewRegistered(state);

		if (log.isInfoEnabled()) {
			log.info("View state is registered : {}", state);
			log.info("Current view state count for session : {}", states.size());
		}
	}

	/**
	 * @param id
	 */
	public synchronized void unregisterState(String id) {
		if (id == null) {
			throw new IllegalArgumentException(
					"Required argument 'id' is null.");
		}

		ViewState state = states.get(id);

		if (state != null) {
			unregisterState(state);
		}
	}

	/**
	 * @param state
	 */
	protected synchronized void unregisterState(ViewState state) {
		PivotModel model = state.getModel();
		if (model != null && model.isInitialized()) {
			model.destroy();
		}

		states.remove(state.getId());

		fireViewUnregistered(state);

		if (log.isInfoEnabled()) {
			log.info("View state is unregistered : {}", state);
			log.info("Current view state count for session : {}", states.size());
		}
	}

	protected synchronized void clearStates() {
		for (ViewState state : states.values()) {
			unregisterState(state);
		}
	}

	/**
	 * Create an empty view state.
	 * 
	 * @return
	 */
	public ViewState createNewState() {
		List<CatalogInfo> catalogs = dataSourceManager.getCatalogs();

		ConnectionInfo connectionInfo = null;

		if (catalogs.size() == 1) {
			connectionInfo = new ConnectionInfo(catalogs.get(0).getName(), null);
		}

		return createNewState(connectionInfo, null);
	}

	/**
	 * @param connectionInfo
	 * @param viewId
	 * @return
	 */
	public ViewState createNewState(ConnectionInfo connectionInfo, String viewId) {
		String id;

		if (viewId == null) {
			id = UUID.randomUUID().toString();
		} else {
			id = viewId;
		}

		FacesContext context = FacesContext.getCurrentInstance();

		ResourceBundle messages = context.getApplication().getResourceBundle(
				context, "msg");
		MessageFormat mf = new MessageFormat(
				messages.getString("label.untitled"));

		List<ViewState> stateList = getStates();

		Set<String> names = new HashSet<String>(stateList.size());

		for (ViewState state : stateList) {
			names.add(state.getName());
		}

		int count = 1;

		String name = null;
		while (name == null) {
			name = mf.format(new Object[] { count });

			if (names.contains(name)) {
				name = null;
				count++;
			}
		}

		PivotModel model = null;

		if (connectionInfo != null) {
			OlapDataSource dataSource = dataSourceManager
					.getDataSource(connectionInfo);

			model = new PivotModelImpl(dataSource);

			HierarchicalConfiguration configuration = settings
					.getConfiguration();

			try {
				model.restoreSettings(configuration.configurationAt("model"));
			} catch (IllegalArgumentException e) {
			}
		}

		return new ViewState(id, name, connectionInfo, model, null);
	}

	/**
	 * @param state
	 */
	protected void fireViewRegistered(ViewState state) {
		ViewStateEvent e = new ViewStateEvent(this, state);

		List<ViewStateListener> copiedListeners = new ArrayList<ViewStateListener>(
				viewStateListeners);
		for (ViewStateListener listener : copiedListeners) {
			listener.viewRegistered(e);
		}
	}

	/**
	 * @param state
	 */
	protected void fireViewUnregistered(ViewState state) {
		ViewStateEvent e = new ViewStateEvent(this, state);

		List<ViewStateListener> copiedListeners = new ArrayList<ViewStateListener>(
				viewStateListeners);
		for (ViewStateListener listener : copiedListeners) {
			listener.viewUnregistered(e);
		}
	}

	/**
	 * @param listener
	 */
	public void addViewStateListener(ViewStateListener listener) {
		if (listener == null) {
			throw new NullArgumentException("listener");
		}

		viewStateListeners.add(listener);
	}

	/**
	 * @param listener
	 */
	public void removeViewStateListener(ViewStateListener listener) {
		if (listener == null) {
			throw new NullArgumentException("listener");
		}

		viewStateListeners.remove(listener);
	}

	/**
	 * @return the settings
	 */
	public Settings getSettings() {
		return settings;
	}

	/**
	 * @param settings
	 *            the settings to set
	 */
	public void setSettings(Settings settings) {
		this.settings = settings;
	}

	/**
	 * @return the dataSourceManager
	 */
	public DataSourceManager getDataSourceManager() {
		return dataSourceManager;
	}

	/**
	 * @param dataSourceManager
	 *            the dataSourceManager to set
	 */
	public void setDataSourceManager(DataSourceManager dataSourceManager) {
		this.dataSourceManager = dataSourceManager;
	}

	/**
	 * @return the checkInterval
	 */
	public long getCheckInterval() {
		return checkInterval;
	}

	/**
	 * @param checkInterval
	 *            the checkInterval to set
	 */
	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	/**
	 * @return the keepAliveInterval
	 */
	public long getKeepAliveInterval() {
		return keepAliveInterval;
	}

	/**
	 * @param keepAliveInterval
	 *            the keepAliveInterval to set
	 */
	public void setKeepAliveInterval(long keepAliveInterval) {
		this.keepAliveInterval = keepAliveInterval;
	}

	/**
	 * @return the expires
	 */
	public long getExpires() {
		return expires;
	}

	/**
	 * @param expires
	 *            the expires to set
	 */
	public void setExpires(long expires) {
		this.expires = expires;
	}
}