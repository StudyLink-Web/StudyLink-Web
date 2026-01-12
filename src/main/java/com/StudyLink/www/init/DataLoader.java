package com.StudyLink.www.init;

import com.StudyLink.www.entity.Subject;
import com.StudyLink.www.repository.SubjectRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements CommandLineRunner {
    // subject 초기 데이터 삽입 용
    // 서버 실행시 자동 실행

    private final SubjectRepository subjectRepository;

    public DataLoader(SubjectRepository subjectRepository) {
        this.subjectRepository = subjectRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 초기 데이터 세팅 필요시 여기에 추가

        if(subjectRepository.count() == 0) {
            subjectRepository.save(Subject.builder().name("고등국어").build());
            subjectRepository.save(Subject.builder().name("독서").build());
            subjectRepository.save(Subject.builder().name("문학").build());
            subjectRepository.save(Subject.builder().name("화법과작문").build());
            subjectRepository.save(Subject.builder().name("언어와매체").build());
            subjectRepository.save(Subject.builder().name("영어").build());
            subjectRepository.save(Subject.builder().name("한국사").build());
            subjectRepository.save(Subject.builder().name("고등수학").build());
            subjectRepository.save(Subject.builder().name("수학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("수학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("미적분").build());
            subjectRepository.save(Subject.builder().name("확률과통계").build());
            subjectRepository.save(Subject.builder().name("기하").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("물리학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("화학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("화학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("생명과학Ⅱ").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅰ").build());
            subjectRepository.save(Subject.builder().name("지구과학Ⅱ").build());
        }
    }
}