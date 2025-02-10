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
package com.adeptj.runtime.kernel.util;

import com.typesafe.config.Config;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

/**
 * Utility methods for typesafe {@link Config} object.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class Configs {

    /**
     * Checks whether a value is present and non-null at the given path.
     *
     * @return true or false basis a value is present and non-null at the given path.
     */
    public static boolean isPresent(Config config, String path) {
        if (config == null || StringUtils.isEmpty(path)) {
            return false;
        }
        return config.hasPath(path);
    }

    public static List<String> getStringList(Config config, String path) {
        if (isPresent(config, path)) {
            return config.getStringList(path);
        }
        return Collections.emptyList();
    }
}
