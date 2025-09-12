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
                .route(RequestPredicates.path("/users/**"), HandlerFunctions.http("http://user-management-service:8082"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> projectManagementService() {
        return GatewayRouterFunctions.route("project_management_service")
                .route(RequestPredicates.path("/projects/**"), HandlerFunctions.http("http://project-management-service:8083"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> taskManagementService() {
        return GatewayRouterFunctions.route("task_management_service")
                .route(RequestPredicates.path("/tasks/**"), HandlerFunctions.http("http://task-management-service:8089"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> feedManagementService() {
        return GatewayRouterFunctions.route("feed_management_service")
                .route(RequestPredicates.path("/feedposts/**"), HandlerFunctions.http("http://feed-management-service:8090"))
                .route(RequestPredicates.path("/events/**"), HandlerFunctions.http("http://feed-management-service:8090"))
                .route(RequestPredicates.path("/pollvotes/**"), HandlerFunctions.http("http://feed-management-service:8090"))
                .route(RequestPredicates.path("/appreciations/**"), HandlerFunctions.http("http://feed-management-service:8090"))
                .build();
    }

    @Bean
    public RouterFunction<ServerResponse> messageManagementService() {
        return GatewayRouterFunctions.route("message_management_service")
                .route(RequestPredicates.path("/channels/**"), HandlerFunctions.http("http://message-management-service:8091"))
                .build();
    }
    
    @Bean
    public RouterFunction<ServerResponse> notificationService() {
        return GatewayRouterFunctions.route("notification_service")
                .route(RequestPredicates.path("/notifications/**"), HandlerFunctions.http("http://notification-service:8092"))
                .build();
    }
    
}
