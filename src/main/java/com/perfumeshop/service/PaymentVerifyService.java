package com.perfumeshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

@Service
public class PaymentVerifyService {

    private final RestClient restClient;

    public PaymentVerifyService(
            @Value("${bakong.api.baseUrl:https://api-bakong.nbc.gov.kh}") String baseUrl,
            @Value("${bakong.api.token:}") String token
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        System.out.println("=== BAKONG VERIFY CONFIG ===");
        System.out.println("Base URL = " + baseUrl);
        System.out.println("Token exists = " + (token != null && !token.isBlank()));
    }

    @SuppressWarnings("unchecked")
    public boolean isPaid(String md5) {

        if (md5 == null || md5.isBlank()) {
            System.out.println("Bakong verify: md5 is blank");
            return false;
        }

        try {
            System.out.println("Bakong verify checking md5 = " + md5.trim());

            Map<String, Object> response = restClient.post()
                    .uri("/v1/check_transaction_by_md5")
                    .body(Map.of("md5", md5.trim()))
                    .retrieve()
                    .body(Map.class);

            System.out.println("Bakong verify raw response = " + response);

            if (response == null) {
                return false;
            }

            Object responseCode = response.get("responseCode");
            Object data = response.get("data");

            boolean success = false;

            if (responseCode instanceof Number number) {
                success = number.intValue() == 0;
            } else if (responseCode != null) {
                success = "0".equals(String.valueOf(responseCode).trim());
            }

            System.out.println("Bakong verify success = " + success);
            System.out.println("Bakong verify data = " + data);

            return success && data != null;

        } catch (Exception ex) {
            System.out.println("Bakong verify failed: " + ex.getMessage());
            ex.printStackTrace();
            return false;
        }
    }
}