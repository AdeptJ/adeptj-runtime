/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel;

import jakarta.servlet.Filter;

/**
 * {@link Filter} info object needed for deployment on AdeptJ Runtime.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class FilterInfo {

    private final String filterName;

    private final String pattern;

    private Class<? extends Filter> filterClass;

    private Filter filterInstance;

    public FilterInfo(String filterName, String pattern) {
        this.filterName = filterName;
        this.pattern = pattern;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getPattern() {
        return pattern;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(Class<? extends Filter> filterClass) {
        this.filterClass = filterClass;
    }

    public Filter getFilterInstance() {
        return filterInstance;
    }

    public void setFilterInstance(Filter filterInstance) {
        this.filterInstance = filterInstance;
    }
}
