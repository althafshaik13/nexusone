package dev.nexusone.gateway.ratelimit;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final StringRedisTemplate redisTemplate;
    private final long windowSeconds;
    private final long maxRequests;
    private final Counter rejectedCounter;

    public RateLimitFilter(StringRedisTemplate redisTemplate,
                            MeterRegistry meterRegistry,
                            @Value("${nexusone.ratelimit.window-seconds}") long windowSeconds,
                            @Value("${nexusone.ratelimit.max-requests}") long maxRequests) {
        this.redisTemplate = redisTemplate;
        this.windowSeconds = windowSeconds;
        this.maxRequests = maxRequests;
        this.rejectedCounter = Counter.builder("gateway.ratelimit.rejected")
                .description("Requests rejected by the gateway rate limiter")
                .register(meterRegistry);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod()) || request.getRequestURI().startsWith("/actuator/")) {
            chain.doFilter(request, response);
            return;
        }

        String key = resolveKey(request);
        long windowStart = System.currentTimeMillis() / 1000 / windowSeconds;
        String redisKey = "ratelimit:" + key + ":" + windowStart;

        Long count = redisTemplate.opsForValue().increment(redisKey);
        if (count != null && count == 1L) {
            redisTemplate.expire(redisKey, Duration.ofSeconds(windowSeconds));
        }

        if (count != null && count > maxRequests) {
            rejectedCounter.increment();
            long secondsIntoWindow = (System.currentTimeMillis() / 1000) % windowSeconds;
            long retryAfter = windowSeconds - secondsIntoWindow;
            response.setStatus(429);
            response.setHeader("Retry-After", String.valueOf(retryAfter));
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Too Many Requests\",\"retryAfterSeconds\":" + retryAfter + "}");
            return;
        }

        chain.doFilter(request, response);
    }

    private String resolveKey(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String subject = extractSubjectUnverified(authHeader.substring("Bearer ".length()));
            if (subject != null) {
                return "user:" + subject;
            }
        }
        return "ip:" + request.getRemoteAddr();
    }

    // Rate-limit bucketing only, not an authorization decision — the gateway
    // deliberately doesn't validate JWTs (see milestone 15); ticket-service
    // does that independently. A forged subject here just buys an attacker a
    // separate rate-limit bucket, not access to anything.
    private String extractSubjectUnverified(String jwt) {
        try {
            String[] parts = jwt.split("\\.");
            if (parts.length < 2) {
                return null;
            }
            byte[] decoded = Base64.getUrlDecoder().decode(parts[1]);
            String payload = new String(decoded, StandardCharsets.UTF_8);
            int subIndex = payload.indexOf("\"sub\"");
            if (subIndex < 0) {
                return null;
            }
            int colonIndex = payload.indexOf(':', subIndex);
            int firstQuote = payload.indexOf('"', colonIndex + 1);
            int secondQuote = payload.indexOf('"', firstQuote + 1);
            if (colonIndex < 0 || firstQuote < 0 || secondQuote < 0) {
                return null;
            }
            return payload.substring(firstQuote + 1, secondQuote);
        } catch (Exception e) {
            return null;
        }
    }
}
