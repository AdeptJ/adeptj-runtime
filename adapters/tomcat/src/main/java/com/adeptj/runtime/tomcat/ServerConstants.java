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

public class ServerConstants {

    public static final String SYMBOL_DASH = "-";

    public static final String CFG_KEY_BASE_DIR = "base-dir";

    public static final String CFG_KEY_RELAXED_PATH_CHARS = "connector.relaxed-path-chars";

    public static final String CFG_KEY_CONNECTOR_PROTOCOL = "connector.protocol";

    public static final String CFG_KEY_CONNECTOR_SERVER = "connector.server";

    public static final String CFG_KEY_CTX_PATH = "context-path";

    public static final String CFG_KEY_DOC_BASE = "doc-base";

    public static final String CFG_KEY_MAIN_COMMON = "main.common";

    public static final String CFG_KEY_LIB_PATH = "lib-path";

    public static final String CFG_KEY_WEBAPP_JAR_NAME = "webapp-jar-name";

    public static final String CFG_KEY_JAR_RES_INTERNAL_PATH = "jar-resource-internal-path";

    public static final String CFG_KEY_JAR_RES_WEBAPP_MT = "jar-resource-webapp-mount";

    public static final String CFG_KEY_ASYNC = "async";

    private ServerConstants() {
    }
}
