package com.StudyLink.www.service;

import com.StudyLink.www.dto.InquiryDTO;
import com.StudyLink.www.entity.Inquiry;
import com.StudyLink.www.repository.InquiryRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final PasswordEncoder passwordEncoder; // ✅ Security에 Bean 등록 필요

    /* ===================== 목록 ===================== */
    @Transactional(readOnly = true)
    @Override
    public Page<InquiryDTO> getList(int page) {
        int pageIndex = Math.max(page - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                10,
                Sort.by(Sort.Direction.DESC, "qno")
        );

        Page<Inquiry> result = inquiryRepository.findAll(pageable);
        return result.map(this::toDto);
    }

    /* ===================== 등록 ===================== */
    @Transactional
    @Override
    public void register(InquiryDTO dto, String writerEmail) {
        if (dto == null) throw new IllegalArgumentException("InquiryDTO is null");

        // ✅ 공개여부 기본값 방어
        String isPublic = (dto.getIsPublic() == null || dto.getIsPublic().isBlank())
                ? "N"
                : dto.getIsPublic();

        // ✅ 비공개면 비밀번호 필수
        if ("N".equals(isPublic)) {
            if (dto.getPassword() == null || dto.getPassword().isBlank()) {
                throw new IllegalArgumentException("비공개 문의는 비밀번호가 필요합니다.");
            }
        } else {
            // 공개면 비밀번호는 저장 안 하도록 비움
            dto.setPassword(null);
        }

        Inquiry inquiry = toEntity(dto);

        // ✅ 비밀번호는 암호화 저장(비공개일 때만)
        if ("N".equals(isPublic) && dto.getPassword() != null && !dto.getPassword().isBlank()) {
            inquiry.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        // writerEmail은 현재 엔티티에 컬럼이 없어서 저장은 안 함.
        inquiryRepository.save(inquiry);
    }

    /* ===================== 상세 ===================== */
    @Transactional(readOnly = true)
    @Override
    public InquiryDTO getDetail(Long qno) {
        Inquiry inquiry = inquiryRepository.findById(qno)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found. qno=" + qno));
        return toDto(inquiry);
    }

    /* ===================== 답변 ===================== */
    @Transactional
    @Override
    public void answer(Long qno, String adminContent) {
        if (qno == null) throw new IllegalArgumentException("qno is null");
        inquiryRepository.answer(qno, adminContent);
    }

    /* ===================== 비공개 비밀번호 검증 ===================== */
    @Transactional(readOnly = true)
    @Override
    public boolean verifyPassword(Long qno, String rawPassword) {
        if (qno == null) throw new IllegalArgumentException("qno is null");
        if (rawPassword == null || rawPassword.isBlank()) return false;

        String enc = inquiryRepository.findPasswordByQno(qno)
                .orElse(null);

        // 비밀번호가 저장되어 있지 않으면(false)
        if (enc == null || enc.isBlank()) return false;

        return passwordEncoder.matches(rawPassword, enc);
    }

    /* ===================== 비공개 상세 조회(비번 통과) ===================== */
    @Transactional(readOnly = true)
    @Override
    public InquiryDTO getDetailWithPassword(Long qno, String rawPassword) {
        Inquiry inquiry = inquiryRepository.findById(qno)
                .orElseThrow(() -> new EntityNotFoundException("Inquiry not found. qno=" + qno));

        // 공개면 그냥 반환
        if (!"N".equals(inquiry.getIsPublic())) {
            return toDto(inquiry);
        }

        // 비공개면 비번 검증
        if (!verifyPassword(qno, rawPassword)) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }

        return toDto(inquiry);
    }

    /* ===================== DTO <-> Entity ===================== */
    private InquiryDTO toDto(Inquiry e) {
        if (e == null) return null;

        return InquiryDTO.builder()
                .qno(e.getQno())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .answerAt(e.getAnswerAt())
                .title(e.getTitle())
                .userContent(e.getUserContent())
                .adminContent(e.getAdminContent())
                .isPublic(e.getIsPublic())
                .choose(e.getChoose())
                // password는 화면에 내려주지 않음(보안)
                .build();
    }

    private Inquiry toEntity(InquiryDTO d) {
        if (d == null) return null;

        return Inquiry.builder()
                .qno(d.getQno())
                .status(d.getStatus())
                .createdAt(d.getCreatedAt())
                .answerAt(d.getAnswerAt())
                .title(d.getTitle())
                .userContent(d.getUserContent())
                .adminContent(d.getAdminContent())
                .isPublic(d.getIsPublic())
                .choose(d.getChoose())
                // password는 register()에서 암호화 후 setPassword로 넣음
                .build();
    }
}
