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
        requestBody.put("name", "Uzma Farzana M");                     // Your Name
        requestBody.put("regNo", "1RF22IS093");                        // Your RegNo
        requestBody.put("email", "rvit22bis056.rvitm@rvei.edu.in");    // Your Email

        String webhookUrl = null;
        String accessToken = null;

        try {
            ResponseEntity<Map> response =
                    restTemplate.postForEntity(generateUrl, requestBody, Map.class);

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

        // STOP if webhook or accessToken is missing
        if (webhookUrl == null || accessToken == null) {
            System.out.println("❌ Cannot continue without webhook & accessToken.");
            return;
        }

        // STEP 2: Final SQL query from Question 1 (odd regNo)
        String finalQuery =
                "SELECT p.AMOUNT AS SALARY, " +
                        "CONCAT(e.FIRST_NAME, ' ', e.LAST_NAME) AS NAME, " +
                        "TIMESTAMPDIFF(YEAR, e.DOB, CURDATE()) AS AGE, " +
                        "d.DEPARTMENT_NAME " +
                        "FROM PAYMENTS p " +
                        "JOIN EMPLOYEE e ON p.EMP_ID = e.EMP_ID " +
                        "JOIN DEPARTMENT d ON e.DEPARTMENT = d.DEPARTMENT_ID " +
                        "WHERE DAY(p.PAYMENT_TIME) <> 1 " +
                        "ORDER BY p.AMOUNT DESC " +
                        "LIMIT 1;";

        // STEP 3: Submit answer to webhook
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", accessToken); // Use token as plain header

            Map<String, String> answerBody = new HashMap<>();
            answerBody.put("finalQuery", finalQuery);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(answerBody, headers);

            System.out.println("📤 Sending submission to: " + webhookUrl);
            System.out.println("📤 Headers: " + headers);
            System.out.println("📤 Body: " + answerBody);

            ResponseEntity<String> submitResponse =
                    restTemplate.postForEntity(webhookUrl, entity, String.class);

            System.out.println("✅ Submission response: " + submitResponse.getBody());
        } catch (Exception e) {
            System.out.println("⚠️ Submission failed: " + e.getMessage());
        }
    }
}
