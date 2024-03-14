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

import java.io.IOException;
import java.net.URL;
import java.util.Enumeration;

public class ClasspathResource {

    public static URL toUrl(String resourceName, ClassLoader cl) {
        return cl.getResource(resourceName);
    }

    public static Enumeration<URL> toUrls(String resourceName, ClassLoader cl) throws IOException {
        return cl.getResources(resourceName);
    }

    private ClasspathResource() {
    }
}
