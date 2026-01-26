package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;

public interface CommunityService {

    default Community convertDtoToEntity(CommunityDTO dto) {
        if (dto == null) return null;

        return Community.builder()
                .bno(dto.getBno())
                .title(dto.getTitle())
                .writer(dto.getWriter())
                .readCount(dto.getReadCount())
                .cmtQty(dto.getCmtQty())
                .fileQty(dto.getFileQty())
                .build();
    }

    default CommunityDTO convertEntityToDto(Community entity) {
        if (entity == null) return null;

        return CommunityDTO.builder()
                .bno(entity.getBno())
                .title(entity.getTitle())
                .writer(entity.getWriter())
                .readCount(entity.getReadCount())
                .cmtQty(entity.getCmtQty())
                .fileQty(entity.getFileQty())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    Long insert(CommunityDTO communityDTO);

    Page<CommunityDTO> getList(int pageNo);

    CommunityDTO getDetail(Long bno);

    Long modify(CommunityDTO communityDTO);

    void remove(Long bno);
}
