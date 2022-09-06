package com.adeptj.runtime.kernel;

public final class ServerShutdownHook extends Thread {

    private final Server server;

    public ServerShutdownHook(Server server, String threadName) {
        super(threadName);
        this.server = server;
    }

    /**
     * Calls the {@link Server#stop()} in run method.
     */
    @Override
    public void run() {
        this.server.stop();
    }
}
