package com.rackspace.papi.commons.util.servlet.http;

import java.util.Enumeration;
import java.util.Map;

public interface RequestQueryParameters {

    String getParameter(String name);

    Map<String, String[]> getParameterMap();

    Enumeration<String> getParameterNames();

    String[] getParameterValues(String name);

    String getQueryString();

    void setQueryString(String query);
}
