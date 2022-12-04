package com.adeptj.runtime.jetty.osgi;

import com.adeptj.runtime.kernel.osgi.PackageExportsProvider;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class JettyPackageExportsProvider implements PackageExportsProvider {

    @Override
    public String getPackageExports() {
        try (InputStream stream = this.getClass().getResourceAsStream("/package-exports.properties")) {
            Properties properties = new Properties();
            properties.load(stream);
            return properties.getProperty(OSGI_SYSTEM_PACKAGES_EXTRA_HEADER);
        } catch (IOException ex) {
            LoggerFactory.getLogger(this.getClass()).error(ex.getMessage(), ex);
        }
        return null;
    }
}
