/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://www.adeptj.com)                           #
#                                                                             #
#    Licensed under the Apache License, Version 2.0 (the "License");          #
#    you may not use this file except in compliance with the License.         #
#    You may obtain a copy of the License at                                  #
#                                                                             #
#        http://www.apache.org/licenses/LICENSE-2.0                           #
#                                                                             #
#    Unless required by applicable law or agreed to in writing, software      #
#    distributed under the License is distributed on an "AS IS" BASIS,        #
#    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. #
#    See the License for the specific language governing permissions and      #
#    limitations under the License.                                           #
#                                                                             #
###############################################################################
*/

package com.adeptj.runtime.server;

import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.commons.io.input.TailerListenerAdapter;

import javax.websocket.Session;
import java.io.File;
import java.io.FileNotFoundException;

/**
 * Tails server log using Apache Commons IO {@link Tailer}.
 *
 * @author Rakesh.Kumar, AdeptJ
 */
class ServerLogsTailer {

    private static final int DELAY_MILLIS = 1000;

    private final File logFile;

    private final Session session;

    private Tailer tailer;

    ServerLogsTailer(File file, Session session) {
        this.logFile = file;
        this.session = session;
    }

    void startTailer() {
        this.tailer = new Tailer(this.logFile, this.createListener(), DELAY_MILLIS, true);
        ServerLogsExecutors.INSTANCE.execute(this.tailer);
    }

    void stopTailer() {
        this.tailer.stop();
    }

    private TailerListener createListener() {

        return new TailerListenerAdapter() {

            @Override
            public void fileRotated() {
                // ignore, just keep tailing
            }

            @Override
            public void handle(String line) {
                session.getAsyncRemote().sendText(line);
            }

            @Override
            public void fileNotFound() {
                session.getAsyncRemote().sendText(new FileNotFoundException(logFile.toString()).toString());
            }

            @Override
            public void handle(Exception ex) {
                session.getAsyncRemote().sendText(ex.toString());
            }
        };
    }
}
