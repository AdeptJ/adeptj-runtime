package com.adeptj.runtime.jetty;

import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import static java.lang.System.Logger.Level.INFO;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public class TimingHttpChannelListener implements HttpChannel.Listener {

    private final ConcurrentMap<Request, Long> times = new ConcurrentHashMap<>();

    @Override
    public void onRequestBegin(Request request) {
        times.put(request, System.nanoTime());
    }

    @Override
    public void onComplete(Request request) {
        long begin = times.remove(request);
        long elapsed = NANOSECONDS.toMillis(System.nanoTime() - begin);
        System.getLogger("timing").log(INFO, "Request {0} took {1} ms", request, elapsed);
    }
}
