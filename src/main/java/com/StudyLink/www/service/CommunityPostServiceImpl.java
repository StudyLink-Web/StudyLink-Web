package com.StudyLink.www.service;

import com.StudyLink.www.dto.CommunityPostDTO;
import com.StudyLink.www.entity.CommunityPost;
import com.StudyLink.www.entity.CommunityPostFile;
import com.StudyLink.www.entity.Users;
import com.StudyLink.www.repository.CommunityPostRepository;
import com.StudyLink.www.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class CommunityPostServiceImpl implements CommunityPostService {

    private final CommunityPostRepository postRepository;
    private final UserRepository userRepository;

    // 업로드 루트(너 환경에 맞게)
    private final String uploadRoot = "D:/upload/community";

    @Override
    public Long register(CommunityPostDTO dto, Long loginUserId) {
        if (dto == null) throw new IllegalArgumentException("dto is null");
        if (loginUserId == null) throw new IllegalArgumentException("loginUserId is null");
        if (dto.getTitle() == null || dto.getTitle().isBlank()) throw new IllegalArgumentException("title required");
        if (dto.getContent() == null || dto.getContent().isBlank()) throw new IllegalArgumentException("content required");

        Users writer = userRepository.findById(loginUserId)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + loginUserId));

        CommunityPost post = CommunityPost.builder()
                .title(dto.getTitle().trim())
                .content(dto.getContent().trim())
                .writer(writer)
                .build();

        // 파일 저장
        if (dto.getUploadFiles() != null) {
            File root = new File(uploadRoot);
            if (!root.exists() && !root.mkdirs()) {
                throw new IllegalStateException("upload directory create failed: " + uploadRoot);
            }

            for (MultipartFile mf : dto.getUploadFiles()) {
                if (mf == null || mf.isEmpty()) continue;

                String original = (mf.getOriginalFilename() == null || mf.getOriginalFilename().isBlank())
                        ? "file"
                        : mf.getOriginalFilename();

                String saved = UUID.randomUUID() + "_" + original;
                File dest = new File(root, saved);

                try {
                    mf.transferTo(dest);
                } catch (Exception e) {
                    log.error("[CommunityPost upload] fail original={}", original, e);
                    throw new IllegalStateException("file upload failed: " + original, e);
                }

                CommunityPostFile f = CommunityPostFile.builder()
                        .originalName(original)
                        .savedName(saved)
                        .path(dest.getAbsolutePath())
                        .size(mf.getSize())
                        .contentType(mf.getContentType())
                        .build();

                post.addFile(f);
            }
        }

        return postRepository.save(post).getId();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CommunityPostDTO> list(int pageNo) {
        int idx = Math.max(pageNo - 1, 0);
        Pageable pageable = PageRequest.of(idx, 10, Sort.by(Sort.Direction.DESC, "createdAt"));

        return postRepository.findAll(pageable).map(p ->
                CommunityPostDTO.builder()
                        .id(p.getId())
                        .title(p.getTitle())
                        .content(null) // 목록은 내용 제외
                        // ✅ Users PK: getId() ❌ -> getUserId() ✅
                        .writerId(p.getWriter() != null ? p.getWriter().getUserId() : null)
                        // writerEmail 필드가 DTO에 있으면 아래도 같이 사용 가능
                        .writerEmail(p.getWriter() != null ? p.getWriter().getEmail() : null)
                        .createdAt(p.getCreatedAt())
                        .updatedAt(p.getUpdatedAt())
                        .build()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public CommunityPostDTO detail(Long postId) {
        if (postId == null) throw new IllegalArgumentException("postId is null");

        CommunityPost p = postRepository.findById(postId)
                .orElseThrow(() -> new EntityNotFoundException("Post not found: " + postId));

        return CommunityPostDTO.builder()
                .id(p.getId())
                .title(p.getTitle())
                .content(p.getContent())
                // ✅ Users PK: getId() ❌ -> getUserId() ✅
                .writerId(p.getWriter() != null ? p.getWriter().getUserId() : null)
                .writerEmail(p.getWriter() != null ? p.getWriter().getEmail() : null)
                .createdAt(p.getCreatedAt())
                .updatedAt(p.getUpdatedAt())
                .build();
    }
}
