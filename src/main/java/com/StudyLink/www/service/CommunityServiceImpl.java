// src/main/java/com/StudyLink/www/service/CommunityServiceImpl.java
package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.dto.CommunityFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Community;
import com.StudyLink.www.entity.CommunityFile;
import com.StudyLink.www.handler.CommunityFileHandler;
import com.StudyLink.www.handler.FileRemoveHandler;
import com.StudyLink.www.repository.CommunityFileRepository;
import com.StudyLink.www.repository.CommunityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int PAGE_SIZE = 10;

    private final CommunityRepository communityRepository;
    private final CommunityFileRepository communityFileRepository;
    private final CommunityFileHandler communityFileHandler;

    @Transactional
    @Override
    public Long insert(CommunityFileDTO communityFileDTO) {
        if (communityFileDTO == null || communityFileDTO.getCommunityDTO() == null) {
            throw new IllegalArgumentException("communityFileDTO/communityDTO is null");
        }

        CommunityDTO dto = communityFileDTO.getCommunityDTO();

        if (dto.getRole() == null || dto.getRole().isBlank()) dto.setRole("USER");
        if (dto.getReadCount() == null) dto.setReadCount(0);
        if (dto.getCmtQty() == null) dto.setCmtQty(0);

        Community saved = communityRepository.save(convertDtoToEntity(dto));
        Long bno = saved.getBno();

        List<FileDTO> fileDTOList = communityFileDTO.getFileDTOList();
        if (fileDTOList != null && !fileDTOList.isEmpty()) {
            for (FileDTO f : fileDTOList) {
                communityFileRepository.save(CommunityFile.builder()
                        .uuid(f.getUuid())
                        .bno(bno)
                        .saveDir(f.getSaveDir())
                        .fileName(f.getFileName())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize())
                        .build());
            }
            saved.setFileQty(fileDTOList.size());
        } else {
            saved.setFileQty(0);
        }

        return bno;
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

        return communityRepository.findAll(pageable)
                .map(this::convertEntityToDto);
    }

    @Transactional(readOnly = true)
    @Override
    public CommunityFileDTO getDetail(Long bno) {
        if (bno == null) return null;

        CommunityDTO dto = communityRepository.findById(bno)
                .map(this::convertEntityToDto)
                .orElse(null);

        if (dto == null) return null;

        List<CommunityFile> files = communityFileRepository.findByBno(bno);
        if (files == null) files = Collections.emptyList();

        List<FileDTO> fileDTOList = files.stream()
                .filter(f -> f != null)
                .filter(f -> !isThumb(f))
                .map(f -> FileDTO.builder()
                        .uuid(f.getUuid())
                        .saveDir(f.getSaveDir())
                        .fileName(f.getFileName())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize() == null ? 0L : f.getFileSize())
                        .postId(f.getBno()) // ✅ board와 동일 필드로 맞춤
                        .build())
                .toList();

        dto.setFileDTOList(fileDTOList);
        dto.setFileQty(fileDTOList.size());

        return CommunityFileDTO.builder()
                .communityDTO(dto)
                .fileDTOList(fileDTOList)
                .build();
    }

    @Transactional
    @Override
    public Long modify(CommunityFileDTO communityFileDTO) {
        if (communityFileDTO == null || communityFileDTO.getCommunityDTO() == null) {
            throw new IllegalArgumentException("communityFileDTO/communityDTO is null");
        }

        CommunityDTO dto = communityFileDTO.getCommunityDTO();
        if (dto.getBno() == null) throw new IllegalArgumentException("bno is null");

        Community community = communityRepository.findById(dto.getBno())
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커뮤니티 글"));

        if (dto.getRole() == null || dto.getRole().isBlank()) {
            dto.setRole(community.getRole() == null ? "USER" : community.getRole());
        }

        community.setTitle(dto.getTitle());
        community.setDepartment(dto.getDepartment());
        community.setContent(dto.getContent());
        community.setRole(dto.getRole());

        List<FileDTO> newFiles = communityFileDTO.getFileDTOList();
        if (newFiles != null && !newFiles.isEmpty()) {

            List<CommunityFile> oldFiles = communityFileRepository.findByBno(community.getBno());
            if (oldFiles != null && !oldFiles.isEmpty()) {
                FileRemoveHandler remover = new FileRemoveHandler();
                for (CommunityFile old : oldFiles) {
                    if (old == null) continue;
                    remover.removeFile(FileDTO.builder()
                            .uuid(old.getUuid())
                            .saveDir(old.getSaveDir())
                            .fileName(old.getFileName())
                            .fileType(old.getFileType())
                            .fileSize(old.getFileSize() == null ? 0L : old.getFileSize())
                            .build());
                }
            }

            communityFileRepository.deleteByBno(community.getBno());

            for (FileDTO f : newFiles) {
                communityFileRepository.save(CommunityFile.builder()
                        .uuid(f.getUuid())
                        .bno(community.getBno())
                        .saveDir(f.getSaveDir())
                        .fileName(f.getFileName())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize())
                        .build());
            }

            community.setFileQty(newFiles.size());
        }

        return community.getBno();
    }

    @Transactional
    @Override
    public void remove(Long bno) {
        if (bno == null) return;
        if (!communityRepository.existsById(bno)) return;

        List<CommunityFile> oldFiles = communityFileRepository.findByBno(bno);
        if (oldFiles != null && !oldFiles.isEmpty()) {
            FileRemoveHandler remover = new FileRemoveHandler();
            for (CommunityFile old : oldFiles) {
                if (old == null) continue;
                remover.removeFile(FileDTO.builder()
                        .uuid(old.getUuid())
                        .saveDir(old.getSaveDir())
                        .fileName(old.getFileName())
                        .fileType(old.getFileType())
                        .fileSize(old.getFileSize() == null ? 0L : old.getFileSize())
                        .build());
            }
        }

        communityFileRepository.deleteByBno(bno);
        communityRepository.deleteById(bno);
    }

    @Transactional(readOnly = true)
    @Override
    public FileDTO getFile(String uuid) {
        if (uuid == null || uuid.isBlank()) return null;

        return communityFileRepository.findById(uuid)
                .map(f -> FileDTO.builder()
                        .uuid(f.getUuid())
                        .saveDir(f.getSaveDir())
                        .fileName(f.getFileName())
                        .fileType(f.getFileType())
                        .fileSize(f.getFileSize() == null ? 0L : f.getFileSize())
                        .postId(f.getBno())
                        .build())
                .orElse(null);
    }

    @Transactional
    @Override
    public void increaseReadCount(Long bno) {
        if (bno == null) return;
        communityRepository.increaseReadCount(bno);
    }

    public List<FileDTO> uploadAndFilter(MultipartFile[] files) {
        if (files == null || files.length == 0 || files[0] == null || files[0].isEmpty()) return null;

        List<FileDTO> uploaded = communityFileHandler.uploadFile(files);
        if (uploaded == null) return null;

        return uploaded.stream()
                .filter(f -> f != null)
                .filter(f -> !f.isThumbnail())
                .toList();
    }

    private boolean isThumb(CommunityFile f) {
        String u = f.getUuid();
        String n = f.getFileName();
        return (u != null && u.contains("_th_")) || (n != null && n.contains("_th_"));
    }
}
