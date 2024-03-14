/*
###############################################################################
#                                                                             #
#    Copyright 2016-2024, AdeptJ (http://www.adeptj.com)                      #
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
package com.adeptj.runtime.kernel;

import java.util.ArrayList;
import java.util.List;

public class ServletDeployment {

    private final SciInfo sciInfo;

    // Servlet
    private final List<ServletInfo> servletInfos;

    // Filter
    private final List<FilterInfo> filterInfos;

    public ServletDeployment(SciInfo sciInfo) {
        this.sciInfo = sciInfo;
        this.servletInfos = new ArrayList<>();
        this.filterInfos = new ArrayList<>();
    }

    public SciInfo getSciInfo() {
        return sciInfo;
    }

    public List<ServletInfo> getServletInfos() {
        return servletInfos;
    }

    public ServletDeployment addServletInfo(ServletInfo info) {
        this.servletInfos.add(info);
        return this;
    }

    public List<FilterInfo> getFilterInfos() {
        return filterInfos;
    }

    public ServletDeployment addFilterInfo(FilterInfo info) {
        this.filterInfos.add(info);
        return this;
    }
}
