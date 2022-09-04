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

package com.adeptj.runtime.logging;

import java.util.Set;

/**
 * Configurations for creating a Logback Logger.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class LoggerConfig {

    private final String configPid;

    private final Set<String> categories;

    private final String level;

    private final boolean additivity;

    LoggerConfig(String configPid, Set<String> categories, String level, boolean additivity) {
        this.configPid = configPid;
        this.categories = categories;
        this.level = level;
        this.additivity = additivity;
    }

    public String getConfigPid() {
        return configPid;
    }

    public Set<String> getCategories() {
        return this.categories;
    }

    public String getLevel() {
        return this.level;
    }

    public boolean isAdditivity() {
        return this.additivity;
    }
}
