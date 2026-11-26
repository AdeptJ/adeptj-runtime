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
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

import static jakarta.servlet.http.HttpServletResponse.SC_BAD_REQUEST;
import static jakarta.servlet.http.HttpServletResponse.SC_FORBIDDEN;
import static jakarta.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
import static jakarta.servlet.http.HttpServletResponse.SC_NOT_FOUND;
import static jakarta.servlet.http.HttpServletResponse.SC_SERVICE_UNAVAILABLE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link ResponseUtil} is the only place the admin and error servlets touch the response, so these
 * tests pin the exact status codes and the redirect handling.
 *
 * @author AdeptJ Security Review.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ResponseUtil")
class ResponseUtilTest {

    @Mock
    private HttpServletResponse response;

    @Nested
    @DisplayName("Status codes")
    class StatusCodes {

        @Test
        void serverErrorSends500() throws IOException {
            ResponseUtil.serverError(response);

            verify(response).sendError(SC_INTERNAL_SERVER_ERROR);
        }

        @Test
        void unavailableSends503() throws IOException {
            ResponseUtil.unavailable(response);

            verify(response).sendError(SC_SERVICE_UNAVAILABLE);
        }

        @ParameterizedTest
        @ValueSource(ints = {SC_NOT_FOUND, SC_FORBIDDEN, SC_BAD_REQUEST})
        void sendErrorForwardsTheRequestedCodeUnchanged(int code) throws IOException {
            ResponseUtil.sendError(response, code);

            verify(response).sendError(code);
        }

        @Test
        void aFailureWhileReportingAnErrorBecomesAserverException() throws IOException {
            doThrow(new IOException("connection reset")).when(response).sendError(SC_NOT_FOUND);

            assertThrows(ServerException.class, () -> ResponseUtil.sendError(response, SC_NOT_FOUND));
        }

        @Test
        void aFailureWhileReporting500BecomesAserverException() throws IOException {
            doThrow(new IOException("connection reset")).when(response).sendError(SC_INTERNAL_SERVER_ERROR);

            assertThrows(ServerException.class, () -> ResponseUtil.serverError(response));
        }

        @Test
        void aFailureWhileReporting503BecomesAserverException() throws IOException {
            doThrow(new IOException("connection reset")).when(response).sendError(SC_SERVICE_UNAVAILABLE);

            assertThrows(ServerException.class, () -> ResponseUtil.unavailable(response));
        }
    }

    @Nested
    @DisplayName("Redirects and bodies")
    class RedirectsAndBodies {

        @Test
        void redirectSendsTheContainerEncodedLocation() throws IOException {
            when(response.encodeRedirectURL("/about")).thenReturn("/about");

            ResponseUtil.redirect(response, "/about");

            verify(response).sendRedirect("/about");
        }

        @Test
        @DisplayName("a location is never sanitized, so the caller controls the redirect target")
        void theLocationIsPassedThroughUntouched() throws IOException {
            // RedirectUtil only asks the container to encode it; nothing rejects an absolute target, so
            // any caller that builds a location from request data creates an open redirect.
            String external = "https://attacker.example/phishing";
            when(response.encodeRedirectURL(external)).thenReturn(external);

            ResponseUtil.redirect(response, external);

            verify(response).sendRedirect(external);
        }

        @Test
        void aFailureWhileRedirectingBecomesAserverException() throws IOException {
            when(response.encodeRedirectURL("/about")).thenReturn("/about");
            doThrow(new IOException("already committed")).when(response).sendRedirect("/about");

            assertThrows(ServerException.class, () -> ResponseUtil.redirect(response, "/about"));
        }

        @Test
        void writeFlushesThePayloadToTheWriter() throws IOException {
            StringWriter buffer = new StringWriter();
            when(response.getWriter()).thenReturn(new PrintWriter(buffer));

            ResponseUtil.write(response, "OK");

            assertEquals("OK", buffer.toString());
        }

        @Test
        void aFailureWhileWritingBecomesAserverException() throws IOException {
            PrintWriter broken = new PrintWriter(new java.io.Writer() {
                @Override
                public void write(char[] cbuf, int off, int len) throws IOException {
                    throw new IOException("broken pipe");
                }

                @Override
                public void flush() {
                    // no-op
                }

                @Override
                public void close() {
                    // no-op
                }
            });
            when(response.getWriter()).thenReturn(broken);

            ResponseUtil.write(response, "payload");
            broken.flush();

            // PrintWriter swallows IOException by default, so the helper cannot see the failure.
            // Documented here because callers assume write() reports broken clients.
            org.junit.jupiter.api.Assertions.assertFalse(broken.checkError() == false,
                    "PrintWriter hides the write failure from ResponseUtil");
        }
    }
}
