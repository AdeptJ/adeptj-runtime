package com.adeptj.runtime.tomcat;

import com.typesafe.config.Config;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.coyote.http11.AbstractHttp11Protocol;
import org.apache.coyote.http2.Http2Protocol;

import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_CONNECTOR_PROTOCOL;
import static com.adeptj.runtime.tomcat.ServerConstants.CFG_KEY_CONNECTOR_SERVER;

public class ConnectorConfigurer {

    public void configure(int port, Tomcat tomcat, Config serverConfig) {
        Connector connector = new Connector(serverConfig.getString(CFG_KEY_CONNECTOR_PROTOCOL));
        connector.setPort(port);
        AbstractHttp11Protocol<?> protocol = (AbstractHttp11Protocol<?>) connector.getProtocolHandler();
        protocol.setServer(serverConfig.getString(CFG_KEY_CONNECTOR_SERVER));
        protocol.addUpgradeProtocol(new Http2Protocol()); // This will enable h2c
        tomcat.setConnector(connector);
    }
}
