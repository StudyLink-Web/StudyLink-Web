package com.StudyLink.www.service;

import com.StudyLink.www.dto.InquiryDTO;
import com.StudyLink.www.entity.Inquiry;
import com.StudyLink.www.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;

    /* ===================== 목록 ===================== */
    @Override
    @Transactional(readOnly = true)
    public Page<InquiryDTO> getList(int pageNo) {

        int pageIndex = Math.max(pageNo - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                10,
                Sort.by(Sort.Direction.DESC, "qno")
        );

        return inquiryRepository.findAll(pageable)
                .map(this::convertEntityToDto);
    }

    /* ===================== 등록 ===================== */
    @Override
    @Transactional
    public void register(InquiryDTO inquiryDTO, String loginEmail) {

        if (inquiryDTO.getStatus() == null || inquiryDTO.getStatus().isBlank()) {
            inquiryDTO.setStatus("PENDING");
        }

        Inquiry inquiry = convertDtoToEntity(inquiryDTO);
        inquiryRepository.save(inquiry);
    }

    /* ===================== 상세 ===================== */
    @Override
    @Transactional(readOnly = true)
    public InquiryDTO getDetail(Long qno) {
        return inquiryRepository.findById(qno)
                .map(this::convertEntityToDto)
                .orElse(null);
    }

    /* ===================== 비밀번호 검증 ===================== */
    @Override
    @Transactional(readOnly = true)
    public boolean verifyPassword(Long qno, String password) {
        return inquiryRepository.findById(qno)
                .map(i -> i.getPassword() != null && i.getPassword().equals(password))
                .orElse(false);
    }

    /* ===================== 관리자 답변 ===================== */
    @Override
    @Transactional
    public void answer(Long qno, String adminContent) {
        Inquiry inquiry = inquiryRepository.findById(qno).orElseThrow();

        // ✅ 이미 답변 있으면 수정 불가
        if (inquiry.getAdminContent() != null && !inquiry.getAdminContent().trim().isEmpty()) {
            throw new IllegalStateException("이미 답변이 등록된 문의입니다.");
        }

        inquiry.setAdminContent(adminContent);
        inquiry.setAnswerAt(LocalDateTime.now());
        inquiry.setStatus("COMPLETE"); // ✅ 답변 달리면 COMPLETE
    }

    /* ===================== 상태 변경 ===================== */
    @Override
    @Transactional
    public void updateStatus(Long qno, String status) {
        Inquiry inquiry = inquiryRepository.findById(qno).orElseThrow();
        inquiry.setStatus(status);
    }

    /* ===================== Entity → DTO ===================== */
    private InquiryDTO convertEntityToDto(Inquiry inquiry) {
        return InquiryDTO.builder()
                .qno(inquiry.getQno())
                .title(inquiry.getTitle())
                .userContent(inquiry.getUserContent())
                .adminContent(inquiry.getAdminContent())
                .status(inquiry.getStatus())
                .isPublic(inquiry.getIsPublic())
                .createdAt(inquiry.getCreatedAt())
                .answerAt(inquiry.getAnswerAt())
                .build();
    }

    /* ===================== DTO → Entity ===================== */
    private Inquiry convertDtoToEntity(InquiryDTO dto) {
        return Inquiry.builder()
                .qno(dto.getQno())
                .title(dto.getTitle())
                .userContent(dto.getUserContent())
                .adminContent(dto.getAdminContent())
                .status(dto.getStatus())
                .isPublic(dto.getIsPublic())
                .password(dto.getPassword())
                .answerAt(dto.getAnswerAt())
                .build();
    }
}
