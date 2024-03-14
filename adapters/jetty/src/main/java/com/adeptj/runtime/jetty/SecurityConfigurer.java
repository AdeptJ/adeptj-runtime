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

import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.eclipse.jetty.ee10.servlet.security.ConstraintMapping;
import org.eclipse.jetty.ee10.servlet.security.ConstraintSecurityHandler;
import org.eclipse.jetty.security.Constraint;
import org.eclipse.jetty.security.authentication.FormAuthenticator;

import java.util.HashSet;
import java.util.Set;

import static org.eclipse.jetty.security.Constraint.Authorization.SPECIFIC_ROLE;
import static org.eclipse.jetty.security.Constraint.Transport.INHERIT;

public class SecurityConfigurer {

    public void configure(ConstraintSecurityHandler securityHandler, UserManager userManager, Config commonCfg) {
        // Security settings
        securityHandler.setLoginService(new MVStoreLoginService(userManager));
        securityHandler.setAuthenticator(this.getFormAuthenticator(commonCfg));
        this.addConstraintMapping(securityHandler, commonCfg);
    }

    private FormAuthenticator getFormAuthenticator(Config commonCfg) {
        Config formAuthCfg = commonCfg.getConfig("form-auth");
        String loginUrl = formAuthCfg.getString("login-url");
        String errorUlr = formAuthCfg.getString("error-url");
        return new FormAuthenticator(loginUrl, errorUlr, true);
    }

    private void addConstraintMapping(ConstraintSecurityHandler securityHandler, Config commonCfg) {
        Set<String> roles = new HashSet<>(commonCfg.getStringList("auth-roles"));
        Constraint constraint = Constraint.from("AdeptJ Security Constraint", INHERIT, SPECIFIC_ROLE, roles);
        for (String protectedPath : commonCfg.getStringList("protected-paths")) {
            ConstraintMapping constraintMapping = new ConstraintMapping();
            constraintMapping.setConstraint(constraint);
            constraintMapping.setPathSpec(protectedPath);
            securityHandler.addConstraintMapping(constraintMapping);
        }
    }
}
