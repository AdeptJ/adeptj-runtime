package com.adeptj.runtime.jetty.handler;

import com.adeptj.runtime.kernel.ConfigProvider;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

import static org.eclipse.jetty.http.HttpStatus.FOUND_302;

public class ContextPathHandler extends Handler.Abstract {

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        String requestPath = request.getHttpURI().getPath();
        if (requestPath.equals("/") || requestPath.startsWith("/;jsessionid")) {
            response.setStatus(FOUND_302);
            String systemConsolePath = ConfigProvider.getInstance()
                    .getMainConfig()
                    .getString("common.system-console-path");
            response.getHeaders().add(HttpHeader.LOCATION.toString(), systemConsolePath);
            callback.succeeded();
            return true;
        }
        return false;
    }
}
