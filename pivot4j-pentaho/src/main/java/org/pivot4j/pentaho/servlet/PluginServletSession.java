package org.pivot4j.pentaho.servlet;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Enumeration;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionContext;
import javax.servlet.http.HttpSessionEvent;

import org.apache.commons.lang.ObjectUtils;
import org.apache.myfaces.webapp.StartupServletContextListener;
import org.pentaho.platform.api.engine.IPentahoSession;
import org.pentaho.platform.engine.core.system.PentahoSessionHolder;
import org.pentaho.platform.engine.core.system.PentahoSystem;

@SuppressWarnings("deprecation")
public class PluginServletSession implements HttpSession {

    private HttpSession wrappedSession;

    private PluginServletContext servletContext;

    /**
     * @param servletContext
     * @param session
     */
    public PluginServletSession(PluginServletContext servletContext,
            HttpSession session) {
        this.servletContext = servletContext;
        this.wrappedSession = session;

        IPentahoSession pentahoSession = (IPentahoSession) session
                .getAttribute(PentahoSystem.PENTAHO_SESSION_KEY);

        if (!(pentahoSession instanceof PentahoSessionProxy)) {
            if (pentahoSession == null) {
                pentahoSession = PentahoSessionHolder.getSession();
            }

            IPentahoSession proxy = (IPentahoSession) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class<?>[]{PentahoSessionProxy.class},
                    new InvocationHandlerImpl(pentahoSession));

            session.setAttribute(PentahoSystem.PENTAHO_SESSION_KEY, proxy);

            servletContext.getListener().sessionCreated(
                    new HttpSessionEvent(this));
        }
    }

    /**
     * @return the servletContext
     * @see javax.servlet.http.HttpSession#getServletContext()
     */
    public PluginServletContext getServletContext() {
        return servletContext;
    }

    /**
     * @return the wrappedSession
     */
    protected HttpSession getWrappedSession() {
        return wrappedSession;
    }

    /**
     * @return @see javax.servlet.http.HttpSession#getCreationTime()
     */
    public long getCreationTime() {
        return wrappedSession.getCreationTime();
    }

    /**
     * @return @see javax.servlet.http.HttpSession#getId()
     */
    public String getId() {
        return wrappedSession.getId();
    }

    /**
     * @return @see javax.servlet.http.HttpSession#getLastAccessedTime()
     */
    public long getLastAccessedTime() {
        return wrappedSession.getLastAccessedTime();
    }

    /**
     * @param interval
     * @see javax.servlet.http.HttpSession#setMaxInactiveInterval(int)
     */
    public void setMaxInactiveInterval(int interval) {
        wrappedSession.setMaxInactiveInterval(interval);
    }

    /**
     * @return @see javax.servlet.http.HttpSession#getMaxInactiveInterval()
     */
    public int getMaxInactiveInterval() {
        return wrappedSession.getMaxInactiveInterval();
    }

    /**
     * @return @deprecated @see
     * javax.servlet.http.HttpSession#getSessionContext()
     */
    public HttpSessionContext getSessionContext() {
        return wrappedSession.getSessionContext();
    }

    /**
     * @param name
     * @return
     * @see javax.servlet.http.HttpSession#getAttribute(java.lang.String)
     */
    public Object getAttribute(String name) {
        return wrappedSession.getAttribute(name);
    }

    /**
     * @param name
     * @return
     * @deprecated
     * @see javax.servlet.http.HttpSession#getValue(java.lang.String)
     */
    public Object getValue(String name) {
        return wrappedSession.getValue(name);
    }

    /**
     * @return @see javax.servlet.http.HttpSession#getAttributeNames()
     */
    public Enumeration<String> getAttributeNames() {
        return wrappedSession.getAttributeNames();
    }

    /**
     * @return @deprecated @see javax.servlet.http.HttpSession#getValueNames()
     */
    public String[] getValueNames() {
        return wrappedSession.getValueNames();
    }

    /**
     * @param name
     * @param value
     * @see javax.servlet.http.HttpSession#setAttribute(java.lang.String,
     * java.lang.Object)
     */
    public void setAttribute(String name, Object value) {
        wrappedSession.setAttribute(name, value);
        Object oldValue = getAttribute(name);

        wrappedSession.setAttribute(name, value);

        StartupServletContextListener listener = getServletContext()
                .getListener();
        if (oldValue == null) {
            listener.attributeAdded(new HttpSessionBindingEvent(this, name,
                    value));
        } else if (!ObjectUtils.equals(oldValue, value)) {
            listener.attributeReplaced(new HttpSessionBindingEvent(this, name,
                    value));
        }
    }

    /**
     * @param name
     * @param value
     * @deprecated
     * @see javax.servlet.http.HttpSession#putValue(java.lang.String,
     * java.lang.Object)
     */
    public void putValue(String name, Object value) {
        wrappedSession.putValue(name, value);
    }

    /**
     * @param name
     * @see javax.servlet.http.HttpSession#removeAttribute(java.lang.String)
     */
    public void removeAttribute(String name) {
        Object value = getAttribute(name);

        wrappedSession.removeAttribute(name);

        StartupServletContextListener listener = getServletContext()
                .getListener();
        listener.attributeRemoved(new HttpSessionBindingEvent(this, name, value));
    }

    /**
     * @param name
     * @deprecated
     * @see javax.servlet.http.HttpSession#removeValue(java.lang.String)
     */
    public void removeValue(String name) {
        wrappedSession.removeValue(name);
    }

    /**
     *
     * @see javax.servlet.http.HttpSession#invalidate()
     */
    public void invalidate() {
        wrappedSession.invalidate();
    }

    /**
     * @return @see javax.servlet.http.HttpSession#isNew()
     */
    public boolean isNew() {
        return wrappedSession.isNew();
    }

    private interface PentahoSessionProxy extends IPentahoSession {
    }

    private final class InvocationHandlerImpl implements InvocationHandler {

        private IPentahoSession session;

        private InvocationHandlerImpl(IPentahoSession session) {
            this.session = session;
        }

        /**
         * @throws InvocationTargetException
         * @throws IllegalAccessException
         * @throws
         * @see java.lang.reflect.InvocationHandler#invoke(java.lang.Object,
         * java.lang.reflect.Method, java.lang.Object[])
         */
        @Override
        public Object invoke(Object proxy, Method method, Object[] args)
                throws IllegalAccessException, InvocationTargetException {
            Object result = method.invoke(session, args);

            if (method.getName().equals("destroy")) {
                servletContext.getListener().sessionDestroyed(
                        new HttpSessionEvent(wrappedSession));

                Enumeration<?> names = getAttributeNames();

                while (names.hasMoreElements()) {
                    removeAttribute((String) names.nextElement());
                }
            }

            return result;
        }
    }
}
