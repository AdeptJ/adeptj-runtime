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

import java.util.stream.Stream;

/**
 * OSGi Framework event code to string mapping.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public enum FrameworkEvents {

    STARTED(1),

    ERROR(2),

    PACKAGES_REFRESHED(4),

    STOPPED(64),

    STOPPED_UPDATE(128),

    WAIT_TIMEDOUT(512),

    UNKNOWN(-1);

    private int code;

    FrameworkEvents(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public static String asString(int code) {
        return Stream.of(values())
                .filter(fe -> code == fe.getCode())
                .findFirst()
                .orElse(UNKNOWN)
                .toString();
    }
}
