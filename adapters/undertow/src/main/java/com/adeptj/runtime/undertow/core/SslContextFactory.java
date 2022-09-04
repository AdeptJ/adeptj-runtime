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

package com.adeptj.runtime.undertow.core;

import com.typesafe.config.Config;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import static com.adeptj.runtime.kernel.Constants.KEY_KEYSTORE_TYPE;
import static com.adeptj.runtime.kernel.Constants.KEY_P12_FILE_LOCATION;
import static com.adeptj.runtime.kernel.Constants.KEY_P12_PASSWORD;
import static com.adeptj.runtime.kernel.Constants.KEY_TLS_VERSION;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_FILE_EXTERNAL;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_FILE_LOCATION;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_P12_PASSWORD;
import static com.adeptj.runtime.kernel.Constants.SYS_PROP_TLS_VERSION;

/**
 * Utilities for SSL/TLS.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class SslContextFactory {

    private SslContextFactory() {
    }

    public static SSLContext newSslContext(Config httpsConf) throws GeneralSecurityException {
        String p12Loc;
        char[] p12Pwd;
        boolean p12FileExternal = Boolean.getBoolean(SYS_PROP_P12_FILE_EXTERNAL);
        if (p12FileExternal) {
            p12Loc = System.getProperty(SYS_PROP_P12_FILE_LOCATION);
            p12Pwd = System.getProperty(SYS_PROP_P12_PASSWORD).toCharArray();
        } else {
            p12Loc = httpsConf.getString(KEY_P12_FILE_LOCATION);
            p12Pwd = httpsConf.getString(KEY_P12_PASSWORD).toCharArray();
        }
        String keyStoreType = httpsConf.getString(KEY_KEYSTORE_TYPE);
        KeyStore keyStore = KeyStores.getKeyStore(p12FileExternal, keyStoreType, p12Loc, p12Pwd);
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, p12Pwd);
        String protocol = System.getProperty(SYS_PROP_TLS_VERSION, httpsConf.getString(KEY_TLS_VERSION));
        SSLContext sslContext = SSLContext.getInstance(protocol);
        // tm is initialized by SSLContext impl, that's why passing a null.
        sslContext.init(kmf.getKeyManagers(), null, null);
        return sslContext;
    }
}
