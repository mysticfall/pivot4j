package org.pivot4j.analytics.ui;

import java.io.Serializable;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.pivot4j.ui.table.TableRenderer;

public class DefaultTableRenderer extends TableRenderer {

	private boolean visible = true;

	/**
	 * @return the visible
	 */
	public boolean isVisible() {
		return visible;
	}

	/**
	 * @param visible
	 *            the visible to set
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderer#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[2];

		int index = 0;

		states[index++] = super.saveState();
		states[index++] = visible;

		return states;
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderer#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		int index = 0;

		super.restoreState(states[index++]);

		this.visible = (Boolean) states[index++];
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderer#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		configuration.addProperty("[@visible]", visible);
	}

	/**
	 * @see org.pivot4j.ui.table.TableRenderer#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		super.restoreSettings(configuration);

		this.visible = configuration.getBoolean("[@visible]", true);
	}
}
