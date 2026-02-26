package com.perfumeshop.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.Map;

/**
 * Service for verifying KHQR payment by MD5 using Bakong API.
 *
 * API:
 * POST /v1/check_transaction_by_md5
 * Body:
 * {
 *   "md5": "xxxxx"
 * }
 *
 * Response:
 * {
 *   "responseCode": 0,
 *   "data": {...}
 * }
 *
 * If responseCode == 0 AND data != null → payment success
 */
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
    }

    /**
     * Verify payment by md5 hash
     *
     * @param md5 transaction md5
     * @return true if paid, false otherwise
     */
    @SuppressWarnings("unchecked")
    public boolean isPaid(String md5) {

        if (md5 == null || md5.isBlank()) {
            return false;
        }

        try {

            Map<String, Object> response = restClient.post()
                    .uri("/v1/check_transaction_by_md5")
                    .body(Map.of("md5", md5.trim()))
                    .retrieve()
                    .body(Map.class);

            if (response == null) {
                return false;
            }

            Object responseCode = response.get("responseCode");
            Object data = response.get("data");

            boolean success = false;

            if (responseCode instanceof Number number) {
                success = number.intValue() == 0;
            } else if (responseCode != null) {
                success = "0".equals(String.valueOf(responseCode));
            }

            return success && data != null;

        } catch (Exception ex) {
            // log error in real production
            return false;
        }
    }
}