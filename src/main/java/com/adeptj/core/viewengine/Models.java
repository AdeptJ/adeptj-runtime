/** 
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
package com.adeptj.core.viewengine;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Models.
 * 
 * @author Rakesh.Kumar, AdeptJ.
 */
public class Models implements Map<String, Object>, Iterable<String> {

	private Map<String, Object> delegate = new HashMap<>();

	public int size() {
		return this.delegate.size();
	}

	public boolean isEmpty() {
		return this.delegate.isEmpty();
	}

	public boolean containsKey(Object key) {
		return this.delegate.containsKey(key);
	}

	public boolean containsValue(Object value) {
		return this.delegate.containsValue(value);
	}

	public Object get(Object key) {
		return this.delegate.get(key);
	}

	public Object put(String key, Object value) {
		return this.delegate.put(key, value);
	}

	public Object remove(Object key) {
		return this.delegate.remove(key);
	}

	public void putAll(Map<? extends String, ?> m) {
		this.delegate.putAll(m);
	}

	public void clear() {
		this.delegate.clear();
	}

	public Set<String> keySet() {
		return this.delegate.keySet();
	}

	public Collection<Object> values() {
		return this.delegate.values();
	}

	public Set<Map.Entry<String, Object>> entrySet() {
		return this.delegate.entrySet();
	}

	public boolean equals(Object o) {
		return this.delegate.equals(o);
	}

	public int hashCode() {
		return this.delegate.hashCode();
	}

	public Iterator<String> iterator() {
		return this.delegate.keySet().iterator();
	}
}
