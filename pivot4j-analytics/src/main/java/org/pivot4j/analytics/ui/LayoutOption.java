/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pivot4j.analytics.ui;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author Judilson
 */
@ManagedBean(name = "layoutOption")
@SessionScoped
public class LayoutOption {

    private Boolean enableTheme;
    private Boolean enableMdx;

    public void changeOptionTheme() {

        if (enableMdx) {
            setEnableMdx(true);
        } else {
            setEnableMdx(false);
        }

    }

    public void changeOptionMdx() {

        if (enableMdx) {
            setEnableMdx(true);
        } else {
            setEnableMdx(false);
        }

    }

    public Boolean getEnableTheme() {
        return enableTheme;
    }

    public void setEnableTheme(Boolean enableTheme) {
        this.enableTheme = enableTheme;
    }

    public Boolean getEnableMdx() {
        return enableMdx;
    }

    public void setEnableMdx(Boolean enableMdx) {
        this.enableMdx = enableMdx;
    }
}
