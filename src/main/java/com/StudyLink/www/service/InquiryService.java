package com.StudyLink.www.service;

import com.StudyLink.www.dto.AdminInquiryDTO;
import com.StudyLink.www.dto.InquiryDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface InquiryService {

    Page<InquiryDTO> getList(int pageNo, String category, String status, String keyword);

    void register(InquiryDTO inquiryDTO, String loginEmail);

    InquiryDTO getDetail(Long qno);

    boolean verifyPassword(Long qno, String password);

    void answer(Long qno, String adminContent);

    void updateStatus(Long qno, String status);

    Page<AdminInquiryDTO> searchInquiryList(String choose, String status, String username, LocalDate startDate, LocalDate endDate, Pageable sortedPageable);

    InquiryDTO findById(Long id);
}
