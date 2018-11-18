/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pivot4j.analytics.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 *
 * @author Judilson
 */
@EnableWebSecurity
public class SecurityWebConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(AuthenticationManagerBuilder auth)
            throws Exception {

        // in-memory authentication
        BCryptPasswordEncoder encoder = passwordEncoder();
        auth.inMemoryAuthentication().withUser("admin").password(encoder.encode("password")).roles("USER");
        // using custom UserDetailsService DAO
        // auth.userDetailsService(new AppUserDetailsServiceDAO());
        // using JDBC
//        Context ctx = new InitialContext();
//        DataSource ds = (DataSource) ctx
//                .lookup("java:/comp/env/jdbc/MyLocalDB");
//
//        final String findUserQuery = "select username,password,enabled "
//                + "from Employees " + "where username = ?";
//        final String findRoles = "select username,role " + "from Roles "
//                + "where username = ?";
//
//        auth.jdbcAuthentication().dataSource(ds)
//                .usersByUsernameQuery(findUserQuery)
//                .authoritiesByUsernameQuery(findRoles);
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable()
                .headers().frameOptions().disable()
                .and()
                .authorizeRequests()
                .antMatchers("/","/javax.faces.resource/**","/resource/**", "/home","/register","/input","/output","/output/*","/**/*.css","/**/*.js","/**/*.png","/**/*.svg","/**/*.map").permitAll()
                .anyRequest().authenticated()
                .and()
                .formLogin()
                .loginPage("/security/login.xhtml").permitAll()
                .loginProcessingUrl("/Pivot4jLogin")
                .failureUrl("/security/login.xhtml?error=true")
                .and()
                .logout()
                .logoutSuccessUrl("/security/login.xhtml");
    }
}
