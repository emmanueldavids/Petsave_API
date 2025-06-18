package com.petsave.petsave.Service;

import java.util.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
public class FusionAuthService {

    @Value("${fusionauth.apiKey}")
    private String apiKey;

    @Value("${fusionauth.baseUrl}")
    private String baseUrl;

    private final WebClient webClient = WebClient.create();

    public boolean registerUser(String email, String password) {
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        user.put("password", password);

        Map<String, Object> registration = new HashMap<>();
        registration.put("applicationId", "7cf53ad1-ae30-4aa8-8877-8d6169029545");

        Map<String, Object> request = new HashMap<>();
        request.put("user", user);
        request.put("registration", registration);

        try {
            Map response = webClient.post()
                    .uri(baseUrl + "/api/user/register")
                    .header("Authorization", apiKey)
                    .bodyValue(request)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();

            return response != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Map<String, Object> loginUser(String email, String password) {
        Map<String, Object> requestBody = Map.of(
            "loginId", email,
            "password", password
        );

        try {
            return webClient.post()
                    .uri(baseUrl + "/api/login")
                    .header("Authorization", apiKey)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (WebClientResponseException e) {
            e.printStackTrace();
            return Map.of("error", e.getResponseBodyAsString());
        }
    }
    public Map<String, Object> getUserByEmail(String email) {
        try {
            return webClient.get()
                    .uri(baseUrl + "/api/user?email=" + email)
                    .header("Authorization", apiKey)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block();
        } catch (Exception e) {
            e.printStackTrace();
            return Collections.emptyMap();
        }
    }
    // public boolean deleteUser(String userId) {
    //     try {
    //         webClient.delete()
    //                 .uri(baseUrl + "/api/user/" + userId)
    //                 .header("Authorization", apiKey)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    // public boolean updateUser(String userId, Map<String, Object> userUpdates) {
    //     try {
    //         webClient.put()
    //                 .uri(baseUrl + "/api/user/" + userId)
    //                 .header("Authorization", apiKey)
    //                 .bodyValue(userUpdates)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    // public Map<String, Object> getUserById(String userId) {
    //     try {
    //         return webClient.get()
    //                 .uri(baseUrl + "/api/user/" + userId)
    //                 .header("Authorization", apiKey)
    //                 .retrieve()
    //                 .bodyToMono(Map.class)
    //                 .block();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Collections.emptyMap();
    //     }
    // }
    // public List<Map<String, Object>> getAllUsers() {
    //     try {
    //         return webClient.get()
    //                 .uri(baseUrl + "/api/user")
    //                 .header("Authorization", apiKey)
    //                 .retrieve()
    //                 .bodyToFlux(new org.springframework.core.ParameterizedTypeReference<Map<String, Object>>() {})
    //                 .collectList()
    //                 .block();
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return Collections.emptyList();
    //     }
    // }
    // public boolean changePassword(String userId, String newPassword) {
    //     Map<String, Object> requestBody = new HashMap<>();
    //     requestBody.put("password", newPassword);

    //     try {
    //         webClient.put()
    //                 .uri(baseUrl + "/api/user/" + userId + "/password")
    //                 .header("Authorization", apiKey)
    //                 .bodyValue(requestBody)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    // public boolean resetPassword(String email) {
    //     Map<String, Object> requestBody = new HashMap<>();
    //     requestBody.put("email", email);

    //     try {
    //         webClient.post()
    //                 .uri(baseUrl + "/api/user/reset-password")
    //                 .header("Authorization", apiKey)
    //                 .bodyValue(requestBody)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    // public boolean verifyEmail(String userId, String verificationCode) {
    //     Map<String, Object> requestBody = new HashMap<>();
    //     requestBody.put("verificationCode", verificationCode);

    //     try {
    //         webClient.post()
    //                 .uri(baseUrl + "/api/user/" + userId + "/verify-email")
    //                 .header("Authorization", apiKey)
    //                 .bodyValue(requestBody)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    // public boolean resendVerificationEmail(String userId) {
    //     try {
    //         webClient.post()
    //                 .uri(baseUrl + "/api/user/" + userId + "/resend-verification")
    //                 .header("Authorization", apiKey)
    //                 .retrieve()
    //                 .bodyToMono(Void.class)
    //                 .block();
    //         return true;
    //     } catch (Exception e) {
    //         e.printStackTrace();
    //         return false;
    //     }
    // }
    public boolean logoutUser(String userId) {
        try {
            webClient.post()
                    .uri(baseUrl + "/api/user/" + userId + "/logout")
                    .header("Authorization", apiKey)
                    .retrieve()
                    .bodyToMono(Void.class)
                    .block();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
