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
 * WebSocket for displaying server logs.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
@ServerEndpoint(SERVER_LOGS_ENDPOINT)
public class ServerLogsWebSocket {

    private static final Logger LOGGER = LoggerFactory.getLogger(ServerLogsWebSocket.class);

    private static final String SERVER_LOG_FILE = "server-log-file";

    static final String SERVER_LOGS_ENDPOINT = "/server/logs";

    private ServerLogsRenderer logsRenderer;

    @OnOpen
    public void onOpen(Session session) {
        this.logsRenderer = new ServerLogsRenderer(new File(Configs.DEFAULT.logging().getString(SERVER_LOG_FILE)),
                session);
        this.logsRenderer.initRenderer();
    }

    @OnClose
    public void onClose(Session session) {
        this.logsRenderer.stopRenderer();
    }
}
