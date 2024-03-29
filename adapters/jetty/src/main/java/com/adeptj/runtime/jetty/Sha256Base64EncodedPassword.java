/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.jetty;

import com.adeptj.runtime.kernel.util.PasswordEncoder;
import org.eclipse.jetty.util.security.Password;

import java.io.Serial;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Jetty {@link Password} extension for SHA 256/Base 64 encoded password matching.
 *
 * @author Rakesh Kumar, AdeptJ
 */
public class Sha256Base64EncodedPassword extends Password {

    @Serial
    private static final long serialVersionUID = 4019843572977985299L;

    public Sha256Base64EncodedPassword(String password) {
        super(password);
    }

    @Override
    public boolean check(Object credentials) {
        byte[] encodedPassword = PasswordEncoder.encodePassword((String) credentials);
        return super.check(new String(encodedPassword, UTF_8));
    }
}
