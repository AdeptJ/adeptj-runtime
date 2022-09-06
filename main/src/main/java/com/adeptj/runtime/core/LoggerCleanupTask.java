package com.adeptj.runtime.core;

import com.adeptj.runtime.common.LogbackManagerHolder;
import com.adeptj.runtime.kernel.ServerPostStopTask;

public class LoggerCleanupTask implements ServerPostStopTask {

    @Override
    public void execute() {
        LogbackManagerHolder.getInstance().getLogbackManager().cleanup();
    }
}
