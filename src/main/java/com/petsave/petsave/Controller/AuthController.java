package com.petsave.petsave.Controller;

import com.petsave.petsave.dto.LoginRequest;
import com.petsave.petsave.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final RestTemplate restTemplate;

    @Value("${fusionauth.api.key}")
    private String fusionAuthApiKey;

    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@RequestBody RegisterRequest request) {
        String fusionAuthUrl = "http://localhost:9011/api/user/registration";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", fusionAuthApiKey);

        // Replace with your actual FusionAuth Application ID
        String applicationId = "5ac489db-e94a-4abf-babd-08ebc850a6b0";

        Map<String, Object> user = new HashMap<>();
        user.put("email", request.getEmail());
        user.put("password", request.getPassword());
        user.put("firstName", request.getFirstName());
        user.put("lastName", request.getLastName());

        Map<String, Object> registration = new HashMap<>();
        registration.put("applicationId", applicationId);

        Map<String, Object> payload = new HashMap<>();
        payload.put("user", user);
        payload.put("registration", registration);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

        try {
            ResponseEntity<String> response = restTemplate.postForEntity(fusionAuthUrl, entity, String.class);
            return ResponseEntity.status(response.getStatusCode()).body("User registered: " + response.getBody());
        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("Failed to register user with FusionAuth: " + e.getResponseBodyAsString());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        String fusionAuthLoginUrl = "http://localhost:9011/api/login";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", fusionAuthApiKey); // Use injected key

        Map<String, Object> body = new HashMap<>();
        body.put("loginId", request.getEmail());
        body.put("password", request.getPassword());
        body.put("applicationId", "5ac489db-e94a-4abf-babd-08ebc850a6b0");

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(fusionAuthLoginUrl, entity, Map.class);
            Map<String, Object> responseBody = response.getBody();

            if (responseBody != null && responseBody.containsKey("user")) {
                Map<String, Object> user = (Map<String, Object>) responseBody.get("user");
                Boolean verified = (Boolean) user.get("verified");

                if (Boolean.FALSE.equals(verified)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                            .body("Email not verified. Please check your inbox to verify before logging in.");
                }

                return ResponseEntity.ok(responseBody); // Login success
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Login failed: Invalid response.");
            }

        } catch (HttpClientErrorException e) {
            return ResponseEntity.status(e.getStatusCode())
                    .body("Login failed: " + e.getResponseBodyAsString());
        }
    }

    @GetMapping("/users")
public ResponseEntity<?> getAllUsers() {
    String url = "http://localhost:9011/api/user/search";

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", fusionAuthApiKey);

    Map<String, Object> search = new HashMap<>();
    search.put("numberOfResults", 100);
    search.put("queryString", "*"); // wildcard fetch

    Map<String, Object> searchRequest = new HashMap<>();
    searchRequest.put("search", search);

    HttpEntity<Map<String, Object>> entity = new HttpEntity<>(searchRequest, headers);

    try {
        ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);
        return ResponseEntity.ok(response.getBody());
    } catch (HttpClientErrorException e) {
        return ResponseEntity.status(e.getStatusCode())
                .body("Failed to fetch users: " + e.getResponseBodyAsString());
    }
}


    @GetMapping("/users/{userId}")
    public ResponseEntity<?> getUserById(@PathVariable String userId) {
        String url = "http://localhost:9011/api/user/" + userId;

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", fusionAuthApiKey);
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
            return ResponseEntity.ok(response.getBody());
        } catch (HttpClientErrorException e) {
            String error = e.getResponseBodyAsString();
            return ResponseEntity.status(e.getStatusCode())
                    .body("Failed to fetch user: " + (error.isEmpty() ? e.getMessage() : error));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unexpected error occurred: " + ex.getMessage());
        }
    }



}
