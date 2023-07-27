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

import java.util.HashMap;
import java.util.Locale;

/**
 * TemplateData object for storing variables used in template rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public final class TemplateData extends HashMap<String, Object> {

    private final Locale locale;

    public TemplateData(Locale locale) {
        this.locale = locale;
    }

    public TemplateData addVariable(String key, Object value) {
        this.put(key, value);
        return this;
    }

    public Locale getLocale() {
        return locale;
    }
}
