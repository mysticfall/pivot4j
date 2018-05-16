/*
 * ====================================================================
 * This software is subject to the terms of the Common Public License
 * Agreement, available at the following URL:
 *   http://www.opensource.org/licenses/cpl.html .
 * You must accept the terms of that agreement to use this software.
 * ====================================================================
 */
package org.pivot4j.el.freemarker;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.NullArgumentException;
import org.pivot4j.el.AbstractExpressionEvaluator;
import org.pivot4j.el.ExpressionContext;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreeMarkerExpressionEvaluator extends AbstractExpressionEvaluator {

    private Configuration configuration;

    private Map<String, Template> cache = new HashMap<String, Template>();

    /**
     * @param configuration
     */
    public FreeMarkerExpressionEvaluator(Configuration configuration) {
        if (configuration == null) {
            throw new NullArgumentException("configuration");
        }

        this.configuration = configuration;
    }

    /**
     * @return configuration
     */
    protected Configuration getConfiguration() {
        return configuration;
    }

    protected void clearTemplateCache() {
        cache.clear();
    }

    /**
     * @param expression
     */
    protected Template getTemplateFromCache(String expression) {
        return cache.get(expression);
    }

    /**
     * @param expression
     */
    protected void removeTemplateFromCache(String expression) {
        cache.remove(expression);
    }

    /**
     * @param expression
     * @param template
     */
    protected void putTemplateInCache(String expression, Template template) {
        cache.put(expression, template);
    }

    /**
     * @param expression
     * @return
     * @throws IOException
     */
    protected Template createTemplate(String expression) throws IOException {
        Template template = new Template(expression, new StringReader(
                expression), configuration);
        putTemplateInCache(expression, template);

        return template;
    }

    /**
     * @see
     * org.pivot4j.el.AbstractExpressionEvaluator#doEvaluate(java.lang.String,
     * org.pivot4j.el.ExpressionContext)
     */
    @Override
    protected Object doEvaluate(String expression, ExpressionContext context)
            throws Exception {
        Template template = getTemplateFromCache(expression);

        if (template == null) {
            template = createTemplate(expression);
        }

        Locale locale = (Locale) context.get("locale");
        if (locale != null) {
            template.setLocale(locale);
        }

        StringWriter writer = new StringWriter();

        template.process(context, writer);
        writer.flush();

        return writer.toString();
    }
}
