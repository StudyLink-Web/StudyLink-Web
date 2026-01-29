package com.StudyLink.www.service;

import com.StudyLink.www.dto.InquiryDTO;
import org.springframework.data.domain.Page;

public interface InquiryService {

    Page<InquiryDTO> getList(int page);

    void register(InquiryDTO dto, String writerEmail);

    InquiryDTO getDetail(Long qno);

    void answer(Long qno, String adminContent);

    /* ✅ 비공개 문의 비밀번호 검증 */
    boolean verifyPassword(Long qno, String rawPassword);

    /* ✅ 비공개 문의 상세 조회(비밀번호 통과 시) */
    InquiryDTO getDetailWithPassword(Long qno, String rawPassword);
}
