// CommunityServiceImpl.java
package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityDTO;
import com.StudyLink.www.dto.CommunityFileDTO;
import com.StudyLink.www.entity.Community;
import com.StudyLink.www.entity.CommunityFile;
import com.StudyLink.www.repository.CommunityFileRepository;
import com.StudyLink.www.repository.CommunityRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class CommunityServiceImpl implements CommunityService {

    private static final int PAGE_SIZE = 10;

    private final CommunityRepository communityRepository;
    private final CommunityFileRepository communityFileRepository;

    @Value("${app.upload.root:D:/upload}")
    private String uploadRoot;

    private int fileTypeByName(String name) {
        if (name == null) return 0;
        String n = name.toLowerCase();
        return (n.endsWith(".png") || n.endsWith(".jpg") || n.endsWith(".jpeg")
                || n.endsWith(".gif") || n.endsWith(".webp")) ? 1 : 0;
    }

    private CommunityFileDTO toDto(CommunityFile f) {
        return CommunityFileDTO.builder()
                .fno(f.getFno())
                .bno(f.getBno())
                .uuid(f.getUuid())
                .fileName(f.getFileName())
                .saveDir(f.getSaveDir())
                .fileSize(f.getFileSize())
                .fileType(f.getFileType())
                .build();
    }

    private Path resolveSavedPath(CommunityFile f) {
        return Paths.get(uploadRoot, "community", f.getSaveDir(), f.getUuid());
    }

    private void safeDeleteFile(Path p) {
        if (p == null) return;
        try {
            Files.deleteIfExists(p);
        } catch (Exception e) {
            log.warn("file delete fail: {}", p, e);
        }
    }

    private int saveNewFiles(Long bno, MultipartFile[] files) {
        if (bno == null || files == null || files.length == 0) return 0;

        LocalDate today = LocalDate.now();
        String saveDir = String.format("%04d/%02d/%02d",
                today.getYear(), today.getMonthValue(), today.getDayOfMonth());
        Path dir = Paths.get(uploadRoot, "community", saveDir);

        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            throw new RuntimeException("upload dir create fail: " + dir, e);
        }

        int savedCnt = 0;
        for (MultipartFile mf : files) {
            if (mf == null || mf.isEmpty()) continue;

            String original = Optional.ofNullable(mf.getOriginalFilename()).orElse("file");
            String uuid = UUID.randomUUID().toString();

            try {
                mf.transferTo(dir.resolve(uuid));
            } catch (IOException e) {
                throw new RuntimeException("file save fail: " + original, e);
            }

            communityFileRepository.save(
                    CommunityFile.builder()
                            .bno(bno)
                            .uuid(uuid)
                            .fileName(original)
                            .saveDir(saveDir)
                            .fileSize(mf.getSize())
                            .fileType(fileTypeByName(original))
                            .build()
            );
            savedCnt++;
        }
        return savedCnt;
    }

    private int deleteSelectedFiles(Long bno, List<String> removeUuids) {
        if (bno == null || removeUuids == null || removeUuids.isEmpty()) return 0;

        int deleted = 0;
        Set<String> uniq = new LinkedHashSet<>();
        for (String u : removeUuids) {
            if (u != null && !u.isBlank()) uniq.add(u.trim());
        }

        for (String uuid : uniq) {
            Optional<CommunityFile> opt = communityFileRepository.findByUuid(uuid);
            if (opt.isEmpty()) continue;

            CommunityFile f = opt.get();
            if (!bno.equals(f.getBno())) continue;

            safeDeleteFile(resolveSavedPath(f));
            communityFileRepository.delete(f);
            deleted++;
        }
        return deleted;
    }

    private int recalcFileQty(Long bno) {
        if (bno == null) return 0;
        return communityFileRepository.findAllByBnoOrderByFnoDesc(bno).size();
    }

    @Transactional
    @Override
    public Long insert(CommunityDTO communityDTO, MultipartFile[] files) {
        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");

        if (communityDTO.getReadCount() == null) communityDTO.setReadCount(0);
        if (communityDTO.getCmtQty() == null) communityDTO.setCmtQty(0);
        if (communityDTO.getFileQty() == null) communityDTO.setFileQty(0);

        Community saved = communityRepository.save(convertDtoToEntity(communityDTO));
        Long bno = saved.getBno();

        saveNewFiles(bno, files);
        saved.setFileQty(recalcFileQty(bno));

        return bno;
    }

    @Transactional(readOnly = true)
    @Override
    public Page<CommunityDTO> getList(int pageNo) {
        int safePageNo = Math.max(pageNo, 1);
        Pageable pageable = PageRequest.of(
                safePageNo - 1, PAGE_SIZE, Sort.by(Sort.Direction.DESC, "bno"));
        return communityRepository.findAll(pageable)
                .map(this::convertEntityToDto);
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
    public Long modify(CommunityDTO communityDTO,
                       MultipartFile[] files,
                       List<String> removeUuids) {

        if (communityDTO == null) throw new IllegalArgumentException("communityDTO is null");
        if (communityDTO.getBno() == null) throw new IllegalArgumentException("bno is null");

        Long bno = communityDTO.getBno();

        Community community = communityRepository.findById(bno)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 커뮤니티 글"));

        community.setTitle(communityDTO.getTitle());
        community.setWriter(communityDTO.getWriter());
        community.setDepartment(communityDTO.getDepartment());
        community.setContent(communityDTO.getContent());

        deleteSelectedFiles(bno, removeUuids);
        saveNewFiles(bno, files);
        community.setFileQty(recalcFileQty(bno));

        return bno;
    }

    @Transactional
    @Override
    public void remove(Long bno) {
        if (bno == null || !communityRepository.existsById(bno)) return;

        List<CommunityFile> files =
                communityFileRepository.findAllByBnoOrderByFnoDesc(bno);
        for (CommunityFile f : files) {
            safeDeleteFile(resolveSavedPath(f));
        }

        communityFileRepository.deleteAllByBno(bno);
        communityRepository.deleteById(bno);
    }

    @Transactional(readOnly = true)
    @Override
    public List<CommunityFileDTO> getFileList(Long bno) {
        if (bno == null) return List.of();
        return communityFileRepository.findAllByBnoOrderByFnoDesc(bno)
                .stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    @Override
    public CommunityFileDTO getFileByUuid(String uuid) {
        if (uuid == null || uuid.isBlank()) return null;
        return communityFileRepository.findByUuid(uuid)
                .map(this::toDto)
                .orElse(null);
    }
}
