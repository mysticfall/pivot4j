/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.pivot4j.analytics.util;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import mondrian.i18n.LocalizingDynamicSchemaProcessor;
import mondrian.olap.Util;
import mondrian.spi.DynamicSchemaProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jjunior
 */
public class Dsp extends LocalizingDynamicSchemaProcessor implements DynamicSchemaProcessor {
    
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public String filter(String schemaUrl, Util.PropertyList connectInfo, InputStream stream) throws Exception {

        String credor = null;

        try {
            credor = new DspCredor().getCredor();
        } catch (Exception e) {
            e.printStackTrace();
        }

        String schema = super.filter(schemaUrl, connectInfo, stream);

        try {
            schema = schema.replaceAll("%CREDOR%", credor);
        } catch (Exception e) {
            schema = schema.replaceAll("%CREDOR%", "0");
        }

        return schema;
    }

    /**
     * Replaces the SECURITY_PATTERN clause in the given schema
     */
    String replaceSecurityPattern(String schema, String credor) {

        Matcher matcher = Pattern.compile("=$CREDOR").matcher(schema);
        StringBuffer sb = new StringBuffer();
        if (credor != null && !credor.isEmpty()) {
            while (matcher.find()) {
                matcher.appendReplacement(sb, credor);
            }
            return matcher.appendTail(sb).toString();
        } else {
            while (matcher.find()) {
                matcher.appendReplacement(sb, " IS NOT NULL");
            }
            return matcher.appendTail(sb).toString();
//            return schema.replaceAll("=$CREDOR", " IS NOT NULL");

        }

    }

}
