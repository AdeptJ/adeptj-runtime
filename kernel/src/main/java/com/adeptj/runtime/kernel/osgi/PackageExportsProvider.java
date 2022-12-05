package com.adeptj.runtime.kernel.osgi;

/**
 * SPI for providing OSGi export packages.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface PackageExportsProvider {

    String getPackageExports();
}
