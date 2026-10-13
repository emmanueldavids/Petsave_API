package com.petsave.petsave.Config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_REQUESTS = 30;
    private static final Duration WINDOW = Duration.ofMinutes(1);
    private static final Map<String, RequestWindow> REQUESTS = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/auth/") && !path.startsWith("/api/users/") && !path.startsWith("/api/admin/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = request.getRemoteAddr();
        String path = request.getRequestURI();
        String key = clientKey + ":" + path;
        RequestWindow window = REQUESTS.computeIfAbsent(key, ignored -> new RequestWindow());

        synchronized (window) {
            long now = System.currentTimeMillis();
            if (now - window.getStartMillis() > WINDOW.toMillis()) {
                window.reset(now);
            }

            if (window.getCount() >= MAX_REQUESTS) {
                response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.setHeader("Retry-After", String.valueOf(WINDOW.getSeconds()));
                response.getWriter().write("{\"message\":\"Too many requests. Please slow down and try again later.\",\"status\":\"error\"}");
                return;
            }

            window.increment();
        }

        filterChain.doFilter(request, response);
    }

    private static final class RequestWindow {
        private long startMillis;
        private int count;

        private RequestWindow() {
            this.startMillis = System.currentTimeMillis();
            this.count = 0;
        }

        private void reset(long now) {
            this.startMillis = now;
            this.count = 0;
        }

        private long getStartMillis() {
            return startMillis;
        }

        private int getCount() {
            return count;
        }

        private void increment() {
            count++;
        }
    }
}
