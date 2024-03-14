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
package com.adeptj.runtime.jetty.osgi;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.osgi.PackageExportsProvider;
import org.eclipse.jetty.server.Server;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Export Jetty packages with the currently used Jetty version.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class JettyPackageExportsProvider implements PackageExportsProvider {

    @Override
    public String getName() {
        return "Jetty PackageExportsProvider";
    }

    @Override
    public String getPackageExports() {
        StringBuilder packageExportsBuilder = new StringBuilder();
        try {
            List<String> packageExports = ConfigProvider.getInstance()
                    .getApplicationConfig()
                    .getStringList("jetty.osgi.package-exports");
            for (String packageExport : packageExports) {
                packageExportsBuilder.append(packageExport);
            }
            packageExportsBuilder.append("version=")
                    .append("\"")
                    .append(Server.getVersion())
                    .append("\"");
        } catch (Exception ex) {
            LoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);
        }
        return packageExportsBuilder.toString();
    }
}
