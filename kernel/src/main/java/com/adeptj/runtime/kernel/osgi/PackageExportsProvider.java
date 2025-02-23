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
package com.adeptj.runtime.kernel.osgi;

/**
 * SPI for providing OSGi export packages.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface PackageExportsProvider {

    default String getName() {
        return this.getClass().getName();
    }

    /**
     * Gets the java packages to be exported by OSGi from system bundle.
     *
     * @return comma separated packages with version number, if any.
     */
    String getPackageExports();
}
