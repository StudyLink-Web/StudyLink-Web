package com.StudyLink.www.repository;

import com.StudyLink.www.entity.Board;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static com.StudyLink.www.entity.QBoard.board;

@Slf4j
public class BoardCustomeRepositoryImpl implements BoardCustomeRepository{

    private final JPAQueryFactory queryFactory;
    public BoardCustomeRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }


    @Override
    public Page<Board> searchBoard(String type, String keyword, Pageable pageable) {
        /* select * from board
         *  where title like '%aaa%' or writer like '%aaa%'
         * */
        BooleanExpression condition = null;

        //동적검색 조건 추가
        if(type != null && keyword != null) {
            //타입이 여러개 들어올 경우 배열로 처리
            String[] typeArr = type.split("");
            for(String t : typeArr) {
                switch (t) {
                    case "t":
                        condition = (condition == null) ? board.title.containsIgnoreCase(keyword) :
                                condition.or(board.title.containsIgnoreCase(keyword));
                        break;
                    case "w":
                        condition = (condition == null) ? board.writer.containsIgnoreCase(keyword) :
                                condition.or(board.writer.containsIgnoreCase(keyword));
                        break;
                    case "c":
                        condition = (condition == null) ? board.content.containsIgnoreCase(keyword) :
                                condition.or(board.content.containsIgnoreCase(keyword));
                        break;
                    default :
                        break;
                }
            }

        }
        // 값 리턴 쿼리작성, 페이징 적용
        List<Board> result = queryFactory
                .selectFrom(board)
                .where(condition)
                .orderBy(board.bno.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        long total = queryFactory.selectFrom(board).where(condition).fetch().size();
        return new PageImpl<>(result, pageable, total);
    }
}

