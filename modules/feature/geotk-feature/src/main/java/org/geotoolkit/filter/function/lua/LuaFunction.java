/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2019, Geomatys
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.filter.function.lua;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.apache.sis.util.logging.Logging;
import org.geotoolkit.filter.DefaultPropertyName;
import org.geotoolkit.filter.function.AbstractFunction;
import org.opengis.filter.expression.Expression;
import org.opengis.filter.expression.PropertyName;

/**
 * Lua function.
 *
 * @author Johann Sorel (Geomatys)
 * @module
 */
public class LuaFunction extends AbstractFunction {

    private static final char VAR_CHARACTER = '_';

    private static final Set<Character> END_CHARACTERS = new HashSet<Character>();

    static{
        //commun formating characters
        END_CHARACTERS.add(' ');
        END_CHARACTERS.add('\t');
        END_CHARACTERS.add('\r');
        END_CHARACTERS.add('\n');

        //math caracters
        END_CHARACTERS.add('+');
        END_CHARACTERS.add('-');
        END_CHARACTERS.add('/');
        END_CHARACTERS.add('*');
        END_CHARACTERS.add('%');
        END_CHARACTERS.add(',');
        END_CHARACTERS.add(';');
        END_CHARACTERS.add(':');
        END_CHARACTERS.add('<');
        END_CHARACTERS.add('>');

        //others
        END_CHARACTERS.add('(');
        END_CHARACTERS.add(')');
        END_CHARACTERS.add('[');
        END_CHARACTERS.add(']');
        END_CHARACTERS.add('{');
        END_CHARACTERS.add('}');
        END_CHARACTERS.add('.');
    }

    private final String lua;

    public LuaFunction(final Expression expression) {
        super(LuaFunctionFactory.LUA, prepare(expression), null);
        lua = expression.evaluate(null, String.class);
    }

    /**
     * Map lua parameters to PropertyNames.
     * @param gvFunction
     * @return Expression[]
     */
    private static Expression[] prepare(final Expression gvFunction){
        final String str = gvFunction.evaluate(null, String.class);

        final List<Expression> properties = new ArrayList<Expression>();
        properties.add(gvFunction);

        String current = null;

        for(int i=0,n=str.length(); i<n; i++){
            char c = str.charAt(i);
            if(current != null){
                if(END_CHARACTERS.contains(c)){
                    if(!current.isEmpty()){
                        properties.add(new DefaultPropertyName(current));
                    }
                    current = null;
                }else{
                    current += c;
                }
            }else{
                if(c == VAR_CHARACTER){
                    current = "";
                }
            }
        }

        if(current != null && !current.isEmpty()){
            properties.add(new DefaultPropertyName(current));
        }

        return properties.toArray(new Expression[properties.size()]);
    }

    @Override
    public Object evaluate(final Object feature) {

        ScriptEngineManager mgr = new ScriptEngineManager();
        ScriptEngine e = mgr.getEngineByName("luaj");

        for(int i=1,n=parameters.size(); i<n; i++){
            final PropertyName property = (PropertyName) parameters.get(i);
            final Object value = property.evaluate(feature);
            e.put(VAR_CHARACTER+property.getPropertyName(), value);
        }

        try {
            return e.eval(lua);
        } catch (ScriptException ex) {
            Logging.getLogger("org.geotoolkit.filter.function.lua").log(Level.WARNING, null, ex);
        }

        return "";
    }



}
