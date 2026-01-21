package com.StudyLink.www.service;

import com.StudyLink.www.repository.ExchangeRequestRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@Service
public class PortoneServiceImpl implements PortoneService {
    @Value("${portone.api.key}")
    private String apiKey;

    @Value("${portone.api.secret}")
    private String apiSecret;

    private final ExchangeRequestRepository exchangeRequestRepository;
    private final RestTemplate restTemplate;

    @Override
    public String getAccessToken() {
        String url = "https://api.iamport.kr/users/getToken";

        Map<String, String> body = new HashMap<>();
        body.put("imp_key", apiKey);
        body.put("imp_secret", apiSecret);

        Map response = restTemplate.postForObject(url, body, Map.class);

        log.info("PortOne token response: {}", response);

        Map<String, Object> responseData =
                (Map<String, Object>) response.get("response");

        return (String) responseData.get("access_token");
    }
}
