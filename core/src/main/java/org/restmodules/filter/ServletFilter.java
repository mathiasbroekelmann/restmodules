package org.restmodules.filter;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

interface ServletFilter {
    void init(ServletContext context) throws ServletException;

    void doFilter(ServletRequest request, ServletResponse response, ServletFilterChain chain) throws IOException,
        ServletException;

    void destroy();
}