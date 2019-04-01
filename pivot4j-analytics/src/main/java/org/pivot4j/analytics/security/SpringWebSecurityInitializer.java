/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pivot4j.analytics.security;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 *
 * @author Judilson
 */
public class SpringWebSecurityInitializer extends AbstractSecurityWebApplicationInitializer {

    public SpringWebSecurityInitializer() {
        super(SecurityWebConfig.class);
    }
}


