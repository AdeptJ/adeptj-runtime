package com.adeptj.runtime.kernel;

import jakarta.servlet.Filter;

public class FilterInfo {

    private final String filterName;

    private final String pattern;

    private Class<? extends Filter> filterClass;

    private Filter filterInstance;

    public FilterInfo(String filterName, String pattern) {
        this.filterName = filterName;
        this.pattern = pattern;
    }

    public String getFilterName() {
        return filterName;
    }

    public String getPattern() {
        return pattern;
    }

    public Class<? extends Filter> getFilterClass() {
        return filterClass;
    }

    public void setFilterClass(Class<? extends Filter> filterClass) {
        this.filterClass = filterClass;
    }

    public Filter getFilterInstance() {
        return filterInstance;
    }

    public void setFilterInstance(Filter filterInstance) {
        this.filterInstance = filterInstance;
    }
}
