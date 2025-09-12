package com.teamsync.notificationservice.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", url = "${user-service.url:http://user-management-service:8082}")
public interface UserClient {

    @GetMapping("/users/email/{email}")
    UserResponse findByEmail(@PathVariable("email") String email);

    class UserResponse {
        public int code;
        public Object status;
        public String message;
        public Data data;

        public static class Data {
            public Long id;
            public String name;
            public String email;
        }
    }
}
