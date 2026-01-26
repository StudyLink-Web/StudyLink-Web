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
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int PAGE_SIZE = 10;
    private final CommunityRepository communityRepository;

    @Transactional
    @Override
    public Long insert(CommunityDTO communityDTO, MultipartFile[] files) {
        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");

        int fileCount = 0;
        if (files != null) {
            for (MultipartFile f : files) {
                if (f != null && !f.isEmpty()) fileCount++;
            }
        }

        if (communityDTO.getReadCount() == null) communityDTO.setReadCount(0);
        if (communityDTO.getCmtQty() == null) communityDTO.setCmtQty(0);
        communityDTO.setFileQty(fileCount);

        Community saved = communityRepository.save(convertDtoToEntity(communityDTO));
        return saved.getBno();
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CommunityDTO> getList(int pageNo) {
        int safePageNo = Math.max(pageNo, 1);

        Pageable pageable = PageRequest.of(
                safePageNo - 1,
                PAGE_SIZE,
                Sort.by(Sort.Direction.DESC, "bno")
        );

        return communityRepository.findAll(pageable).map(this::convertEntityToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public CommunityDTO getDetail(Long bno) {
        if (bno == null) return null;

        return communityRepository.findById(bno)
                .map(this::convertEntityToDto)
                .orElse(null);
    }

    @Transactional
    @Override
    public Long modify(CommunityDTO communityDTO) {
        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");
        if (communityDTO.getBno() == null) throw new IllegalArgumentException("bno is null");

        Community community = communityRepository.findById(communityDTO.getBno())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커뮤니티 글"));

        community.setTitle(communityDTO.getTitle());
        community.setWriter(communityDTO.getWriter());
        community.setDepartment(communityDTO.getDepartment());
        community.setContent(communityDTO.getContent());
        community.setReadCount(communityDTO.getReadCount() == null ? 0 : communityDTO.getReadCount());
        community.setCmtQty(communityDTO.getCmtQty() == null ? 0 : communityDTO.getCmtQty());
        community.setFileQty(communityDTO.getFileQty() == null ? 0 : communityDTO.getFileQty());

        return community.getBno();
    }

    @Transactional
    @Override
    public void remove(Long bno) {
        if (bno == null) return;
        if (!communityRepository.existsById(bno)) return;

        communityRepository.deleteById(bno);
    }
}
