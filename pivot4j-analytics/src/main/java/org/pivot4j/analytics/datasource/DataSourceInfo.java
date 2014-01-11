package org.pivot4j.analytics.datasource;

import java.io.Serializable;

import org.pivot4j.state.Configurable;

public interface DataSourceInfo extends Serializable, Configurable {

	String getName();

	String getDescription();
}
