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

import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

/**
 * Utilities for Java KeyStore.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
final class KeyStores {

    private KeyStores() {
    }

    static KeyStore getKeyStore(boolean p12FileExternal, String type, String p12Loc, char[] p12Pwd) {
        try (InputStream is = p12FileExternal
                ? Files.newInputStream(Paths.get(p12Loc)) : KeyStores.class.getResourceAsStream(p12Loc)) {
            KeyStore keyStore = KeyStore.getInstance(type);
            keyStore.load(is, p12Pwd);
            return keyStore;
        } catch (IOException | GeneralSecurityException ex) {
            throw new RuntimeInitializationException(ex);
        }
    }
}
