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

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;

import static com.adeptj.runtime.kernel.Constants.H2_MAP_ADMIN_CREDENTIALS;
import static com.adeptj.runtime.kernel.Constants.MV_CREDENTIALS_STORE;

public final class MVStoreUtil {

    private MVStoreUtil() {
    }

    public static String getValue(String storeName, String mapName, String key) {
        try (MVStore store = MVStore.open(storeName)) {
            MVMap<String, String> mvMap = store.openMap(mapName);
            return mvMap.get(key);
        }
    }

    public static String getPassword(String username) {
        return getValue(MV_CREDENTIALS_STORE, H2_MAP_ADMIN_CREDENTIALS, username);
    }
}
