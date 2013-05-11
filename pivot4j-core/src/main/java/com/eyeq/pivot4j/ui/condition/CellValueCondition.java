/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package com.eyeq.pivot4j.ui.condition;

import java.io.Serializable;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.lang.ObjectUtils;
import org.olap4j.Cell;
import org.olap4j.OlapException;
import org.olap4j.Position;
import org.olap4j.metadata.Member;

import com.eyeq.pivot4j.PivotException;
import com.eyeq.pivot4j.ui.RenderContext;

public class CellValueCondition extends AbstractCondition {

	public static final String NAME = "cellValue";

	public enum Criteria {

		Equals {
			@Override
			boolean matches(Double value1, Double value2) {
				return ObjectUtils.equals(value1, value2);
			}

			@Override
			String getOperator() {
				return "=";
			}
		},

		GreaterThan {
			@Override
			boolean matches(Double value1, Double value2) {
				if (value1 == null || value2 == null) {
					return false;
				}

				return value1.compareTo(value2) > 0;
			}

			@Override
			String getOperator() {
				return ">";
			}
		},

		GreaterThanEquals {
			@Override
			boolean matches(Double value1, Double value2) {
				return GreaterThan.matches(value1, value2)
						|| Equals.matches(value1, value2);
			}

			@Override
			String getOperator() {
				return ">=";
			}
		},

		LesserThan {
			@Override
			boolean matches(Double value1, Double value2) {
				if (value1 == null || value2 == null) {
					return false;
				}

				return value1.compareTo(value2) < 0;
			}

			@Override
			String getOperator() {
				return "<";
			}
		},

		LesserThanEquals {
			@Override
			boolean matches(Double value1, Double value2) {
				return LesserThan.matches(value1, value2)
						|| Equals.matches(value1, value2);
			}

			@Override
			String getOperator() {
				return "<=";
			}
		};

		abstract boolean matches(Double value1, Double value2);

		abstract String getOperator();
	}

	private Double value;

	private Criteria criteria;

	private List<String> positionUniqueNames;

	/**
	 * @param conditionFactory
	 */
	public CellValueCondition(ConditionFactory conditionFactory) {
		super(conditionFactory);
	}

	/**
	 * @param conditionFactory
	 * @param value
	 * @param criteria
	 */
	public CellValueCondition(ConditionFactory conditionFactory, Double value,
			Criteria criteria) {
		super(conditionFactory);

		this.value = value;
		this.criteria = criteria;
	}

	/**
	 * @see com.eyeq.kona.equation.AbstractCondition#getName()
	 */
	public String getName() {
		return NAME;
	}

	/**
	 * @return the value
	 */
	public Double getValue() {
		return value;
	}

	/**
	 * @param value
	 *            the value to set
	 */
	public void setValue(Double value) {
		this.value = value;
	}

	/**
	 * @return the criteria
	 */
	public Criteria getCriteria() {
		return criteria;
	}

	/**
	 * @param criteria
	 *            the criteria to set
	 */
	public void setCriteria(Criteria criteria) {
		this.criteria = criteria;
	}

	/**
	 * @return the positionUniqueNames
	 */
	public List<String> getPositionUniqueNames() {
		return positionUniqueNames;
	}

