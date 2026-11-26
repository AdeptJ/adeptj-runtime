/*
###############################################################################
#                                                                             #
#    Copyright 2016-2026, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel.util;

import com.adeptj.runtime.kernel.exception.ServerException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static jakarta.servlet.RequestDispatcher.ERROR_EXCEPTION;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests for {@link RequestUtil} and {@link IOUtils}.
 * <p>
 * {@link RequestUtil#getException(HttpServletRequest)} is the producer for the {@code DEV} mode error
 * page body described in <em>SEC-13</em>; {@link IOUtils} is used to buffer configuration and
 * credential material, so its buffering limits matter.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RequestUtil and IOUtils")
class RequestUtilAndIOUtilsTest {

    @Mock
    private HttpServletRequest request;

    @Nested
    @DisplayName("RequestUtil")
    class RequestHelpers {

        @Test
        void getAttributeDelegatesToTheRequest() {
            Object value = new Object();
            when(request.getAttribute("attr")).thenReturn(value);

            assertSame(value, RequestUtil.getAttribute(request, "attr"));
        }

        @Test
        void hasExceptionIsFalseWhenNothingWasDispatched() {
            assertFalse(RequestUtil.hasException(request));
        }

        @Test
        void hasExceptionIsTrueWhenTheContainerStoredAnException() {
            when(request.getAttribute(ERROR_EXCEPTION)).thenReturn(new IllegalStateException("boom"));

            assertTrue(RequestUtil.hasException(request));
        }

        @Test
        @DisplayName("getException serializes the whole trace, message included")
        void getExceptionReturnsTheWholeTrace() {
            when(request.getAttribute(ERROR_EXCEPTION))
                    .thenReturn(new IllegalStateException("jdbc:postgresql://db/prod?password=hunter2"));

            String trace = RequestUtil.getException(request);

            assertTrue(trace.contains("java.lang.IllegalStateException"));
            assertTrue(trace.contains("password=hunter2"),
                    "SEC-13: exception messages routinely carry secrets and the helper does not redact them");
            assertTrue(trace.contains("RequestUtilAndIOUtilsTest"),
                    "the trace also names internal classes and line numbers");
        }

        @Test
        @DisplayName("getException yields an empty body when nothing was dispatched")
        void getExceptionYieldsEmptyTextWhenThereIsNoException() {
            // commons-lang3's ExceptionUtils.getStackTrace(null) returns "" rather than throwing, so a
            // caller that forgets the hasException() guard renders an empty error body and the request
            // is silently reported as an error page instead of failing loudly.
            assertEquals("", RequestUtil.getException(request));
        }

        @Test
        void logoutDelegatesToTheContainer() throws ServletException {
            RequestUtil.logout(request);

            verify(request).logout();
        }

        @Test
        void logoutWrapsServletExceptionAsAserverException() throws ServletException {
            doThrow(new ServletException("unsupported")).when(request).logout();

            assertThrows(ServerException.class, () -> RequestUtil.logout(request));
        }

        @ParameterizedTest
        @ValueSource(strings = {"/", "/;jsessionid=ABC123", "/;jsessionid=ABC/about"})
        void contextRootRequestsAreRecognised(String uri) {
            assertTrue(RequestUtil.isContextRootRequest(uri), uri + " is the context root");
        }

        @ParameterizedTest
        @ValueSource(strings = {"/admin", "/admin/login", "/about", ""})
        void otherRequestsAreNotTheContextRoot(String uri) {
            assertFalse(RequestUtil.isContextRootRequest(uri), uri + " is not the context root");
        }

        @Test
        void theStringOverloadBacksTheRequestOverload() {
            when(request.getRequestURI()).thenReturn("/;jsessionid=XYZ");

            assertTrue(RequestUtil.isContextRootRequest(request));
        }
    }

    @Nested
    @DisplayName("IOUtils")
    class IoHelpers {

        @Test
        void readsATextStreamUsingUtf8() throws IOException {
            String text = "caf\u00e9 \u00fcber na\u00efve";

            assertEquals(text, IOUtils.toString(streamOf(text)));
        }

        @Test
        void readsABinaryStreamExactly() throws IOException {
            byte[] payload = new byte[20_000];
            for (int i = 0; i < payload.length; i++) {
                payload[i] = (byte) (i % 251);
            }

            assertArrayEquals(payload, IOUtils.toBytes(new ByteArrayInputStream(payload)));
        }

        @Test
        void anEmptyStreamYieldsEmptyOutput() throws IOException {
            assertEquals(0, IOUtils.toBytes(new ByteArrayInputStream(new byte[0])).length);
            assertEquals("", IOUtils.toString(new ByteArrayInputStream(new byte[0])));
        }

        @Test
        void propagatesIoFailures() {
            InputStream failing = new FilterInputStream(new ByteArrayInputStream("x".getBytes(StandardCharsets.UTF_8))) {
                @Override
                public int read(byte[] b, int off, int len) throws IOException {
                    throw new IOException("disk error");
                }
            };

            assertThrows(IOException.class, () -> IOUtils.toBytes(failing));
        }

        @Test
        @DisplayName("VULN: buffering is unbounded, the whole stream is retained in heap")
        void entireStreamIsBufferedInMemory() throws IOException {
            int size = 6 << 20; // 6MB, comfortably above the 8KB copy buffer
            byte[] result = IOUtils.toBytes(new ConstantStream(new byte[1 << 16], size));

            assertEquals(size, result.length,
                    "SEC-15: there is no size cap, so the caller's heap grows with the input. Reachable "
                            + "from the mutable deployment/conf directory (SEC-11) and from uploads");
        }

        @Test
        @DisplayName("VULN: a stream that reports zero bytes read never terminates the copy loop")
        void zeroLengthReadsKeepTheLoopSpinning() throws IOException {
            // read() is allowed to return 0 without meaning end-of-stream. The loop only stops on -1, so
            // such a stream is an unbounded CPU loop. Assert the observable precursor: 0 contributes
            // nothing and does not end the read.
            int[] reads = {0};
            InputStream alwaysZero = new InputStream() {
                @Override
                public int read() {
                    return 0;
                }

                @Override
                public int read(byte[] b, int off, int len) {
                    reads[0]++;
                    return reads[0] > 5 ? -1 : 0;
                }
            };

            assertEquals(0, IOUtils.toBytes(alwaysZero).length);
            assertEquals(6, reads[0], "six attempts, only the -1 stopped the loop");
        }

        private static InputStream streamOf(String text) {
            return new ByteArrayInputStream(text.getBytes(StandardCharsets.UTF_8));
        }

        /** Yields {@code block} repeatedly until {@code total} bytes have been produced. */
        private static final class ConstantStream extends InputStream {

            private final byte[] block;
            private int remaining;

            ConstantStream(byte[] block, int total) {
                this.block = block;
                this.remaining = total;
            }

            @Override
            public int read() {
                if (this.remaining-- <= 0) {
                    return -1;
                }
                return this.block[0];
            }

            @Override
            public int read(byte[] b, int off, int len) {
                if (this.remaining <= 0) {
                    return -1;
                }
                int n = Math.min(Math.min(len, this.block.length), this.remaining);
                System.arraycopy(this.block, 0, b, off, n);
                this.remaining -= n;
                return n;
            }
        }
    }
}
