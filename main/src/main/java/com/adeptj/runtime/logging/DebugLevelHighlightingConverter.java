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

package com.adeptj.runtime.logging;

import ch.qos.logback.classic.pattern.color.HighlightingCompositeConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import static ch.qos.logback.classic.Level.DEBUG_INT;
import static ch.qos.logback.core.pattern.color.ANSIConstants.YELLOW_FG;

/**
 * Extended version of {@link HighlightingCompositeConverter} which prints debug log level in yellow.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class DebugLevelHighlightingConverter extends HighlightingCompositeConverter {

    @Override
    protected String getForegroundColorCode(ILoggingEvent event) {
        if (event.getLevel().toInt() == DEBUG_INT) {
            return YELLOW_FG;
        }
        return super.getForegroundColorCode(event);
    }
}
