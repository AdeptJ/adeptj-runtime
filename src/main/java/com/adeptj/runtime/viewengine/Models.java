/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.viewengine;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Models for storing data used in view rendering.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class Models extends AbstractMap<String, Object> implements Iterable<Entry<String, Object>> {

    private Map<String, Object> delegate;
    
    public Models() {
    	this.delegate = new HashMap<>();
    }

    @Override
    public Object put(String key, Object value) {
        return this.delegate.put(key, value);
    }

    @Override
    public Iterator<Entry<String, Object>> iterator() {
        return this.entrySet().iterator();
    }

    @Override
    public Set<Entry<String, Object>> entrySet() {
        return this.delegate.entrySet();
    }
}
