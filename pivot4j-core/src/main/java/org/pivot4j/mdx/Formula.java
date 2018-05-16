/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.mdx;

import java.util.ArrayList;
import java.util.List;

/**
 * Formula representing a named set or calculated member definition (WITH
 * expression).
 */
public class Formula extends AbstractExp {

    private static final long serialVersionUID = 4575864552263862759L;

    public enum Type {
        MEMBER, SET
    }

    private CompoundId name;

    private Exp exp;

    private Type type;

    private List<Property> properties = new ArrayList<Property>();

    public Formula() {
    }

    /**
     * @param name
     * @param exp
     * @param type
     */
    public Formula(CompoundId name, Exp exp, Type type) {
        this.name = name;
        this.exp = exp;
        this.type = type;
    }

    /**
     * @return name
     */
    public CompoundId getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    public void setName(CompoundId name) {
        this.name = name;
    }

    /**
     * @return exp
     */
    public Exp getExp() {
        return exp;
    }

    /**
     * @param exp the exp to set
     */
    public void setExp(Exp exp) {
        this.exp = exp;
    }

    /**
     * @return the type
     */
    public Type getType() {
        return type;
    }

    /**
     * @param type the type to set
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * @return the properties
     */
    public List<Property> getProperties() {
        return properties;
    }

    /**
     * format to MDX
     */
    public String toMdx() {
        StringBuilder sb = new StringBuilder();

        if (type != null) {
            sb.append(type.name());
            sb.append(' ');
        }

        if (name != null) {
            sb.append(name.toMdx());
        }

        sb.append(" AS '");

        if (exp != null) {
            sb.append(exp.toMdx());
        }

        sb.append('\'');

        for (Property property : properties) {
            sb.append(',');
            sb.append(property.toMdx());
        }

        return sb.toString();
    }

    /**
     * @see org.pivot4j.mdx.Exp#copy()
     */
    public Formula copy() {
        Formula clone = new Formula();

        clone.type = type;

        if (name != null) {
            clone.name = name.copy();
        }

        if (exp != null) {
            clone.exp = exp.copy();
        }

        for (Property property : properties) {
            clone.properties.add(property.copy());
        }

        return clone;
    }

    /**
     * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
     */
    @Override
    public void accept(ExpVisitor visitor) {
        visitor.visitFormula(this);

        if (name != null) {
            name.accept(visitor);
        }

        if (exp != null) {
            exp.accept(visitor);
        }

        for (Property property : properties) {
            property.accept(visitor);
        }
    }

    public static class Property extends AbstractExp {

        private static final long serialVersionUID = 519325113391951347L;

        private String name;

        private Exp exp;

        public Property() {
        }

        /**
         * @param name
         * @param exp
         */
        public Property(String name, Exp exp) {
            this.name = name;
            this.exp = exp;
        }

        /**
         * @return the name
         */
        public String getName() {
            return name;
        }

        /**
         * @param name the name to set
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * @return The expression that makes up the value of the member property
         */
        public Exp getExp() {
            return exp;
        }

        /**
         * @param exp the exp to set
         */
        public void setExp(Exp exp) {
            this.exp = exp;
        }

        /**
         * format to MDX
         */
        public String toMdx() {
            StringBuilder sb = new StringBuilder();

            if (name != null) {
                sb.append(name);
            }

            sb.append(" = ");

            if (exp != null) {
                sb.append(exp.toMdx());
            }

            return sb.toString();
        }

        /**
         * @see org.pivot4j.mdx.Exp#copy()
         */
        public Property copy() {
            Property clone = new Property();
            clone.name = name;

            if (exp != null) {
                clone.exp = exp.copy();
            }

            return clone;
        }

        /**
         * @see org.pivot4j.mdx.Exp#accept(org.pivot4j.mdx.ExpVisitor)
         */
        public void accept(ExpVisitor visitor) {
            visitor.visitFormulaProperty(this);

            if (exp != null) {
                exp.accept(visitor);
            }
        }
    }
}
