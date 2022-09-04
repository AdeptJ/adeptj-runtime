package com.adeptj.runtime.kernel;

import com.adeptj.runtime.kernel.security.MVStoreUserManager;
import com.typesafe.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_PORT;

public abstract class AbstractServer implements Server {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    private final UserManager userManager;

    public AbstractServer() {
        this.userManager = new MVStoreUserManager();
    }

    @Override
    public void postStart() {
        Runtime.getRuntime().addShutdownHook(new ShutdownHook(this, "AdeptJ Terminator"));
    }

    @Override
    public void preStop() {
        this.logger.info("Stopping {}", this.getRuntime().getName());
    }

    @Override
    public void registerServlets(List<ServletInfo> servletInfos) {
        for (ServletInfo info : servletInfos) {
            this.doRegisterServlet(info);
        }
    }

    @Override
    public void registerFilters(List<FilterInfo> filterInfos) {
        for (FilterInfo info : filterInfos) {
            this.doRegisterFilter(info);
        }
    }

    @Override
    public UserManager getUserManager() {
        return this.userManager;
    }

    protected abstract void doRegisterServlet(ServletInfo info);

    protected abstract void doRegisterFilter(FilterInfo info);

    protected int resolvePort(Config appConfig) {
        ServerRuntime runtime = this.getRuntime();
        Integer port = Integer.getInteger("adeptj.rt.port");
        if (port == null || port == 0) {
            this.logger.info("No port specified via system property: [{}], will resolve port from configs", SYS_PROP_SERVER_PORT);
            Config serverConfig = appConfig.getConfig(runtime.getName().toLowerCase());
            if (runtime == ServerRuntime.UNDERTOW) {
                port = serverConfig.getInt("http.port");
            } else {
                port = serverConfig.getInt("port");
            }
            if (port > 0) {
                this.logger.info("Resolved port from server({}) configs!", runtime.getName());
            }
            if (port == 0) {
                // Fallback
                port = appConfig.getInt("kernel.server.port");
                this.logger.info("Resolved port from kernel configs!");
            }
        }
        this.logger.info("Starting {} on port: {}", runtime.getName(), port);
        return port;
    }
}
