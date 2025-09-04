// package com.teamsync.message_management_service.client;

// import com.teamsync.message_management_service.dto.UserResponseDTO;
// import org.springframework.stereotype.Component;

// import java.time.LocalDate;
// import java.util.HashMap;
// import java.util.Map;
// import java.util.Optional;

// @Component
// public class UserClient {
    
//     // Dummy data storage
//     private static final Map<Long, UserResponseDTO> users = new HashMap<>();
//     private static final String COMMON_EMAIL = "a@b.com";
    
//     static {
//         // Initialize dummy users - all with same email as used in service
//         users.put(1L, UserResponseDTO.builder()
//                 .id(1L)
//                 .name("John Doe")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/john.jpg")
//                 .designation("Software Engineer")
//                 .birthdate(LocalDate.of(1990, 5, 15))
//                 .joinDate(LocalDate.of(2020, 1, 10))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(2L, UserResponseDTO.builder()
//                 .id(2L)
//                 .name("Jane Smith")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/jane.jpg")
//                 .designation("Project Manager")
//                 .birthdate(LocalDate.of(1988, 8, 22))
//                 .joinDate(LocalDate.of(2019, 3, 15))
//                 .predictedBurnoutRisk(true)
//                 .build());
                
//         users.put(3L, UserResponseDTO.builder()
//                 .id(3L)
//                 .name("Mike Johnson")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/mike.jpg")
//                 .designation("DevOps Engineer")
//                 .birthdate(LocalDate.of(1992, 12, 5))
//                 .joinDate(LocalDate.of(2021, 6, 20))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(4L, UserResponseDTO.builder()
//                 .id(4L)
//                 .name("Sarah Wilson")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/sarah.jpg")
//                 .designation("UI/UX Designer")
//                 .birthdate(LocalDate.of(1985, 3, 18))
//                 .joinDate(LocalDate.of(2018, 9, 1))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(5L, UserResponseDTO.builder()
//                 .id(5L)
//                 .name("David Brown")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/david.jpg")
//                 .designation("Backend Developer")
//                 .birthdate(LocalDate.of(1993, 7, 30))
//                 .joinDate(LocalDate.of(2022, 2, 14))
//                 .predictedBurnoutRisk(true)
//                 .build());
                
//         users.put(6L, UserResponseDTO.builder()
//                 .id(6L)
//                 .name("Emily Davis")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/emily.jpg")
//                 .designation("QA Engineer")
//                 .birthdate(LocalDate.of(1991, 11, 12))
//                 .joinDate(LocalDate.of(2020, 8, 25))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(7L, UserResponseDTO.builder()
//                 .id(7L)
//                 .name("Robert Taylor")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/robert.jpg")
//                 .designation("Frontend Developer")
//                 .birthdate(LocalDate.of(1989, 4, 8))
//                 .joinDate(LocalDate.of(2019, 11, 30))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(8L, UserResponseDTO.builder()
//                 .id(8L)
//                 .name("Lisa Anderson")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/lisa.jpg")
//                 .designation("Data Analyst")
//                 .birthdate(LocalDate.of(1987, 9, 25))
//                 .joinDate(LocalDate.of(2017, 5, 12))
//                 .predictedBurnoutRisk(true)
//                 .build());
                
//         users.put(9L, UserResponseDTO.builder()
//                 .id(9L)
//                 .name("Chris Martinez")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/chris.jpg")
//                 .designation("System Administrator")
//                 .birthdate(LocalDate.of(1994, 6, 14))
//                 .joinDate(LocalDate.of(2023, 1, 8))
//                 .predictedBurnoutRisk(false)
//                 .build());
                
//         users.put(10L, UserResponseDTO.builder()
//                 .id(10L)
//                 .name("Amanda Garcia")
//                 .email(COMMON_EMAIL)
//                 .profilePicture("https://example.com/profiles/amanda.jpg")
//                 .designation("Product Owner")
//                 .birthdate(LocalDate.of(1986, 1, 20))
//                 .joinDate(LocalDate.of(2016, 12, 5))
//                 .predictedBurnoutRisk(false)
//                 .build());
//     }
    
//     // Method used in ChannelService.createChannel()
//     public boolean existsById(Long id) {
//         return users.containsKey(id);
//     }
    
//     // Method used in ChannelService.updateChannel()
//     public Optional<UserResponseDTO> findById(Long id) {
//         return Optional.ofNullable(users.get(id));
//     }
    
//     // Method used in MessageService.createChannelMessage()
//     public UserResponseDTO findByEmail(String email) {
//         return users.values().stream()
//                 .filter(user -> user.getEmail().equals(email))
//                 .findFirst()
//                 .orElse(null);
//     }
    
//     // Method used in MessageService.createMessageWithFiles()
//     public UserResponseDTO getCurrentUser() {
//         // Return the first user as current user (dummy implementation)
//         return users.get(1L);
//     }
// }



package com.teamsync.message_management_service.client;

import com.teamsync.message_management_service.dto.UserResponseDTO;
import com.teamsync.message_management_service.response.SuccessResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@FeignClient(name = "user-service", url = "${user-service.url}")
public interface UserClient {
    
    @GetMapping("/users/email/{email}")
    SuccessResponse<UserResponseDTO> findByEmail(@PathVariable("email") String userEmail);
    
    @GetMapping("/users/{userId}")
    SuccessResponse<UserResponseDTO> findById(@PathVariable("userId") Long userId);
    
    @GetMapping("/users/email/{email}")
    SuccessResponse<UserResponseDTO> authenticateByEmail(@PathVariable("email") String userEmail);
    
    @GetMapping("/users/email/{email}")
    SuccessResponse<UserResponseDTO> getUserByEmail(@PathVariable("email") String userEmail);
    
    @GetMapping("/users/exists/{userId}")
    SuccessResponse<Boolean> existsById(@PathVariable("userId") Long userId);
    
    @GetMapping("/users/current")
    SuccessResponse<UserResponseDTO> getCurrentUser();
    
}
