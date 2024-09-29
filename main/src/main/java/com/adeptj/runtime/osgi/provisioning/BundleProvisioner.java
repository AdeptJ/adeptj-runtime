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

package com.adeptj.runtime.osgi.provisioning;

import com.typesafe.config.Config;
import org.osgi.framework.BundleContext;

import java.io.IOException;

/**
 * Find, install and start the Bundles from given location using the System Bundle's BundleContext.
 * <p>
 * Note: The OSGi Bundles provisioning is inspired by Sling Launchpad. Thank you Sling Dev Team!
 *
 * @author Rakesh Kumar, AdeptJ
 */
public interface BundleProvisioner {

    String CFG_KEY_FELIX_CM_DIR = "felix-cm-dir";

    String BUNDLE_PROVISIONED_MSG = "Provisioned [{}] Bundles in: [{}] ms!!";

    String SYS_PROP_FORCE_PROVISION_BUNDLES = "force.provision.bundles";

    String DOT_JAR = ".jar";

    boolean installOrUpdateBundles(Config felixConf, BundleContext bundleContext) throws IOException;
}
