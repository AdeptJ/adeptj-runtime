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
package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.UserManager;
import org.apache.catalina.realm.GenericPrincipal;
import org.apache.catalina.realm.RealmBase;

import java.security.Principal;

/**
 * Tomcat Catalina Realm implementation.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class MVStoreRealm extends RealmBase {

    private final UserManager userManager;

    public MVStoreRealm(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected String getPassword(String username) {
        return this.userManager.getPassword(username);
    }

    @Override
    protected Principal getPrincipal(String username) {
        return new GenericPrincipal(username, this.userManager.getRoles(username));
    }
}
