/*
 * See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.xwiki.scripting.documentation.internal;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import javax.script.ScriptContext;

import org.apache.velocity.VelocityContext;
import org.xwiki.component.annotation.Component;
import org.xwiki.script.ScriptContextManager;
import org.xwiki.scripting.documentation.BindingKind;
import org.xwiki.velocity.VelocityManager;

/**
 * Search for bindings in the velocity context.
 *
 * @version $Id$
 */
@Component
@Named("velocity")
@Singleton
public class VelocityScriptBindingsFinder extends AbstractScriptBindingsFinder
{
    /**
     * Used to get the Velocity Context from which we retrieve the list of bound variables.
     */
    @Inject
    private VelocityManager velocityManager;

    /**
     * Used to get the Script Context from which we retrieve the list of bindings to exclude from velocity context.
     */
    @Inject
    private ScriptContextManager scriptContextManager;

    /**
     * @return the map of classes bindings in all velocity contexts
     */
    protected Map<String, Class<?>> getBindings()
    {
        VelocityContext vContext = this.velocityManager.getVelocityContext();
        ScriptContext scriptContext = this.scriptContextManager.getScriptContext();
        Map<String, Object> scriptBindings = Collections.emptyMap();

        if (scriptContext != null) {
            scriptBindings = scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
        }

        Map<String, Class<?>> bindings = new HashMap<String, Class<?>>();
        while (vContext != null) {
            addAllBinding(vContext, scriptBindings, bindings);
            vContext = (VelocityContext) vContext.getChainedContext();
        }

        return bindings;
    }

    @Override
    protected String getFullName(String name)
    {
        return name;
    }

    @Override
    protected BindingKind getType()
    {
        return BindingKind.VELOCITY;
    }

    private static void addAllBinding(VelocityContext vcontext, Map<String, Object> scriptBindings,
        Map<String, Class<?>> bindings)
    {
        for (Object key : vcontext.getKeys()) {
            String name = key.toString();
            Class<?> klass = vcontext.get(name).getClass();
            Object scriptBinding = scriptBindings.get(name);
            if (scriptBinding == null || !klass.equals(scriptBinding.getClass())) {
                bindings.put(name, klass);
            }
        }
    }
}
