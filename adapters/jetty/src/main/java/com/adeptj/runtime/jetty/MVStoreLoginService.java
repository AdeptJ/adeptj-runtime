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
import org.eclipse.jetty.security.AbstractLoginService;
import org.eclipse.jetty.security.RolePrincipal;
import org.eclipse.jetty.security.UserPrincipal;
import org.eclipse.jetty.util.StringUtil;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MVStoreLoginService extends AbstractLoginService {

    private final UserManager userManager;

    public MVStoreLoginService(UserManager userManager) {
        this.userManager = userManager;
    }

    @Override
    protected List<RolePrincipal> loadRoleInfo(UserPrincipal user) {
        return this.userManager.getRoles(user.getName())
                .stream()
                .map(RolePrincipal::new)
                .collect(Collectors.toList());
    }

    @Override
    protected UserPrincipal loadUserInfo(String username) {
        String password = this.userManager.getPassword(username);
        if (StringUtil.isEmpty(password)) {
            // This is just called to waste a bit of time as not to reveal that the user does not exist.
            this.userManager.encodePassword(UUID.randomUUID().toString());
            return null;
        }
        return new UserPrincipal(username, new Sha256Base64EncodedPassword(password));
    }
}
