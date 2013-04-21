package com.eyeq.pivot4j.analytics.datasource;

import java.io.Serializable;

import com.eyeq.pivot4j.state.Configurable;

public interface DataSourceDefinition extends Serializable, Configurable {

	String getName();

	String getDescription();
}
