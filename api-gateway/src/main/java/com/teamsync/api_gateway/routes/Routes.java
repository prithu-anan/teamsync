package com.teamsync.api_gateway.routes;

import org.springframework.cloud.gateway.server.mvc.handler.GatewayRouterFunctions;
import org.springframework.cloud.gateway.server.mvc.handler.HandlerFunctions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RequestPredicates;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.ServerResponse;

@Configuration
public class Routes {

    @Bean
    public RouterFunction<ServerResponse> testRoute() {
        return GatewayRouterFunctions.route()
                .route(RequestPredicates.GET("/health"),
                        request -> ServerResponse.ok().body("Gateway is alive!"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> authService() {
        return GatewayRouterFunctions.route("auth_service")
                .route(RequestPredicates.path("/auth/**"), HandlerFunctions.http("http://auth-service:8081"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> userManagementService() {
        return GatewayRouterFunctions.route("user_management_service")
                .route(RequestPredicates.path("/api/users/**"), HandlerFunctions.http("http://user-management-service:8082"))
                .build();
    }

}
