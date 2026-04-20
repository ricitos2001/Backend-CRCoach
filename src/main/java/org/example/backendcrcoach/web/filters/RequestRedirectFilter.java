package org.example.backendcrcoach.web.filters;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RequestRedirectFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(RequestRedirectFilter.class);
    private static final String REDIRECT_URL = "https://ricitos2001.github.io/Backend-CRCoach/";

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        String method = request.getMethod();
        String accept = request.getHeader("Accept");
        String xRequestedWith = request.getHeader("X-Requested-With");

        // Only consider redirects for GET requests
        if (!"GET".equalsIgnoreCase(method)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Allow programmatic/API consumers when they explicitly accept JSON or are XHR
        boolean acceptsJson = accept != null && (accept.contains("application/json") || accept.contains("application/*+json"));
        boolean isXhr = "XMLHttpRequest".equalsIgnoreCase(xRequestedWith);

        if (acceptsJson || isXhr) {
            // Let API or AJAX requests pass through
            filterChain.doFilter(request, response);
            return;
        }

        // If Accept header explicitly asks for HTML (typical browser navigation) or doesn't include JSON,
        // redirect to the public homepage to hide endpoint content from casual browsing.
        if (accept == null || accept.contains("text/html") || accept.contains("application/xhtml+xml") || accept.contains("*/*")) {
            String requestUri = request.getRequestURI();
            log.info("Redirecting browser request [{} {}] to {}", method, requestUri, REDIRECT_URL);
            response.sendRedirect(REDIRECT_URL);
            return;
        }

        filterChain.doFilter(request, response);
    }
}

