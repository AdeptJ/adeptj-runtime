package com.adeptj.runtime.server;

import com.adeptj.runtime.config.Configs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.websocket.OnClose;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import java.io.File;

import static com.adeptj.runtime.server.ServerLogsWebSocket.SERVER_LOGS_ENDPOINT;

/**
 * WebSocket for rendering server logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@ServerEndpoint(SERVER_LOGS_ENDPOINT)
public class ServerLogsWebSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLogsWebSocket.class);

    private static final String SERVER_LOG_FILE = "server-log-file";

    static final String SERVER_LOGS_ENDPOINT = "/server/logs";

    private ServerLogsTailer logsTailer;

    /**
     * Creates a ServerLogsTailer for each session.
     *
     * @param session the WebSocket Session
     */
    @OnOpen
    public void onOpen(Session session) {
        this.logsTailer = new ServerLogsTailer(new File(Configs.DEFAULT.logging().getString(SERVER_LOG_FILE)),
                session);
        this.logsTailer.initTailer();
    }

    @OnClose
    public void onClose(Session session) {
        this.logsTailer.stopTailer();
    }
}
