package com.adeptj.runtime.kernel;

import com.adeptj.runtime.kernel.exception.RuntimeInitializationException;
import com.adeptj.runtime.kernel.util.ClasspathResource;
import com.adeptj.runtime.kernel.util.Environment;
import com.adeptj.runtime.kernel.util.IOUtils;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Enumeration;

import static com.adeptj.runtime.kernel.Constants.SYS_PROP_OVERWRITE_CONF_FILES;

public enum ConfigProvider {

    INSTANCE;

    private static final String APPLICATION_CONF = "application.conf";

    private static final String REFERENCE_CONF = "reference.conf";

    private static final String MAIN_CONF = "main.conf";

    private static final String JETTY_CONF = "jetty.conf";

    private static final String TOMCAT_CONF = "tomcat.conf";

    private static final String UNDERTOW_CONF = "undertow.conf";

    private static final String KERNEL_CONF = "kernel.conf";

    private static final String UNKNOWN_CONF = "unknown.conf";

    private static final String RT_JETTY = "adeptj-runtime-jetty";

    private static final String RT_TOMCAT = "adeptj-runtime-tomcat";

    private static final String RT_UNDERTOW = "adeptj-runtime-undertow";

    private static final String RT_KERNEL = "adeptj-runtime-kernel";

    private final Config root;

    ConfigProvider() {
        try {
            this.root = this.resolveConf();
        } catch (IOException e) {
            throw new RuntimeInitializationException(e);
        }
    }

    public Config getApplicationConfig() {
        return this.root;
    }

    public Config getMainConfig() {
        return this.getApplicationConfig().getConfig("main");
    }

    public Config getMainConfig(Config appConfig) {
        return appConfig.getConfig("main");
    }

    public Config getKernelConfig() {
        return this.getApplicationConfig().getConfig("kernel");
    }

    public Config getKernelConfig(Config appConfig) {
        return appConfig.getConfig("kernel");
    }

    public Config getServerConfig(ServerRuntime runtime) {
        return this.getServerConfig(runtime, this.getApplicationConfig());
    }

    public Config getServerConfig(ServerRuntime runtime, Config appConfig) {
        return appConfig.getConfig(runtime.getLowerCaseName());
    }

    public static ConfigProvider getInstance() {
        return INSTANCE;
    }

    private Config resolveConf() throws IOException {
        Path confDirPath = Environment.getConfDirPath();
        if (Files.exists(confDirPath)) {
            if (Boolean.getBoolean(SYS_PROP_OVERWRITE_CONF_FILES)) {
                this.createOrUpdateConfFiles(confDirPath);
            }
            return this.parseConfFiles(confDirPath);
        }
        this.createOrUpdateConfFiles(Files.createDirectories(confDirPath));
        return ConfigFactory.load();
    }

    private Config parseConfFiles(Path confDirPath) {
        File confDir = confDirPath.toFile();
        File[] files = confDir.listFiles();
        if (files == null) {
            String msg = String.format("No config files present in (%s) directory!!", confDir.getAbsolutePath());
            throw new IllegalStateException(msg);
        }
        Config config = null;
        for (File confFile : files) {
            if (config == null) {
                config = ConfigFactory.parseFile(confFile);
            } else {
                config = ConfigFactory.parseFile(confFile).withFallback(config);
            }
        }
        if (config == null) { // Failsafe, always load from classpath if previous steps resulted in null config.
            config = ConfigFactory.load();
            System.err.println("Config loaded from classpath because conf files from file system could not be parsed.");
        } else {
            config = config.withFallback(ConfigFactory.systemProperties()).resolve();
        }
        return config;
    }

    private void createOrUpdateConfFiles(Path confDir) throws IOException {
        URL resource = ClasspathResource.toUrl(APPLICATION_CONF, this.getClass().getClassLoader());
        if (resource != null) {
            this.writeConfFile(resource, confDir);
        }
        Enumeration<URL> resources = ClasspathResource.toUrls(REFERENCE_CONF, this.getClass().getClassLoader());
        while (resources.hasMoreElements()) {
            this.writeConfFile(resources.nextElement(), confDir);
        }
    }

    private void writeConfFile(URL url, Path confDir) throws IOException {
        try (InputStream stream = url.openStream()) {
            if (stream != null) {
                Files.write(this.getConfFilePath(url, confDir), IOUtils.toBytes(stream));
            }
        }
    }

    private Path getConfFilePath(URL confResource, Path confDir) {
        String confFileName;
        if (StringUtils.endsWith(confResource.getPath(), APPLICATION_CONF)) {
            confFileName = MAIN_CONF;
        } else if (StringUtils.contains(confResource.getPath(), RT_JETTY)) {
            confFileName = JETTY_CONF;
        } else if (StringUtils.contains(confResource.getPath(), RT_TOMCAT)) {
            confFileName = TOMCAT_CONF;
        } else if (StringUtils.contains(confResource.getPath(), RT_UNDERTOW)) {
            confFileName = UNDERTOW_CONF;
        } else if (StringUtils.contains(confResource.getPath(), RT_KERNEL)) {
            confFileName = KERNEL_CONF;
        } else {
            confFileName = UNKNOWN_CONF;
        }
        return confDir.resolve(confFileName);
    }

}