	/**
	 * @param positionUniqueNames
	 *            the positionUniqueNames to set
	 */
	public void setPositionUniqueNames(List<String> positionUniqueNames) {
		this.positionUniqueNames = positionUniqueNames;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.Condition#matches(com.eyeq.pivot4j.ui.RenderContext)
	 */
	@Override
	public boolean matches(RenderContext context) {
		if (value == null) {
			throw new IllegalStateException("Value is not specified.");
		}

		if (criteria == null) {
			throw new IllegalStateException("Criteria is not specified.");
		}

		Double cellValue = getCellValue(context);

		return value.equals(cellValue);
	}

	/**
	 * @param context
	 * @return
	 */
	protected Double getCellValue(RenderContext context) {
		Position position = getPosition(context);

		Cell cell = null;

		if (position == null) {
			cell = context.getCell();
		} else {
			Position columnPosition = null;
			Position rowPosition = null;

			if (position.equals(context.getColumnPosition())) {
				columnPosition = position;
				rowPosition = context.getRowPosition();
			} else if (position.equals(context.getRowPosition())) {
				columnPosition = context.getColumnPosition();
				rowPosition = position;
			}

			if (columnPosition != null && rowPosition != null) {
				cell = context.getCellSet()
						.getCell(columnPosition, rowPosition);
			}
		}

		if (cell == null) {
			return null;
		}

		try {
			return cell.getDoubleValue();
		} catch (OlapException e) {
			throw new PivotException(e);
		}
	}

	/**
	 * @param context
	 * @return
	 */
	protected Position getPosition(RenderContext context) {
		Position position = null;

		if (positionUniqueNames != null && !positionUniqueNames.isEmpty()) {
			String cacheKey = "cellValueCache" + hashCode();

			if (context.hasAttribute(cacheKey)) {
				position = (Position) context.getAttribute(cacheKey);
			}

			if (position == null) {
				if (matches(context.getColumnPosition())) {
					position = context.getColumnPosition();
				} else if (matches(context.getRowPosition())) {
					position = context.getRowPosition();
				}

				if (position != null) {
					context.setAttribute(cacheKey, position);
				}
			}
		}

		return position;
	}

	/**
	 * @param position
	 * @return
	 */
	protected boolean matches(Position position) {
		if (positionUniqueNames == null || position == null) {
			return false;
		}

		boolean matches = false;

		List<Member> members = position.getMembers();

		if (positionUniqueNames.size() == members.size()) {
			Iterator<String> it = positionUniqueNames.iterator();

			for (Member member : position.getMembers()) {
				matches = it.next().equals(member.getUniqueName());

				if (!matches) {
					break;
				}
			}
		}

		return matches;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#saveState()
	 */
	@Override
	public Serializable saveState() {
		Serializable[] states = new Serializable[3];

		states[0] = value;
		states[1] = criteria;

		if (positionUniqueNames != null) {
			states[2] = new LinkedList<String>(positionUniqueNames);
		}

		return states;
	}

	/**
	 * @see com.eyeq.pivot4j.state.Bookmarkable#restoreState(java.io.Serializable)
	 */
	@Override
	public void restoreState(Serializable state) {
		Serializable[] states = (Serializable[]) state;

		this.value = (Double) states[0];
		this.criteria = (Criteria) states[1];

		@SuppressWarnings("unchecked")
		List<String> names = (List<String>) states[2];
		this.positionUniqueNames = names;
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void saveSettings(HierarchicalConfiguration configuration) {
		super.saveSettings(configuration);

		if (criteria != null) {
			configuration.addProperty("criteria", criteria.name());
		}

		if (value != null) {
			configuration.addProperty("value", value);
		}

		if (positionUniqueNames != null) {
			int index = 0;
			for (String member : positionUniqueNames) {
				String name = String.format("position.member(%s)", index++);

				configuration.setProperty(name, member);
			}
		}
	}

	/**
	 * @see com.eyeq.pivot4j.state.Configurable#restoreSettings(org.apache.commons.configuration.HierarchicalConfiguration)
	 */
	@Override
	public void restoreSettings(HierarchicalConfiguration configuration) {
		String name = configuration.getString("criteria");

		if (name == null) {
			this.criteria = null;
		} else {
			this.criteria = Criteria.valueOf(name);
		}

		this.value = configuration.getDouble("value", null);

		try {
			List<HierarchicalConfiguration> members = configuration
					.configurationsAt("position.member");

			this.positionUniqueNames = new LinkedList<String>();

			for (HierarchicalConfiguration member : members) {
				positionUniqueNames.add(member.getString(""));
			}
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * @see com.eyeq.pivot4j.ui.condition.AbstractCondition#toString()
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("cellValue ");

		if (criteria != null) {
			builder.append(criteria.getOperator());
		}

		if (value != null) {
			builder.append(" ");
			builder.append(value);
		}

		return builder.toString();
	}
}
