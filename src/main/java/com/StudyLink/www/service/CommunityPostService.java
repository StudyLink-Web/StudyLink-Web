package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityPostDTO;
import org.springframework.data.domain.Page;

public interface CommunityPostService {
    Long register(CommunityPostDTO dto, Long loginUserId);
    Page<CommunityPostDTO> list(int pageNo);
    CommunityPostDTO detail(Long postId);
}
