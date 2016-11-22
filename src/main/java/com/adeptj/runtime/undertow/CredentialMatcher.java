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

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.stream.Collectors;

import org.slf4j.LoggerFactory;

import com.adeptj.runtime.config.Configs;

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
	
	private static final String OSGI_MGR_CFG_FILE = "/org/apache/felix/webconsole/internal/servlet/OsgiManager.config";

	private final String cfgFile;

	public CredentialMatcher() {
		this.cfgFile = new StringBuilder(Configs.INSTANCE.felix().getString("felix-cm-dir"))
				.append(OSGI_MGR_CFG_FILE.replace('/', File.separatorChar)).toString();
	}

	boolean match(String id, String pwd) {
		// When OsgiManager.config file is non-existent as configuration was never saved from OSGi console, make use of
		// default password maintained in provisioning file.
		return Files.exists(Paths.get(this.cfgFile)) ? this.fromOsgiManagerConfig(pwd) : this.fromProvisioningConfig(id, pwd);
	}

	private boolean fromProvisioningConfig(String id, String pwd) {
		return Configs.INSTANCE.undertow().getObject("common.user-credential-mapping").unwrapped().entrySet().stream()
				.filter(entry -> entry.getKey().equals(id)).anyMatch(entry -> Arrays
						.equals(this.bytes(this.hash(pwd)), this.bytes((String) entry.getValue())));
	}

	private boolean fromOsgiManagerConfig(String pwd) {
		boolean matched = false;
		try {
			String pwdLine = Files.readAllLines(Paths.get(this.cfgFile)).stream()
					.filter(line -> line.startsWith("password=")).collect(Collectors.joining()).replace("\\", "");
			matched = Arrays.equals(this.bytes(this.hash(pwd)),
					this.bytes(pwdLine.substring(pwdLine.indexOf('"') + 1, pwdLine.length() - 1)));
		} catch (Exception ex) {
			LoggerFactory.getLogger(CredentialMatcher.class).error("Exception!!", ex);
		}
		return matched;
	}

	private byte[] bytes(String pwdHash) {
		return Base64.getDecoder().decode(pwdHash.substring(pwdHash.indexOf('}') + 1));
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
