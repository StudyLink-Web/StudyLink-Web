// src/main/java/com/StudyLink/www/service/CommunityService.java
package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.dto.CommunityFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;

public interface CommunityService {

    default Community convertDtoToEntity(CommunityDTO dto) {
        if (dto == null) return null;

        return Community.builder()
                .bno(dto.getBno())
                .userId(dto.getUserId())
                .email(dto.getEmail())
                .role((dto.getRole() == null || dto.getRole().isBlank()) ? "USER" : dto.getRole())
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
                .userId(entity.getUserId())
                .email(entity.getEmail())
                .role(entity.getRole())
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

    Long insert(CommunityFileDTO communityFileDTO);

    Page<CommunityDTO> getList(int pageNo);

    CommunityFileDTO getDetail(Long bno);

    Long modify(CommunityFileDTO communityFileDTO);

    void remove(Long bno);

    FileDTO getFile(String uuid);

    void increaseReadCount(Long bno);
}
