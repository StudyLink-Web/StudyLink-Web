package com.StudyLink.www.service;

import com.StudyLink.www.dto.BoardDTO;
import com.StudyLink.www.dto.BoardFileDTO;
import com.StudyLink.www.dto.FileDTO;
import com.StudyLink.www.entity.Board;
import com.StudyLink.www.entity.File;
import com.StudyLink.www.repository.BoardRepository;
import com.StudyLink.www.repository.FileRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BoardServiceImpl implements BoardService {
    private final BoardRepository boardRepository;
    private final FileRepository fileRepository;

    @Transactional
    @Override
    public Long insert(BoardFileDTO boardFileDTO) {
        BoardDTO boardDTO = boardFileDTO.getBoardDTO();
        List<FileDTO> fileDTOList = boardFileDTO.getFileList();
        if(fileDTOList != null){
            boardDTO.setFileQty(fileDTOList.size());
        }

        Long bno = boardRepository.save(convertDtoToEntity(boardDTO)).getBno();

        if(bno > 0 &&  fileDTOList != null){
            for(FileDTO fileDTO : fileDTOList){
                fileDTO.setBno(bno);
                bno = fileRepository.save(convertDtoToEntity(fileDTO)).getBno();
            }
        }
        return bno;
    }

    @Transactional
    @Override
    public long fileRemove(String uuid) {
        Optional<File> optionalFile = fileRepository.findById(uuid);
        if(optionalFile.isPresent()){
            long bno = optionalFile.get().getBno();
            fileRepository.deleteById(uuid);
            // 삭제한 파일 갯수 차감
            Board board = boardRepository.findById(bno)
                    .orElseThrow(()-> new EntityNotFoundException());
            board.setFileQty(board.getFileQty()-1);

            return bno;
        }
        return 0;
    }

    @Override
    public FileDTO getFile(String uuid) {
        Optional<File> optionalFile = fileRepository.findById(uuid);
        if(optionalFile.isPresent()){
            return convertEntityToDto(optionalFile.get());
        }
        return null;
    }

    @Override
    public List<FileDTO> getTodayFileList(String today) {
        List<File> fileList = fileRepository.findBySaveDir(today);
        if(fileList.isEmpty() || fileList != null){
            return fileList.stream()
                    .map(this :: convertEntityToDto)
                    .toList();
        }
        return null;
    }

    @Override
    public Long insert(BoardDTO boardDTO) {
        // save() : 저장
        // 저장 객체는 Entity (Board)
        // DTO => Entity 로 변환
        Board board = convertDtoToEntity(boardDTO); // 변환 후
        Long bno = boardRepository.save(board).getBno(); // 저장

        return bno;
    }


    @Override
    public Page<BoardDTO> getList(int pageNo, String type, String keyword) {
        // limit 시작번지, 개수 => 번지는 0부터 시작
        // pageNo = 1 => limit 10,10  => pageNo -1  => 0부터 인식되게 처리
        Pageable pageable = PageRequest.of(pageNo-1, 10, Sort.by("bno").descending());
        // type과 keyword에 일치하는 리스트를 리턴받고 싶음

        Page<Board> pageList = boardRepository.searchBoard(type, keyword, pageable);
        Page<BoardDTO> boardDTOPage = pageList.map(this::convertEntityToDto);
        return boardDTOPage;
    }

    @Transactional
    @Override
    public BoardFileDTO getDetail(long bno) {
        Optional<Board> optional = boardRepository.findById(bno);
        if(optional.isPresent()){
            Board board = optional.get();
            board.setReadCount(board.getReadCount()+1);

            BoardDTO boardDTO = convertEntityToDto(board);

            // file 리스트 가져오기  bno에 일치하는 파일 가져오기
            List<File> fileList = fileRepository.findByBno(bno);
            List<FileDTO> fileDTOList = fileList.stream()
                    .map(this::convertEntityToDto)
                    .toList();
            BoardFileDTO boardFileDTO = new BoardFileDTO(boardDTO, fileDTOList);
            return boardFileDTO;
        }
        return null;
    }

    @Transactional
    @Override
    public Long modify(BoardFileDTO boardFileDTO) {
        Board board = boardRepository.findById(boardFileDTO.getBoardDTO().getBno())
                .orElseThrow(()-> new EntityNotFoundException("존재하지 않는 게시글입니다."));
        board.setTitle(boardFileDTO.getBoardDTO().getTitle());
        board.setContent(boardFileDTO.getBoardDTO().getContent());
        // readcount
        boardReadCountUpdate(board, -1);

        if(boardFileDTO.getFileList() != null){
            // fileList가 null이 아니면 파일 갯수 저장
            board.setFileQty(boardFileDTO.getFileList().size());

            for(FileDTO fileDTO : boardFileDTO.getFileList()){
                fileDTO.setBno(board.getBno());
                fileRepository.save(convertDtoToEntity(fileDTO));
            }
        }
        return board.getBno();
    }


    @Override
    public void remove(long bno) {
        /*deleteById(bno)*/
        boardRepository.deleteById(bno);
    }



    private void boardReadCountUpdate(Board board, int i){
        // readCount update method
        board.setReadCount(board.getReadCount()+i);
        boardRepository.save(board);
    }
}
