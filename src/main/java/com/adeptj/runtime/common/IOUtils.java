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

package com.adeptj.runtime.common;

import org.xnio.streams.Streams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.adeptj.runtime.common.Constants.UTF8;

/**
 * Common Utilities
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public final class IOUtils {

    /**
     * Deny direct instantiation.
     */
    private IOUtils() {
    }

    public static String toString(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toString(UTF8);
    }

    public static byte[] toBytes(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toByteArray();
    }

    private static ByteArrayOutputStream toByteArrayOutputStream(InputStream source) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Streams.copyStream(source, out);
        return out;
    }
}
