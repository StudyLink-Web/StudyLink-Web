package com.StudyLink.www.service;

import com.StudyLink.www.dto.InquiryDTO;
import org.springframework.data.domain.Page;

public interface InquiryService {

    // ✅ Page로 변경 (InquiryPageHandler가 Page<T> 받기 때문)
    Page<InquiryDTO> getList(int pageNo);

    void register(InquiryDTO inquiryDTO, String loginEmail);

    InquiryDTO getDetail(Long qno);

    boolean verifyPassword(Long qno, String password);

    void answer(Long qno, String adminContent);

    // ✅ 추가
    void updateStatus(Long qno, String status);
}
