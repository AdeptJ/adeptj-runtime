/*
###############################################################################
#                                                                             #
#    Copyright 2016, AdeptJ (http://adeptj.com)                               #
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
package com.adeptj.runtime.config;

/**
 * ViewEngineConfig bean initialized by Typesafe config.
 *
 * @author Rakesh.Kumar, AdeptJ.
 */
public class ViewEngineConfig {

    private int templateLocatorPriority;

    private int cacheExpiration;

    private boolean cacheEnabled;

    private String prefix;

    private String suffix;

    private String startDelimiter;

    private String endDelimiter;

    private String resourceBundleBasename;

    public int getTemplateLocatorPriority() {
        return templateLocatorPriority;
    }

    public void setTemplateLocatorPriority(int templateLocatorPriority) {
        this.templateLocatorPriority = templateLocatorPriority;
    }

    public int getCacheExpiration() {
        return cacheExpiration;
    }

    public void setCacheExpiration(int cacheExpiration) {
        this.cacheExpiration = cacheExpiration;
    }

    public boolean isCacheEnabled() {
        return cacheEnabled;
    }

    public void setCacheEnabled(boolean cacheEnabled) {
        this.cacheEnabled = cacheEnabled;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public String getStartDelimiter() {
        return startDelimiter;
    }

    public void setStartDelimiter(String startDelimiter) {
        this.startDelimiter = startDelimiter;
    }

    public String getEndDelimiter() {
        return endDelimiter;
    }

    public void setEndDelimiter(String endDelimiter) {
        this.endDelimiter = endDelimiter;
    }

    public String getResourceBundleBasename() {
        return resourceBundleBasename;
    }

    public void setResourceBundleBasename(String resourceBundleBasename) {
        this.resourceBundleBasename = resourceBundleBasename;
    }
}