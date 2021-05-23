/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.runtime.htmlrender;

import org.trimou.engine.resolver.Mapper;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

/**
 * TemplateData object for storing variables used in template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateData implements Mapper, Iterable<Entry<String, Object>> {

    private final Locale locale;

    private Map<String, Object> variables;

    public TemplateData(Locale locale) {
        this.locale = locale;
    }

    public TemplateData with(String key, Object value) {
        if (this.variables == null) {
            this.variables = new HashMap<>(8);
        }
        this.variables.put(key, value);
        return this;
    }

    public Locale getLocale() {
        return locale;
    }

    @Override
    public Object get(String key) {
        return this.variables.get(key);
    }

    @Override
    public Iterator<Entry<String, Object>> iterator() {
        return this.variables.entrySet().iterator();
    }
}
