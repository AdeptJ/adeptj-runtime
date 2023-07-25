package com.adeptj.runtime.kernel;

import com.adeptj.runtime.kernel.security.MVStoreUserManager;
import com.adeptj.runtime.kernel.util.Times;
import com.typesafe.config.Config;
import org.slf4j.Logger;

import java.util.List;
import java.util.ResourceBundle;

import static com.adeptj.runtime.kernel.Constants.SYS_PROP_SERVER_PORT;

public abstract class AbstractServer implements Server {

    private final UserManager userManager;

    private ServerPostStopTask postStopTask;

    public AbstractServer() {
        this.userManager = new MVStoreUserManager();
    }

    public void setServerPostStopTask(ServerPostStopTask postStopTask) {
        this.postStopTask = postStopTask;
    }

    protected abstract Logger getLogger();

    @Override
    public void stop() {
        long startTime = System.nanoTime();
        this.getLogger().info("Stopping AdeptJ Runtime!!");
        try {
            this.doStop();
            this.getLogger().info("AdeptJ Runtime stopped in [{}] ms!!", Times.elapsedMillis(startTime));
        } catch (Throwable ex) { // NOSONAR
            this.getLogger().error("Exception while stopping AdeptJ Runtime!!", ex);
        } finally {
            this.postStopTask.execute();
        }
    }

    protected abstract void doStop() throws Exception;

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
        ResourceBundle rb = ResourceBundle.getBundle("i18n/kernel");
        ServerRuntime runtime = this.getRuntime();
        // First check - system property.
        Integer port = Integer.getInteger("adeptj.rt.port");
        if (port == null || port == 0) {
            this.getLogger().info(rb.getString("ajrt.port.system.property.not.specified"), SYS_PROP_SERVER_PORT);
            // Second check - server configs.
            port = appConfig.getConfig(runtime.getLowerCaseName()).getInt("http.port");
            if (port > 0) {
                this.getLogger().info("Resolved port from server({}) configs!", runtime);
            }
            if (port == 0) {
                // Third check - kernel configs.
                port = appConfig.getInt("kernel.server.port");
                this.getLogger().info("Resolved port from kernel configs!");
            }
        }
        this.getLogger().info("Starting {} on port: {}", runtime, port);
        return port;
    }
}
