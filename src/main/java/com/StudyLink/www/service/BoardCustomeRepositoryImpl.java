package com.StudyLink.www.service;

import com.StudyLink.www.entity.Board;
import com.StudyLink.www.repository.BoardCustomeRepository;
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
public class BoardCustomeRepositoryImpl implements BoardCustomeRepository {

    private final JPAQueryFactory queryFactory;

    public BoardCustomeRepositoryImpl(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    @Override
    public Page<Board> searchBoard(String type, String keyword, Pageable pageable) {

        BooleanExpression condition = null;

        if (type != null && !type.isBlank() && keyword != null && !keyword.isBlank()) {

            String[] typeArr = type.split("");
            for (String t : typeArr) {
                switch (t) {
                    case "t":
                        condition = (condition == null)
                                ? board.title.containsIgnoreCase(keyword)
                                : condition.or(board.title.containsIgnoreCase(keyword));
                        break;

                    // 작성자(FK) 검색: keyword가 숫자일 때만 userId(Long) 비교
                    // 만약 너 UI에서 계속 w를 쓰고 있다면 case "w"도 같이 처리
                    case "u":
                    case "w":
                        try {
                            Long uid = Long.valueOf(keyword);
                            BooleanExpression userCond = board.userId.eq(uid);
                            condition = (condition == null) ? userCond : condition.or(userCond);
                        } catch (NumberFormatException ignored) {
                            // userId가 Long(FK)이므로 숫자가 아니면 조건 추가 안 함
                        }
                        break;

                    case "c":
                        condition = (condition == null)
                                ? board.content.containsIgnoreCase(keyword)
                                : condition.or(board.content.containsIgnoreCase(keyword));
                        break;

                    default:
                        break;
                }
            }
        }

        List<Board> result = queryFactory
                .selectFrom(board)
                .where(condition)
                .orderBy(board.postId.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(board.count())
                .from(board)
                .where(condition)
                .fetchOne();

        long totalCount = (total == null) ? 0L : total;

        return new PageImpl<>(result, pageable, totalCount);
    }
}
