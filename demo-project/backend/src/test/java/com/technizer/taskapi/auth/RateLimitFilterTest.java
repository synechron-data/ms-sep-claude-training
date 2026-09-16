package com.technizer.taskapi.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.PrintWriter;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RateLimitFilterTest {

    private RateLimitFilter filter;
    private FilterChain chain;

    @BeforeEach
    void setUp() {
        filter = new RateLimitFilter();
        chain = mock(FilterChain.class);
    }

    @Test
    void requestsUnderLimitPassThrough() throws Exception {
        HttpServletRequest request = authRequest("1.1.1.1");
        HttpServletResponse response = mock(HttpServletResponse.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, times(5)).doFilter(request, response);
    }

    @Test
    void sixthRequestWithinWindowIsRejected() throws Exception {
        HttpServletRequest request = authRequest("2.2.2.2");

        for (int i = 0; i < 5; i++) {
            filter.doFilter(request, mock(HttpServletResponse.class), chain);
        }
        verify(chain, times(5)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));

        HttpServletResponse sixthResponse = mock(HttpServletResponse.class);
        PrintWriter writer = mock(PrintWriter.class);
        when(sixthResponse.getWriter()).thenReturn(writer);

        filter.doFilter(request, sixthResponse, chain);

        verify(sixthResponse).setStatus(429);
        verify(chain, never()).doFilter(request, sixthResponse);
        verify(chain, times(5)).doFilter(any(HttpServletRequest.class), any(HttpServletResponse.class));
    }

    @Test
    void nonAuthPathsAlwaysPassThroughRegardlessOfCount() throws Exception {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/tasks");
        when(request.getRemoteAddr()).thenReturn("3.3.3.3");
        HttpServletResponse response = mock(HttpServletResponse.class);

        for (int i = 0; i < 10; i++) {
            filter.doFilter(request, response, chain);
        }

        verify(chain, times(10)).doFilter(request, response);
    }

    @Test
    void differentIpsGetIndependentBuckets() throws Exception {
        HttpServletRequest requestA = authRequest("4.4.4.4");
        HttpServletRequest requestB = authRequest("5.5.5.5");
        HttpServletResponse response = mock(HttpServletResponse.class);

        for (int i = 0; i < 5; i++) {
            filter.doFilter(requestA, response, chain);
        }
        filter.doFilter(requestB, response, chain);

        verify(chain, times(5)).doFilter(requestA, response);
        verify(chain, times(1)).doFilter(requestB, response);
    }

    private HttpServletRequest authRequest(String ip) {
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getRequestURI()).thenReturn("/api/auth/login");
        when(request.getRemoteAddr()).thenReturn(ip);
        return request;
    }
}
