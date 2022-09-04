package com.adeptj.runtime.tomcat;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;

public class FallbackResourceHandler {

    public void handle(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        URL resource = this.getClass().getResource("/WEB-INF" + req.getRequestURI());
        if (resource == null) {
            resp.sendError(SC_NOT_FOUND);
            return;
        }
        this.doHandle(req, resp, resource, req.getPathInfo());
    }

    // Below section is borrowed (with much appreciation) from Apache Felix HttpService ResourceServlet :)

    private void doHandle(HttpServletRequest req, HttpServletResponse res, URL url, String resName) throws IOException {
        String contentType = req.getServletContext().getMimeType(resName);
        if (contentType != null) {
            res.setContentType(contentType);
        }
        long lastModified = this.getLastModified(url);
        if (lastModified != 0) {
            res.setDateHeader("Last-Modified", lastModified);
        }
        if (!resourceModified(lastModified, req.getDateHeader("If-Modified-Since"))) {
            res.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
        } else {
            this.copyResource(url, res);
        }
    }

    private long getLastModified(URL url) {
        long lastModified = 0;
        try {
            URLConnection conn = url.openConnection();
            lastModified = conn.getLastModified();
        } catch (Exception e) {
            // Do nothing
        }
        if (lastModified == 0) {
            String filepath = url.getPath();
            if (filepath != null) {
                File f = new File(filepath);
                if (f.exists()) {
                    lastModified = f.lastModified();
                }
            }
        }

        return lastModified;
    }

    private boolean resourceModified(long resTimestamp, long modSince) {
        modSince /= 1000;
        resTimestamp /= 1000;
        return resTimestamp == 0 || modSince == -1 || resTimestamp > modSince;
    }

    private void copyResource(URL url, HttpServletResponse res) throws IOException {
        URLConnection conn;
        OutputStream os = null;
        InputStream is = null;
        try {
            conn = url.openConnection();
            is = conn.getInputStream();
            os = res.getOutputStream();
            int len = this.getContentLength(conn);
            if (len >= 0) {
                res.setContentLength(len);
            }
            byte[] buf = new byte[1024];
            int n;
            while ((n = is.read(buf, 0, buf.length)) >= 0) {
                os.write(buf, 0, n);
            }
        } finally {
            if (is != null) {
                is.close();
            }
            if (os != null) {
                os.close();
            }
        }
    }

    private int getContentLength(URLConnection conn) {
        int length;
        length = conn.getContentLength();
        if (length < 0) {
            // Unknown, try whether it is a file, and if so, use the file
            // API to get the length of the content...
            String path = conn.getURL().getPath();
            if (path != null) {
                File f = new File(path);
                // In case more than 2GB is streamed
                if (f.length() < Integer.MAX_VALUE) {
                    length = (int) f.length();
                }
            }
        }
        return length;
    }
}
