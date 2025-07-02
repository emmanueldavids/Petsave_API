// package com.petsave.petsave.Service;

// import com.fasterxml.jackson.databind.ObjectMapper;
// import com.petsave.petsave.dto.RegisterRequest;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
// import org.springframework.http.*;
// import org.springframework.stereotype.Service;
// import org.springframework.web.client.RestTemplate;

// import java.util.*;

// @Slf4j
// @Service
// @RequiredArgsConstructor
// public class FusionAuthClient {

//     private final RestTemplate restTemplate = new RestTemplate();
//     private final ObjectMapper objectMapper = new ObjectMapper();

//     private static final String BASE_URL = "http://localhost:9011/api";
//     private static final String API_KEY = "iCkS5LF6DfxHknZT8hw0Sni6jo-GPf4A8ghcJLsgweaeGQJ2lmrliio9";
//     private static final String APPLICATION_ID = "5ac489db-e94a-4abf-babd-08ebc850a6b0"; // From FusionAuth dashboard

//     public boolean registerUser(RegisterRequest request) {
//         try {
//             Map<String, Object> user = new HashMap<>();
//             user.put("email", request.getEmail());
//             user.put("firstName", request.getFirstName());
//             user.put("lastName", request.getLastName());
//             user.put("password", request.getPassword());

//             Map<String, Object> registration = new HashMap<>();
//             registration.put("applicationId", APPLICATION_ID);

//             Map<String, Object> body = new HashMap<>();
//             body.put("user", user);
//             body.put("registration", registration);

//             HttpHeaders headers = new HttpHeaders();
//             headers.setContentType(MediaType.APPLICATION_JSON);
//             headers.set("Authorization", API_KEY);

//             HttpEntity<String> entity = new HttpEntity<>(objectMapper.writeValueAsString(body), headers);
//             ResponseEntity<String> response = restTemplate.exchange(BASE_URL + "/user/register", HttpMethod.POST, entity, String.class);

//             log.info("FusionAuth registration response: {}", response.getBody());
//             return response.getStatusCode() == HttpStatus.OK;

//         } catch (Exception e) {
//             log.error("FusionAuth registration failed", e);
//             return false;
//         }
//     }
// }
