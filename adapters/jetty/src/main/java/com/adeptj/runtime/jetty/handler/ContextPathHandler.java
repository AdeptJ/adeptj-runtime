package com.adeptj.runtime.jetty.handler;

import com.adeptj.runtime.kernel.ConfigProvider;
import com.adeptj.runtime.kernel.util.RequestUtil;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;

public class ContextPathHandler extends Handler.Abstract {

    @Override
    public boolean handle(Request request, Response response, Callback callback) {
        if (RequestUtil.isContextRootRequest(request.getHttpURI().getPath())) {
            String systemConsolePath = ConfigProvider.getInstance()
                    .getMainConfig()
                    .getString("common.system-console-path");
            Response.sendRedirect(request, response, callback, systemConsolePath);
            return true;
        }
        return false;
    }
}
