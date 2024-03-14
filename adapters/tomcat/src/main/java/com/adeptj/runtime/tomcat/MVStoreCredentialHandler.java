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
import org.apache.catalina.CredentialHandler;

public class MVStoreCredentialHandler implements CredentialHandler {

    private final UserManager userManager;

    public MVStoreCredentialHandler(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    public boolean matches(String inputCredentials, String storedCredentials) {
        return this.userManager.matchPassword(inputCredentials, storedCredentials);
    }

    /**
     * This is just called to waste a bit of time as not to reveal that the user does not exist.
     *
     * @param inputCredentials User provided credentials
     * @return encoded version of passed credentials.
     */
    @Override
    public String mutate(String inputCredentials) {
        return this.userManager.encodePassword(inputCredentials);
    }
}
