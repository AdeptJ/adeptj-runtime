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

package io.adeptj.runtime.common;

import java.text.MessageFormat;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

/**
 * Utility for providing execution time in different {@link java.util.concurrent.TimeUnit}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class Times {

    private static final String FMT_PATTERN = "{0,number} 'days' {1,number,00}:{2,number,00}:{3,number,00}.{4,number,000}";

    // No instances, just utility methods.
    private Times() {
    }

    /**
     * Returns elapsed time in milliseconds from the provided time in nanoseconds.
     *
     * @param startTime time in nanoseconds
     * @return elapsed time in milliseconds
     */
    public static long elapsedMillis(final long startTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - startTime);
    }

    /**
     * Returns elapsed time in seconds from the provided time in milliseconds.
     *
     * @param startTime time in milliseconds
     * @return elapsed time in seconds
     */
    public static long elapsedSeconds(final long startTime) {
        return MILLISECONDS.toSeconds(System.currentTimeMillis() - startTime);
    }

    /**
     * Formats the given start time in below format.
     * <p>
     * {0,number} 'days' {1,number,00}:{2,number,00}:{3,number,00}.{4,number,000}
     *
     * @param startTime the start time.
     * @return formatted time.
     */
    public static String format(final long startTime) {
        long period = System.currentTimeMillis() - startTime;
        final long millis = period % 1000;
        final long seconds = period / 1000 % 60;
        final long minutes = period / 1000 / 60 % 60;
        final long hours = period / 1000 / 60 / 60 % 24;
        final long days = period / 1000 / 60 / 60 / 24;
        return MessageFormat.format(FMT_PATTERN, days, hours, minutes, seconds, millis);
    }
}
