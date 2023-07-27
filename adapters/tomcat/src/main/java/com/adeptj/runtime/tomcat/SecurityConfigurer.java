package com.adeptj.runtime.tomcat;

import com.adeptj.runtime.kernel.UserManager;
import com.typesafe.config.Config;
import org.apache.catalina.authenticator.FormAuthenticator;
import org.apache.catalina.core.StandardContext;
import org.apache.tomcat.util.descriptor.web.LoginConfig;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;

import java.util.List;

public class SecurityConfigurer {

    public void configure(StandardContext context, UserManager userManager, Config commonConfig) {
        // SecurityConstraint
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
        context.addConstraint(constraint);
        // LoginConfig
        Config formAuthCfg = commonConfig.getConfig("form-auth");
        LoginConfig loginConfig = new LoginConfig();
        loginConfig.setAuthMethod(formAuthCfg.getString("method"));
        loginConfig.setLoginPage(formAuthCfg.getString("login-url"));
        loginConfig.setErrorPage(formAuthCfg.getString("error-url"));
        loginConfig.setRealmName(formAuthCfg.getString("realm"));
        context.setLoginConfig(loginConfig);
        // Form Auth
        FormAuthenticator valve = new FormAuthenticator();
        valve.setLandingPage("/");
        valve.setCharacterEncoding(commonConfig.getString("default-encoding"));
        context.addValve(valve);
        // Realm and CredentialHandler
        MVStoreRealm realm = new MVStoreRealm(userManager);
        realm.setCredentialHandler(new MVStoreCredentialHandler(userManager));
        context.setRealm(realm);
    }
}
