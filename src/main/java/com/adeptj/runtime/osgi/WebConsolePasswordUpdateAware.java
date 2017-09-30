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

package com.adeptj.runtime.osgi;

/**
 * WebConsolePasswordUpdateAware.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class WebConsolePasswordUpdateAware {

    private char[] password;

    /**
     * Singleton class
     */
    private WebConsolePasswordUpdateAware() {
    }

    public static WebConsolePasswordUpdateAware getInstance() {
        return Holder.getInstance();
    }

    public char[] getPassword() {
        return this.password;
    }

    public void setPassword(char[] pwd) {
        this.password = pwd;
    }

    public boolean isPasswordSet() {
        return this.password != null;
    }

    /**
     * WebConsolePasswordUpdateAware.Holder
     *
     * @author Rakesh.Kumar, AdeptJ
     */
    private static class Holder {

        private static final WebConsolePasswordUpdateAware INSTANCE = new WebConsolePasswordUpdateAware();

        private static WebConsolePasswordUpdateAware getInstance() {
            return INSTANCE;
        }
    }
}
