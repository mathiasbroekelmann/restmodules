package org.restmodules.filter;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

interface ServletFilterChain {
    void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException;
}