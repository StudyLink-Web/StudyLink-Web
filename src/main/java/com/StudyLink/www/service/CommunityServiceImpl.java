package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.entity.Community;
import com.StudyLink.www.repository.CommunityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImpl implements CommunityService {

    private final CommunityRepository communityRepository;

    @Transactional
    @Override
    public Long insert(CommunityDTO communityDTO) {
        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");

        // userId가 이미 있으면(중복/갱신 시도) insert가 아니라 modify로 처리되게 방어
        if (communityDTO.getUserId() != null && communityRepository.existsById(communityDTO.getUserId())) {
            return modify(communityDTO);
        }

        Community community = convertDtoToEntity(communityDTO);
        return communityRepository.save(community).getUserId();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CommunityDTO> getList(int pageNo) {
        int safePageNo = Math.max(pageNo, 1);
        Pageable pageable = PageRequest.of(safePageNo - 1, 10, Sort.by("userId").descending());
        return communityRepository.findAll(pageable).map(this::convertEntityToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public CommunityDTO getDetail(Long userId) {
        if (userId == null) return null;
        return communityRepository.findById(userId)
                .map(this::convertEntityToDto)
                .orElse(null);
    }

    @Transactional
    @Override
    public Long modify(CommunityDTO communityDTO) {
        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");
        if (communityDTO.getUserId() == null) throw new IllegalArgumentException("userId is null");

        Community community = communityRepository.findById(communityDTO.getUserId())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커뮤니티 사용자"));

        // email은 보통 수정 불가(고유값/로그인키)라서 null/blank면 기존값 유지
        if (communityDTO.getEmail() != null && !communityDTO.getEmail().isBlank()) {
            community.setEmail(communityDTO.getEmail());
        }

        if (communityDTO.getName() != null) community.setName(communityDTO.getName());
        if (communityDTO.getNickname() != null) community.setNickname(communityDTO.getNickname());
        if (communityDTO.getRole() != null && !communityDTO.getRole().isBlank()) community.setRole(communityDTO.getRole());

        community.setPagenum(communityDTO.getPagenum());
        community.setBno(communityDTO.getBno());

        // dirty checking으로 update (save() 불필요)
        return community.getUserId();
    }

    @Transactional
    @Override
    public void remove(Long userId) {
        if (userId == null) return;
        if (!communityRepository.existsById(userId)) return;
        communityRepository.deleteById(userId);
    }
}
