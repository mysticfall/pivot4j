/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.ui.property;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.lang.NullArgumentException;
import org.apache.commons.lang.ObjectUtils;
import org.pivot4j.el.ExpressionEvaluator;
import org.pivot4j.ui.RenderContext;
import org.pivot4j.ui.condition.Condition;
import org.pivot4j.ui.condition.ConditionFactory;

public class ConditionalRenderProperty extends AbstractRenderProperty {

    private String defaultValue;

    private ConditionFactory conditionFactory;

    private List<ConditionalValue> values;

    /**
     * @param conditionFactory
     */
    ConditionalRenderProperty(ConditionFactory conditionFactory) {
        if (conditionFactory == null) {
            throw new NullArgumentException("conditionFactory");
        }

        this.conditionFactory = conditionFactory;
    }

    /**
     * @param name
     * @param conditionFactory
     */
    public ConditionalRenderProperty(String name,
            ConditionFactory conditionFactory) {
        super(name);

        if (conditionFactory == null) {
            throw new NullArgumentException("conditionFactory");
        }

        this.conditionFactory = conditionFactory;
    }

    /**
     * @param name
     * @param defaultValue
     * @param values
     * @param conditionFactory
     */
    public ConditionalRenderProperty(String name, String defaultValue,
            List<ConditionalValue> values, ConditionFactory conditionFactory) {
        super(name);

        if (conditionFactory == null) {
            throw new NullArgumentException("conditionFactory");
        }

        this.defaultValue = defaultValue;
        this.values = values;
        this.conditionFactory = conditionFactory;
    }

    /**
     * @return the conditionFactory
     */
    protected ConditionFactory getConditionFactory() {
        return conditionFactory;
    }

    /**
     * @return the defaultValue
     */
    public String getDefaultValue() {
        return defaultValue;
    }

    /**
     * @param defaultValue the defaultValue to set
     */
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    /**
     * @return the values
     */
    public List<ConditionalValue> getValues() {
        return values;
    }

    /**
     * @param values the values to set
     */
    public void setValues(List<ConditionalValue> values) {
        this.values = values;
    }

    /**
     * @see
     * org.pivot4j.ui.property.RenderProperty#getValue(org.pivot4j.ui.RenderContext)
     */
    @Override
    public String getValue(RenderContext context) {
        if (values == null) {
            return null;
        }

        String value = null;

        for (ConditionalValue conditionValue : values) {
            if (conditionValue.getCondition().matches(context)) {
                value = conditionValue.getValue();
            }
        }

        if (value == null) {
            value = defaultValue;
        }

        if (value != null) {
            ExpressionEvaluator evaluator = context.getExpressionEvaluator();
            value = ObjectUtils.toString(evaluator.evaluate(value,
                    context.getExpressionContext()));
        }

        return value;
    }

    /**
     * @see org.pivot4j.ui.property.AbstractRenderProperty#saveState()
     */
    @Override
    public Serializable saveState() {
        Serializable[] states = null;

        if (values != null) {
            states = new Serializable[values.size()];

            int index = 0;
            for (ConditionalValue value : values) {
                Condition condition = value.getCondition();

                states[index++] = new Serializable[]{condition.getName(),
                    condition.saveState(), value.getValue()};
            }
        }

        return new Serializable[]{super.saveState(), defaultValue, states};
    }

    /**
     * @see
     * org.pivot4j.ui.property.AbstractRenderProperty#restoreState(java.io.Serializable)
     */
    @Override
    public void restoreState(Serializable state) {
        Serializable[] states = (Serializable[]) state;

        super.restoreState(states[0]);

        this.defaultValue = (String) states[1];

        Serializable[] conditionStates = (Serializable[]) states[2];

        if (conditionStates == null) {
            this.values = null;
        } else {
            this.values = new LinkedList<ConditionalValue>();

            for (Serializable conditionState : conditionStates) {
                Serializable[] stateValues = (Serializable[]) conditionState;

                Condition condition = conditionFactory
                        .createCondition((String) stateValues[0]);
                condition.restoreState(stateValues[1]);

                values.add(new ConditionalValue(condition,
                        (String) stateValues[2]));
            }
        }
    }

    /**
     * @see
     * org.pivot4j.ui.property.AbstractRenderProperty#saveSettings(org.apache.commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public void saveSettings(HierarchicalConfiguration configuration) {
        super.saveSettings(configuration);

        if (defaultValue != null) {
            configuration.setProperty("default", defaultValue);
        }

        if (values != null) {
            int index = 0;

            configuration.setProperty("conditions", "");

            SubnodeConfiguration configurations = configuration
                    .configurationAt("conditions");

            for (ConditionalValue value : values) {
                String prefix = String
                        .format("condition-property(%s)", index++);

                configurations.setProperty(prefix + ".condition", "");
                configurations.setProperty(prefix + ".value", value.getValue());

                SubnodeConfiguration conditionConfig = configurations
                        .configurationAt(prefix + ".condition");

                value.getCondition().saveSettings(conditionConfig);
            }
        }
    }

    /**
     * @see
     * org.pivot4j.ui.property.AbstractRenderProperty#restoreSettings(org.apache
     * .commons.configuration.HierarchicalConfiguration)
     */
    @Override
    public void restoreSettings(HierarchicalConfiguration configuration) {
        super.restoreSettings(configuration);

        this.defaultValue = configuration.getString("default");
        this.values = new LinkedList<ConditionalValue>();

        try {
            List<HierarchicalConfiguration> configurations = configuration
                    .configurationsAt("conditions.condition-property");

            for (HierarchicalConfiguration propertyConfig : configurations) {
                String name = propertyConfig.getString("condition[@name]");

                Condition condition = conditionFactory.createCondition(name);
                condition.restoreSettings(propertyConfig
                        .configurationAt("condition"));

                String value = propertyConfig.getString("value");

                values.add(new ConditionalValue(condition, value));
            }
        } catch (IllegalArgumentException e) {
        }
    }
}
