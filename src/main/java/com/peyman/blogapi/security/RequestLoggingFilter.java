package com.peyman.blogapi.security;

import io.jsonwebtoken.io.IOException;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Enumeration;

//public class RequestLoggingFilter{}
//
@Component
@Order(1) // Ensures this filter runs early
public class RequestLoggingFilter implements Filter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException, java.io.IOException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        long startTime = System.currentTimeMillis();
        chain.doFilter(request, response);
        long duration = System.currentTimeMillis() - startTime;

        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();

        // Collect headers
        Enumeration<String> headerNames = request.getHeaderNames();
        StringBuilder headers = new StringBuilder();
        while (headerNames.hasMoreElements()) {
            String header = headerNames.nextElement();
            headers.append(header).append(": ").append(request.getHeader(header)).append("; ");
        }

        String queryString = request.getQueryString();

        // Final logging
        log.info(">> {} {} [{}] - {} ms", method, uri, status, duration);
        log.info("Headers: {} | Query: {}", headers.toString(), queryString != null ? queryString : "none");
    }
}

