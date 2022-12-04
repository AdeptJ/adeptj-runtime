package com.adeptj.runtime.kernel.osgi;
public interface PackageExportsProvider {

    String OSGI_SYSTEM_PACKAGES_EXTRA_HEADER = "org.osgi.framework.system.packages.extra";

    String getPackageExports();
}
