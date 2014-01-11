package org.pivot4j.analytics.component;

import javax.faces.component.UIComponent;
import javax.faces.component.behavior.Behavior;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.event.AjaxBehaviorListener;
import javax.faces.event.FacesListener;

public class ChangeEvent extends AjaxBehaviorEvent {

	private static final long serialVersionUID = -1474206753996627009L;

	private Object object;

	/**
	 * @param component
	 * @param behavior
	 * @param object
	 */
	public ChangeEvent(UIComponent component, Behavior behavior, Object object) {
		super(component, behavior);
		this.object = object;
	}

	/**
	 * @see javax.faces.event.AjaxBehaviorEvent#isAppropriateListener(javax.faces.event.FacesListener)
	 */
	@Override
	public boolean isAppropriateListener(FacesListener faceslistener) {
		return (faceslistener instanceof AjaxBehaviorListener);
	}

	/**
	 * @see javax.faces.event.AjaxBehaviorEvent#processListener(javax.faces.event.FacesListener)
	 */
	@Override
	public void processListener(FacesListener faceslistener) {
		((AjaxBehaviorListener) faceslistener).processAjaxBehavior(this);
	}

	/**
	 * @return
	 */
	public Object getObject() {
		return object;
	}
}