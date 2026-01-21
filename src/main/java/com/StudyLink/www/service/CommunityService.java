package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.entity.Community;
import org.springframework.data.domain.Page;

public interface CommunityService {

    default Community convertDtoToEntity(CommunityDTO communityDTO) {
        if (communityDTO == null) return null;

        return Community.builder()
                .userId(communityDTO.getUserId())
                .email(communityDTO.getEmail())
                .name(communityDTO.getName())
                .nickname(communityDTO.getNickname())
                .role(communityDTO.getRole())
                .pagenum(communityDTO.getPagenum())
                .bno(communityDTO.getBno())
                .build();
    }

    default CommunityDTO convertEntityToDto(Community community) {
        if (community == null) return null;

        return CommunityDTO.builder()
                .userId(community.getUserId())
                .email(community.getEmail())
                .name(community.getName())
                .nickname(community.getNickname())
                .role(community.getRole())
                .pagenum(community.getPagenum())
                .bno(community.getBno())
                .createdAt(community.getCreatedAt())
                .updatedAt(community.getUpdatedAt())
                .build();
    }

    Long insert(CommunityDTO communityDTO);

    Page<CommunityDTO> getList(int pageNo);

    CommunityDTO getDetail(Long userId);

    Long modify(CommunityDTO communityDTO);

    void remove(Long userId);
}
