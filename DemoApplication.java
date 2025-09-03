package com.example.demo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class DemoApplication implements CommandLineRunner {

    private final RestTemplate restTemplate = new RestTemplate();

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Override
    public void run(String... args) {
        // STEP 1: Call generateWebhook API
        String generateUrl = "https://bfhldevapigw.healthrx.co.in/hiring/generateWebhook/JAVA";

        Map<String, String> requestBody = new HashMap<>();
        requestBody.put("name", "Uzma Farzana M");                     
        requestBody.put("regNo", "1RF22IS093");                        
        requestBody.put("email", "rvit22bis056.rvitm@rvei.edu.in");    

        String webhookUrl = null;
        String accessToken = null;

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(generateUrl, requestBody, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                webhookUrl = (String) response.getBody().get("webhook");
                accessToken = (String) response.getBody().get("accessToken");

                System.out.println("✅ Webhook: " + webhookUrl);
                System.out.println("✅ AccessToken: " + accessToken);
            } else {
                System.out.println("⚠️ Failed to generate webhook. Status: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("⚠️ API call failed: " + e.getMessage());
        }

        
        if (webhookUrl == null || accessToken == null) {
            System.out.println("❌ Cannot continue without webhook & accessToken.");
            return;
        }

        
        String finalQuery = "QUERY_HERE";
        

        
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(accessToken);

            Map<String, String> answerBody = new HashMap<>();
            answerBody.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(answerBody, headers);

            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("✅ Submission response: " + submitResponse.getBody());
        } catch (Exception e) {
            System.out.println("⚠️ Submission failed: " + e.getMessage());
        }
    }
}
