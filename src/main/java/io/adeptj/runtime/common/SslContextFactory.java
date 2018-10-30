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

package io.adeptj.runtime.common;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.security.GeneralSecurityException;

/**
 * Utilities for SSL/TLS.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class SslContextFactory {

    private SslContextFactory() {
    }

    public static SSLContext newSslContext(String defaultTLSVersion) throws GeneralSecurityException, IOException {
        String keyStoreLoc = System.getProperty("adeptj.rt.keyStore");
        String keyStorePwd = System.getProperty("adeptj.rt.keyStorePassword");
        String keyPwd = System.getProperty("adeptj.rt.keyPassword");
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(KeyStores.getKeyStore(keyStoreLoc, keyStorePwd.toCharArray()), keyPwd.toCharArray());
        SSLContext sslContext = SSLContext.getInstance(System.getProperty("tls.version", defaultTLSVersion));
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
