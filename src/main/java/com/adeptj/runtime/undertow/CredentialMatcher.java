/** 
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.undertow;

import static com.adeptj.runtime.common.Constants.UTF8;

import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

import org.slf4j.LoggerFactory;

import com.adeptj.runtime.config.Configs;
import com.adeptj.runtime.osgi.WebConsolePasswordUpdateAware;

/**
 * CredentialMatcher, Logic for creating password hash and comparing submitted credential is same as implemented
 * in [org.apache.felix.webconsole.internal.servlet.Password]
 * 
 * Because, we want to be consistent with the hashing mechanism used by OSGi Web Console configuration management,
 * but supporting classes available there are package private and therefore can't be accessible to outside world.
 * 
 * @author Rakesh.Kumar, AdeptJ
 */
class CredentialMatcher {

	private static final String SHA256 = "SHA-256";

	boolean match(String id, String pwd) {
		// When OsgiManager.config file is non-existent as configuration was never saved from OSGi console, make use of
		// default password maintained in provisioning file.
		return WebConsolePasswordUpdateAware.getInstance().isPasswordSet() ? this.fromOsgiManagerConfig(pwd)
				: this.fromProvisioningConfig(id, pwd);
	}

	private boolean fromProvisioningConfig(String id, String pwd) {
		return Configs.INSTANCE.undertow().getObject("common.user-credential-mapping").unwrapped().entrySet().stream()
				.filter(entry -> entry.getKey().equals(id))
				.anyMatch(entry -> Arrays.equals(this.chars(this.hash(pwd)), this.chars((String) entry.getValue())));
	}

	private boolean fromOsgiManagerConfig(String pwd) {
		try {
			return Arrays.equals(this.chars(this.hash(pwd)), WebConsolePasswordUpdateAware.getInstance().getPassword());
		} catch (Exception ex) {
			// Don't care!!
		}
		return false;
	}

	private char[] chars(String pwdHash) {
		return pwdHash.toCharArray();
	}

	private String hash(String pwd) {
		String hashPassword = pwd;
		try {
			MessageDigest md = MessageDigest.getInstance(SHA256);
			hashPassword = new StringBuilder().append('{').append(SHA256.toLowerCase()).append('}')
					.append(new String(Base64.getEncoder().encode(md.digest(pwd.getBytes(UTF8))), UTF8)).toString();
		} catch (Exception ex) {
			LoggerFactory.getLogger(CredentialMatcher.class).error("Exception!!", ex);
		}
		return hashPassword;
	}
}
