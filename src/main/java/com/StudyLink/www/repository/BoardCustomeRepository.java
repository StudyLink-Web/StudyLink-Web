package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BoardCustomeRepository {
    //type과 keywoard, pageable 주고 page<board>를 리턴받고 싶음
    Page<Board> searchBoard(String type, String keyword, Pageable pageable);
}
