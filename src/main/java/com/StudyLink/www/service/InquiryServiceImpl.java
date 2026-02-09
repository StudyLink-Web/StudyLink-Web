package com.StudyLink.www.service;

import com.StudyLink.www.dto.AdminInquiryDTO;
import com.StudyLink.www.dto.InquiryDTO;
import com.StudyLink.www.dto.UsersDTO;
import com.StudyLink.www.entity.Inquiry;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.InquiryRepository;
import com.StudyLink.www.repository.PushTokenRepository;
import com.StudyLink.www.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class InquiryServiceImpl implements InquiryService {

    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;

    private final NotificationService notificationService;
    private final FCMService fcmService;
    private final BoardRepository boardRepository;
    private final PushTokenRepository pushTokenRepository;

    /* ===================== 목록(검색 포함) ===================== */
    @Override
    @Transactional(readOnly = true)
    public Page<InquiryDTO> getList(int pageNo, String category, String status, String keyword) {

        int pageIndex = Math.max(pageNo - 1, 0);

        Pageable pageable = PageRequest.of(
                pageIndex,
                10,
                Sort.by(Sort.Direction.DESC, "qno")
        );

        return inquiryRepository.search(category, status, keyword, pageable)
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
        inquiry.setWriterEmail(loginEmail);
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
        Inquiry inquiry = inquiryRepository.findById(qno)
                .orElseThrow(() -> new EntityNotFoundException("해당 문의 내역이 없습니다."));

        inquiry.setAdminContent(adminContent);
        inquiry.setAnswerAt(LocalDateTime.now());
        inquiry.setStatus("COMPLETE");

        long userId = userRepository.findByEmail(inquiry.getWriterEmail())
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 없습니다."))
                .getUserId();

        notificationService.createNotification(userId, "ANSWER_RECEIVED", "관리자가 문의내역에 답변을 달았습니다.", null);
    }

    /* ===================== 상태 변경 ===================== */
    @Override
    @Transactional
    public void updateStatus(Long qno, String status) {
        Inquiry inquiry = inquiryRepository.findById(qno).orElseThrow();
        inquiry.setStatus(status);
    }

    /* ===================== 관리자 문의내역 검색 ===================== */
    @Override
    public Page<AdminInquiryDTO> searchInquiryList(String choose, String status, String username,
                                                   LocalDate startDate, LocalDate endDate,
                                                   Pageable sortedPageable) {

        LocalDateTime startDateTime = (startDate != null) ? startDate.atStartOfDay() : null;
        LocalDateTime endDatePlus = (endDate != null) ? endDate.plusDays(1).atStartOfDay() : null;

        Page<Inquiry> inquiryPage = inquiryRepository.searchInquiries(
                choose,
                status,
                username,
                startDateTime,
                endDatePlus,
                sortedPageable
        );

        List<AdminInquiryDTO> dtoList = new ArrayList<>();

        for (Inquiry inquiry : inquiryPage.getContent()) {

            InquiryDTO inquiryDTO = convertEntityToDto(inquiry);

            Users users = userRepository.findByEmail(inquiry.getWriterEmail()).orElse(null);
            UsersDTO usersDTO = (users != null) ? new UsersDTO(users) : null;

            dtoList.add(
                    AdminInquiryDTO.builder()
                            .inquiryDTO(inquiryDTO)
                            .usersDTO(usersDTO)
                            .build()
            );
        }

        return new PageImpl<>(dtoList, sortedPageable, inquiryPage.getTotalElements());
    }

    @Override
    public InquiryDTO findById(Long id) {
        return convertEntityToDto(
                inquiryRepository.findById(id)
                        .orElseThrow(() -> new EntityNotFoundException("문의 내용을 찾을 수 없습니다."))
        );
    }

    /* ===================== Entity → DTO ===================== */
    private InquiryDTO convertEntityToDto(Inquiry inquiry) {
        return InquiryDTO.builder()
                .qno(inquiry.getQno())
                .title(inquiry.getTitle())
                .userContent(inquiry.getUserContent())
                .adminContent(inquiry.getAdminContent())
                .choose(inquiry.getChoose())
                .category(inquiry.getCategory())      // ✅ fix
                .status(inquiry.getStatus())
                .isPublic(inquiry.getIsPublic())
                .password(inquiry.getPassword())
                .createdAt(inquiry.getCreatedAt())
                .writerEmail(inquiry.getWriterEmail())
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
                .choose(dto.getChoose())
                .category(dto.getCategory())          // ✅ fix
                .status(dto.getStatus())
                .isPublic(dto.getIsPublic())
                .password(dto.getPassword())
                .createdAt(dto.getCreatedAt())
                .answerAt(dto.getAnswerAt())
                .writerEmail(dto.getWriterEmail())
                .build();
    }
}
