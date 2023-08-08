package com.adeptj.runtime.jetty.handler;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import static org.eclipse.jetty.http.HttpStatus.OK_200;

public class HealthCheckHandler extends Handler.Abstract {

    private static final String HC_URI = "/hc";

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (request.getHttpURI().getPath().equals(HC_URI)) {
            response.setStatus(OK_200);
            callback.succeeded();
            return true;
        }
        return false;
    }
}
