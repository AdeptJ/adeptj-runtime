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
package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.apache.catalina.Context;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import java.util.List;

public class SecurityConfigurer {

    public void configure(Context context, UserManager userManager, Config commonConfig) {
        // SecurityConstraint
        context.addConstraint(this.getSecurityConstraint(commonConfig));
        // LoginConfig
        context.setLoginConfig(this.getLoginConfig(commonConfig));
        // Form Auth
        FormAuthenticator valve = new FormAuthenticator();
        valve.setLandingPage("/");
        valve.setCharacterEncoding(commonConfig.getString("default-encoding"));
        ((StandardContext)context).addValve(valve);
        // Realm and CredentialHandler
        MVStoreRealm realm = new MVStoreRealm(userManager);
        realm.setCredentialHandler(new MVStoreCredentialHandler(userManager));
        context.setRealm(realm);
    }

    private LoginConfig getLoginConfig(Config commonConfig) {
        Config formAuthCfg = commonConfig.getConfig("form-auth");
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setAuthMethod(formAuthCfg.getString("method"));
        loginConfig.setLoginPage(formAuthCfg.getString("login-url"));
        loginConfig.setErrorPage(formAuthCfg.getString("error-url"));
        loginConfig.setRealmName(formAuthCfg.getString("realm"));
        return loginConfig;
    }

    private SecurityConstraint getSecurityConstraint(Config commonConfig) {
        SecurityConstraint constraint = new SecurityConstraint();
        List<String> authRoles = commonConfig.getStringList("auth-roles");
        for (String authRole : authRoles) {
            constraint.addAuthRole(authRole);
        }
        SecurityCollection collection = new SecurityCollection();
        List<String> protectedPaths = commonConfig.getStringList("protected-paths");
        for (String protectedPath : protectedPaths) {
            collection.addPattern(protectedPath);
        }
        constraint.addCollection(collection);
        return constraint;
    }
}
