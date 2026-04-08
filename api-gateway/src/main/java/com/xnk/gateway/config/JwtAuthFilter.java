package com.xnk.gateway.config;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.Key;
import java.util.List;

@Component
@Order(-1)
public class JwtAuthFilter implements GlobalFilter {

    private static final Logger log = LoggerFactory.getLogger(JwtAuthFilter.class);

    // Các path KHÔNG cần xác thực
    private static final List<String> PUBLIC_PATHS = List.of(
        "/api/auth/login",
        "/actuator"
    );

    @Value("${jwt.secret}")
    private String secret;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();

        // Bỏ qua public paths
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return chain.filter(exchange);
        }

        // Lấy Authorization header
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("[GATEWAY] Missing token: {}", path);
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }

        String token = authHeader.substring(7);
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // Forward thông tin user xuống downstream service qua header
            ServerHttpRequest mutated = exchange.getRequest().mutate()
                    .header("X-Username", claims.getSubject())
                    .header("X-Role",     claims.get("role", String.class))
                    .header("X-Agent-Id", claims.get("agentId") != null
                            ? claims.get("agentId").toString() : "")
                    .build();

            log.info("[GATEWAY] Authenticated: {} role={} path={}", claims.getSubject(),
                     claims.get("role"), path);
            return chain.filter(exchange.mutate().request(mutated).build());

        } catch (JwtException e) {
            log.warn("[GATEWAY] Invalid token: {} — {}", path, e.getMessage());
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }

    private Key getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }
}
