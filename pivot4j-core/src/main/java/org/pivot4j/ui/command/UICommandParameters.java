package org.pivot4j.ui.command;

import java.io.Serializable;

public class UICommandParameters implements Serializable {

    private static final long serialVersionUID = 5176340826717067158L;

    private int axisOrdinal;

    private int positionOrdinal;

    private int memberOrdinal;

    private int hierarchyOrdinal;

    private int[] cellCoordinate;

    /**
     * @return the axisOrdinal
     */
    public int getAxisOrdinal() {
        return axisOrdinal;
    }

    /**
     * @param axisOrdinal the axisOrdinal to set
     */
    public void setAxisOrdinal(int axisOrdinal) {
        this.axisOrdinal = axisOrdinal;
    }

    /**
     * @return the positionOrdinal
     */
    public int getPositionOrdinal() {
        return positionOrdinal;
    }

    /**
     * @param positionOrdinal the positionOrdinal to set
     */
    public void setPositionOrdinal(int positionOrdinal) {
        this.positionOrdinal = positionOrdinal;
    }

    /**
     * @return the memberOrdinal
     */
    public int getMemberOrdinal() {
        return memberOrdinal;
    }

    /**
     * @param memberOrdinal the memberOrdinal to set
     */
    public void setMemberOrdinal(int memberOrdinal) {
        this.memberOrdinal = memberOrdinal;
    }

    /**
     * @return the hierarchyOrdinal
     */
    public int getHierarchyOrdinal() {
        return hierarchyOrdinal;
    }

    /**
     * @param hierarchyOrdinal the hierarchyOrdinal to set
     */
    public void setHierarchyOrdinal(int hierarchyOrdinal) {
        this.hierarchyOrdinal = hierarchyOrdinal;
    }

    /**
     * @return the cellCoordinate
     */
    public int[] getCellCoordinate() {
        return cellCoordinate;
    }

    /**
     * @param cellCoordinate the cellCoordinate to set
     */
    public void setCellCoordinate(int[] cellCoordinate) {
        this.cellCoordinate = cellCoordinate;
    }
}
