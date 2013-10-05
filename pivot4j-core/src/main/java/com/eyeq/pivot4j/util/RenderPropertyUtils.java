/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.lang.StringUtils;

import com.eyeq.pivot4j.el.EvaluationFailedException;
import com.eyeq.pivot4j.ui.RenderContext;
import com.eyeq.pivot4j.ui.property.RenderProperty;
import com.eyeq.pivot4j.ui.property.RenderPropertyList;

public class RenderPropertyUtils {

	private RenderContext context;

	private Map<String, Map<String, EvaluationFailedException>> errors;

	private boolean suppressErrors;

	/**
	 * @param context
	 */
	public RenderPropertyUtils(RenderContext context) {
		if (context == null) {
			throw new NullArgumentException("context");
		}

		this.context = context;

		clearErrors();
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public Object getValue(String name, String category, Object defaultValue) {
		try {
			return getValue(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static Object getValue(String name, String category,
			Object defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Object value = null;

		RenderPropertyList properties = context.getRenderProperties().get(
				category);

		if (properties != null) {
			RenderProperty property = properties.getRenderProperty(name);

			if (property != null) {
				value = property.getValue(context);
			}
		}

		if (value == null) {
			value = defaultValue;
		}

		return value;
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public String getString(String name, String category, String defaultValue) {
		try {
			return getString(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static String getString(String name, String category,
			String defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Object value = getValue(name, category, null, context);

		String stringValue = StringUtils
				.trimToNull(ObjectUtils.toString(value));

		if (stringValue == null) {
			stringValue = defaultValue;
		}

		return stringValue;
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public Integer getInteger(String name, String category, Integer defaultValue) {
		try {
			return getInteger(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static Integer getInteger(String name, String category,
			Integer defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Integer intValue = defaultValue;

		Object value = getValue(name, category, defaultValue, context);

		if (value instanceof Integer) {
			intValue = (Integer) value;
		} else {
			value = StringUtils.trimToNull(ObjectUtils.toString(value));

			if (value != null) {
				intValue = Integer.parseInt((String) value);
			}
		}

		return intValue;
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public Float getFloat(String name, String category, Float defaultValue) {
		try {
			return getFloat(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static Float getFloat(String name, String category,
			Float defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Float floatValue = defaultValue;

		Object value = getValue(name, category, defaultValue, context);

		if (value instanceof Float) {
			floatValue = (Float) value;
		} else {
			value = StringUtils.trimToNull(ObjectUtils.toString(value));

			if (value != null) {
				floatValue = Float.parseFloat((String) value);
			}
		}

		return floatValue;
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public Double getDouble(String name, String category, Double defaultValue) {
		try {
			return getDouble(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static Double getDouble(String name, String category,
			Double defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Double doubleValue = defaultValue;

		Object value = getValue(name, category, defaultValue, context);

		if (value instanceof Double) {
			doubleValue = (Double) value;
		} else {
			value = StringUtils.trimToNull(ObjectUtils.toString(value));

			if (value != null) {
				doubleValue = Double.parseDouble((String) value);
			}
		}

		return doubleValue;
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @return
	 */
	public Boolean getBoolean(String name, String category, Boolean defaultValue) {
		try {
			return getBoolean(name, category, defaultValue, context);
		} catch (EvaluationFailedException e) {
			if (suppressErrors) {
				addError(name, category, e);
				return defaultValue;
			}

			throw e;
		}
	}

	/**
	 * @param name
	 * @param category
	 * @param defaultValue
	 * @param context
	 * @return
	 */
	public static Boolean getBoolean(String name, String category,
			Boolean defaultValue, RenderContext context) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (context == null) {
			throw new NullArgumentException("context");
		}

		Boolean booleanValue = defaultValue;

		Object value = getValue(name, category, defaultValue, context);

		if (value instanceof Boolean) {
			booleanValue = (Boolean) value;
		} else {
			value = StringUtils.trimToNull(ObjectUtils.toString(value));

			if (value != null) {
				booleanValue = Boolean.parseBoolean((String) value);
			}
		}

		return booleanValue;
	}

	/**
	 * @return the suppressErrors
	 */
	public boolean getSuppressErrors() {
		return suppressErrors;
	}

	/**
	 * @param suppressErrors
	 *            the suppressErrors to set
	 */
	public void setSuppressErrors(boolean suppressErrors) {
		this.suppressErrors = suppressErrors;
	}

	/**
	 * @param name
	 * @param category
	 * @param exception
	 */
	protected void addError(String name, String category,
			EvaluationFailedException exception) {
		if (name == null) {
			throw new NullArgumentException("name");
		}

		if (category == null) {
			throw new NullArgumentException("category");
		}

		if (exception == null) {
			throw new NullArgumentException("exception");
		}

		Map<String, EvaluationFailedException> errorsInCategory = errors
				.get(category);

		if (errorsInCategory == null) {
			errorsInCategory = new HashMap<String, EvaluationFailedException>();

			errors.put(category, errorsInCategory);
		}

		errorsInCategory.put(name, exception);
	}

	/**
	 * @param category
	 * @return
	 */
	public Map<String, EvaluationFailedException> getLastErrors(String category) {
		if (category == null) {
			throw new NullArgumentException("category");
		}

		Map<String, EvaluationFailedException> errorsInCategory = errors
				.get(category);

		if (errorsInCategory == null) {
			return Collections.emptyMap();
		}

		return Collections.unmodifiableMap(errorsInCategory);
	}

	public void clearErrors() {
		this.errors = new HashMap<String, Map<String, EvaluationFailedException>>();
	}
}
