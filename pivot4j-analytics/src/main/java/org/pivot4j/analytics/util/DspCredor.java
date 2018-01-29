/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pivot4j.analytics.util;

import java.io.Serializable;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

/**
 *
 * @author jjunior
 */
public class DspCredor implements Serializable {

    HttpSession session = (HttpSession) FacesContext.getCurrentInstance().getExternalContext().getSession(false);

    private String credor;

    public DspCredor() {
    }

    public DspCredor(String credor) {
        this.credor = credor;
    }

    public String getCredor() {

        try {
            credor = (String) session.getAttribute("idCredor");
        } catch (Exception e) {
            e.printStackTrace();
            credor = "0";
        }

        return credor;
    }

    public void setCredor(String credor) {
        this.credor = credor;
    }

}
