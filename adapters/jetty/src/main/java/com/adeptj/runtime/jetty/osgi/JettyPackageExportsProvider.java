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
