/*
###############################################################################
#                                                                             # 
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Common Utilities
 *
 * @author Rakesh.Kumar, AdeptJ
 */
public class IOUtils {

    private static final int EOF = -1;

    private static final int DEFAULT_BUFFER_SIZE = 1024;

    private static final int OFFSET = 0;

    /**
     * Deny direct instantiation.
     */
    private IOUtils() {
    }

    public static String toString(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toString(Constants.UTF8);
    }

    public static byte[] toBytes(InputStream input) throws IOException {
        return toByteArrayOutputStream(input).toByteArray();
    }

    public static ByteArrayOutputStream toByteArrayOutputStream(InputStream input) throws IOException {
        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numberOfBytesRead;
        while ((numberOfBytesRead = input.read(buffer)) != EOF) {
            out.write(buffer, OFFSET, numberOfBytesRead);
        }
        return out;
    }
}
