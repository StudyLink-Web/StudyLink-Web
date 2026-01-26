package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;

public interface CommunityService {

    default Community convertDtoToEntity(CommunityDTO dto) {
        if (dto == null) return null;

        return Community.builder()
                .bno(dto.getBno())
                .title(dto.getTitle())
                .writer(dto.getWriter())
                .department(dto.getDepartment())
                .content(dto.getContent())
                .readCount(dto.getReadCount() == null ? 0 : dto.getReadCount())
                .cmtQty(dto.getCmtQty() == null ? 0 : dto.getCmtQty())
                .fileQty(dto.getFileQty() == null ? 0 : dto.getFileQty())
                .build();
    }

    default CommunityDTO convertEntityToDto(Community entity) {
        if (entity == null) return null;

        return CommunityDTO.builder()
                .bno(entity.getBno())
                .title(entity.getTitle())
                .writer(entity.getWriter())
                .department(entity.getDepartment())
                .content(entity.getContent())
                .readCount(entity.getReadCount())
                .cmtQty(entity.getCmtQty())
                .fileQty(entity.getFileQty())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    Long insert(CommunityDTO communityDTO, MultipartFile[] files);

    Page<CommunityDTO> getList(int pageNo);

    CommunityDTO getDetail(Long bno);

    Long modify(CommunityDTO communityDTO);

    void remove(Long bno);
}
