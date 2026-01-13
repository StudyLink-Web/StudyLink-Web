package com.StudyLink.www.service;

import com.StudyLink.www.dto.MapDataDTO;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class MapService {

    private final RestTemplate restTemplate = new RestTemplate();
    
    // 파이썬 서버 주소 (Hugging Face 클라우드 주소 - 챗봇과 동일 환경 가정)
    // 실제 운영 시에는 설정 파일에서 관리하는 것이 좋습니다.
    private final String AI_MAP_SERVER_URL = "https://yaimbot23-chatbot-docker.hf.space/map-data";

    public MapDataDTO.Response getMapData(MapDataDTO.Request request) {
        try {
            System.out.println("Calling Python Map API with request: " + request);
            MapDataDTO.Response response = restTemplate.postForObject(AI_MAP_SERVER_URL, request, MapDataDTO.Response.class);
            if (response != null && response.getItems() != null) {
                System.out.println("Received " + response.getItems().size() + " items from Python Map API.");
            } else {
                System.out.println("Received empty or null response from Python Map API.");
            }
            return response;
        } catch (Exception e) {
            System.err.println("Error calling Python Map API: " + e.getMessage());
            e.printStackTrace(); // 상세 스택트레이스 출력
            return MapDataDTO.Response.builder()
                    .items(java.util.Collections.emptyList())
                    .build();
        }
    }
}
