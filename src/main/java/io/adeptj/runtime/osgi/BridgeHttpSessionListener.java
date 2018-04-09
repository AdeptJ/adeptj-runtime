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

package io.adeptj.runtime.osgi;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import static io.adeptj.runtime.osgi.HttpSessionEvents.SESSION_CREATED;
import static io.adeptj.runtime.osgi.HttpSessionEvents.SESSION_DESTROYED;

/**
 * An {@link HttpSessionListener} which propagates the {@link javax.servlet.http.HttpSession} create and destroy
 * event to Felix {@link org.apache.felix.http.base.internal.EventDispatcher}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class BridgeHttpSessionListener implements HttpSessionListener {

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        HttpSessionEvents.handleHttpSessionEvent(SESSION_CREATED, se);
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        HttpSessionEvents.handleHttpSessionEvent(SESSION_DESTROYED, se);
    }
}
