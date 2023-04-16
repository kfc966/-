package com.example.gateway.filters;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URI;

@Component
@Slf4j
public class ElapsedTimeGlobalFilter implements GlobalFilter, Ordered {



    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        ServerHttpRequest request = exchange.getRequest();
        URI uri = request.getURI();
        log.info("Original request URI: " + uri);
        return chain.filter(exchange).then(
                Mono.fromRunnable(() -> {
                    URI routeUri = exchange.getAttribute(ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR);
                    log.info("Routed request URI: " + routeUri);
                    long endTime = System.currentTimeMillis();
                    long duration = endTime - startTime;
                    log.info("{} {} {} ({}) ms", exchange.getRequest().getMethod(),
                            exchange.getRequest().getURI().getPath(),
                            exchange.getResponse().getStatusCode(),
                            duration);})
        );
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
