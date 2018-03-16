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

package com.adeptj.runtime.server;

import com.adeptj.runtime.config.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.File;
import java.util.Optional;
import java.util.concurrent.RejectedExecutionException;

import static com.adeptj.runtime.server.ServerLogsWebSocket.SERVER_LOGS_ENDPOINT;

/**
 * WebSocket for rendering server logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@ServerEndpoint(SERVER_LOGS_ENDPOINT)
public class ServerLogsWebSocket {

    static final String SERVER_LOGS_ENDPOINT = "/server/logs";

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLogsWebSocket.class);

    private static final String SERVER_LOG_FILE = "server-log-file";

    private ServerLogsTailer logsTailer;

    /**
     * Creates a ServerLogsTailer for each session.
     *
     * @param session the WebSocket Session
     */
    @OnOpen
    public void onOpen(Session session) {
        try {
            session.getAsyncRemote().sendText("Server logs tailing will be started shortly!!");
            this.logsTailer = new ServerLogsTailer(new File(Configs.DEFAULT.logging().getString(SERVER_LOG_FILE)), session);
            this.logsTailer.startTailer();
        } catch (RejectedExecutionException ex) {
            LOGGER.error(ex.getMessage(), ex);
            session.getAsyncRemote().sendText(ex.getMessage());
        }
    }

    @OnClose
    public void onClose(Session session) {
        Optional.ofNullable(this.logsTailer).ifPresent(ServerLogsTailer::stopTailer);
    }
}
