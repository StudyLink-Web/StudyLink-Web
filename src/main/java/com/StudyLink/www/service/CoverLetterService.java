package com.StudyLink.www.service;

import com.StudyLink.www.dto.CoverLetterDTO;
import com.StudyLink.www.entity.CoverLetter;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.CoverLetterRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CoverLetterService {

    private final CoverLetterRepository coverLetterRepository;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${python.api.url}")
    private String pythonApiUrl;

    /**
     * AI 자소서 생성 요청
     */
    public String generateAIContent(Users user, CoverLetterDTO.Request request) {
        CoverLetterDTO.AIRequest aiRequest = CoverLetterDTO.AIRequest.builder()
                .name(user.getName())
                .university(request.getTargetUniversity())
                .major(request.getTargetMajor())
                .question(request.getQuestionText())
                .keywords(request.getKeywords())
                .tone(request.getTone())
                .build();

        try {
            log.info("[AI CoverLetter] 요청 시작 - User: {}, University: {}", user.getEmail(), request.getTargetUniversity());
            CoverLetterDTO.AIResponse response = restTemplate.postForObject(
                    pythonApiUrl + "/generate-cover-letter",
                    aiRequest,
                    CoverLetterDTO.AIResponse.class
            );
            return response != null ? response.getContent() : "AI 생성에 실패했습니다.";
        } catch (Exception e) {
            log.error("[AI CoverLetter] AI 서버 연동 실패: {}", e.getMessage());
            return "AI 서버와의 통신 중 오류가 발생했습니다: " + e.getMessage();
        }
    }

    /**
     * 생기부 텍스트에서 키워드 및 추천 정보 추출
     */
    public CoverLetterDTO.ExtractResponse extractFromRecord(CoverLetterDTO.ExtractRequest request) {
        try {
            log.info("[AI Extraction] 생기부 데이터 추출 시작");
            return restTemplate.postForObject(
                    pythonApiUrl + "/extract-record",
                    request,
                    CoverLetterDTO.ExtractResponse.class
            );
        } catch (Exception e) {
            log.error("[AI Extraction] AI 서버 연동 실패: {}", e.getMessage());
            // 실패 시 빈 값 반환 (사용자가 직접 입력할 수 있도록)
            return CoverLetterDTO.ExtractResponse.builder()
                    .keywords(List.of())
                    .suggestedTitle("")
                    .summary("데이터 추출 중 오류가 발생했습니다.")
                    .build();
        }
    }

    /**
     * 자소서 저장
     */
    @Transactional
    public CoverLetterDTO.Response saveCoverLetter(Users user, CoverLetterDTO.Request request, String content) {
        CoverLetter coverLetter = CoverLetter.builder()
                .user(user)
                .title(request.getTitle())
                .questionNum(request.getQuestionNum())
                .questionText(request.getQuestionText())
                .content(content)
                .targetUniversity(request.getTargetUniversity())
                .targetMajor(request.getTargetMajor())
                .status("DRAFT")
                .build();

        CoverLetter saved = coverLetterRepository.save(coverLetter);
        return convertToResponse(saved);
    }

    /**
     * 사용자의 자소서 목록 조회
     */
    public List<CoverLetterDTO.Response> getMyCoverLetters(Users user) {
        return coverLetterRepository.findByUserOrderByCreatedAtDesc(user)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    /**
     * 자소서 삭제
     */
    @Transactional
    public void deleteCoverLetter(Long id) {
        coverLetterRepository.deleteById(id);
    }

    private CoverLetterDTO.Response convertToResponse(CoverLetter coverLetter) {
        return CoverLetterDTO.Response.builder()
                .coverLetterId(coverLetter.getCoverLetterId())
                .title(coverLetter.getTitle())
                .questionNum(coverLetter.getQuestionNum())
                .content(coverLetter.getContent())
                .targetUniversity(coverLetter.getTargetUniversity())
                .targetMajor(coverLetter.getTargetMajor())
                .createdAt(coverLetter.getCreatedAt())
                .build();
    }
}
