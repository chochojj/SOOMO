package com.codestates.board.service;


import com.codestates.amazonaws.service.AmazonS3ClientService;
import com.codestates.auth.LoginUtils;
import com.codestates.board.entity.Board;
import com.codestates.board.entity.BoardLike;
import com.codestates.board.repository.BoardLikeRepository;
import com.codestates.board.repository.BoardRepository;
import com.codestates.exception.BusinessLogicException;
import com.codestates.exception.ExceptionCode;
import com.codestates.member.entity.Member;
import com.codestates.member.repository.MemberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardLikeRepository boardLikeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private AmazonS3ClientService amazonS3ClientService;

    // 게시글 생성
    @Transactional
    public Board createBoard(Board board){

        Member currentMember = getCurrentMember();
        board.setMember(currentMember);
        currentMember.addBoard(board);

        if(Boolean.TRUE.equals(board.getCalendarShare()) && canCalendarShare(currentMember.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.ALREADY_POSTED_THIS_MONTH);
        }

        if(board.getCalendarShare() != null){
            board.setCalendarShare(board.getCalendarShare());
        }
        if(board.getWorkoutRecordShare() != null){
            board.setWorkoutRecordShare(board.getWorkoutRecordShare());
        }
        return boardRepository.save(board);

    }

    @Transactional
    public Board createBoard(Board board, MultipartFile image){
        Member currentMember = getCurrentMember();
        board.setMember(currentMember);
        currentMember.addBoard(board);

        if(Boolean.TRUE.equals(board.getCalendarShare()) && canCalendarShare(currentMember.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.ALREADY_POSTED_THIS_MONTH);
        }

        if(board.getCalendarShare() != null){
            board.setCalendarShare(board.getCalendarShare());
        }
        if(board.getWorkoutRecordShare() != null){
            board.setWorkoutRecordShare(board.getWorkoutRecordShare());
        }

        String fileName = board.getBoardId() + "_" + currentMember.getMemberId() + "_boardImage";
        String imageUrl = uploadImageAndGetUrl(image, fileName);
        board.setBoardImageAddress(imageUrl);

        return boardRepository.save(board);

    }


    // 게시글 수정
    @Transactional
    public Board updateBoard(Board board){

        Member currentMember = getCurrentMember();
        Board findBoard = findVerifiedBoard(board.getBoardId());


        if (!findBoard.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.BOARD_ACCESS_DENIED);
        }

        Optional.ofNullable(board.getTitle()).ifPresent(title -> findBoard.setTitle(title));
        Optional.ofNullable(board.getContent()).ifPresent(content -> findBoard.setContent(content));
        Optional.ofNullable(board.getCalendarShare()).ifPresent(calendarShare -> findBoard.setCalendarShare(calendarShare));
        Optional.ofNullable(board.getWorkoutRecordShare()).ifPresent(workoutRecordShare -> findBoard.setWorkoutRecordShare(workoutRecordShare));

        findBoard.setModifiedAt(LocalDateTime.now());
        return boardRepository.save(findBoard);
    }


    @Transactional
    public Board updateBoard(Board board, MultipartFile image){

        Member currentMember = getCurrentMember();
        Board findBoard = findVerifiedBoard(board.getBoardId());


        if (!findBoard.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.BOARD_ACCESS_DENIED);
        }

        Optional.ofNullable(board.getTitle()).ifPresent(title -> findBoard.setTitle(title));
        Optional.ofNullable(board.getContent()).ifPresent(content -> findBoard.setContent(content));
        Optional.ofNullable(board.getBoardImageAddress()).ifPresent(boardImageAddress -> findBoard.setBoardImageAddress(boardImageAddress));
        Optional.ofNullable(board.getCalendarShare()).ifPresent(calendarShare -> findBoard.setCalendarShare(calendarShare));
        Optional.ofNullable(board.getWorkoutRecordShare()).ifPresent(workoutRecordShare -> findBoard.setWorkoutRecordShare(workoutRecordShare));

        Optional.ofNullable(board.getBoardImageAddress()).ifPresent(boardImageAddress -> {
            String oldImageUrl = findBoard.getBoardImageAddress();
            if (oldImageUrl != null && !oldImageUrl.isEmpty()) {
                amazonS3ClientService.deleteFileFromS3Bucket(oldImageUrl);
            }

            String fileName = findBoard.getBoardId() + "_" + currentMember.getMemberId() + "_boardImage";
            String newImageUrl = uploadImageAndGetUrl(image, fileName);
            findBoard.setBoardImageAddress(newImageUrl);
        });



        findBoard.setModifiedAt(LocalDateTime.now());
        return boardRepository.save(findBoard);
    }

    // 게시글 삭제
    public void deleteBoard(long boardId){
        Member currentMember = getCurrentMember();
        Board board = findVerifiedBoard(boardId);

        if (!board.getMember().getMemberId().equals(currentMember.getMemberId())) {
            throw new BusinessLogicException(ExceptionCode.BOARD_ACCESS_DENIED);
        }
        if(board.getBoardImageAddress() != null && !board.getBoardImageAddress().isEmpty()){
            String fileName = board.getBoardId() + "_" + currentMember.getMemberId() + "_boardImage1";
            amazonS3ClientService.deleteFileFromS3Bucket(fileName);
        }

        boardRepository.deleteById(boardId);
    }


    // 게시글 조회
    public Board findBoard(long boardId){
        Board findBoard = findVerifiedBoard(boardId);

        findBoard.setViewCount(findBoard.getViewCount() +1);
        boardRepository.save(findBoard);

        return findBoard;
    }

    // 게시글 확인
    private Board findVerifiedBoard(long boardId){
        Optional<Board> optionalBoard = boardRepository.findById(boardId);
        Board findBoard =
                optionalBoard.orElseThrow(() ->
                        new BusinessLogicException(ExceptionCode.BOARD_NOT_FOUND));
        return findBoard;
    }


  
    //TOGGLE LIKE
    public void toggleLike(Long memberId, Long boardId){
        Board board = findVerifiedBoard(boardId);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));

        Optional<BoardLike> boardLike = boardLikeRepository.findByBoardAndMember(board, member);


        if(boardLike.isPresent()) {
            if (boardLike.get().getBoardLikeStatus() == BoardLike.BoardLikeStatus.LIKE){
                    boardLike.get().setBoardLikeStatus(BoardLike.BoardLikeStatus.DISLIKE);
            } else {
                boardLike.get().setBoardLikeStatus(BoardLike.BoardLikeStatus.LIKE);
            }
            boardLikeRepository.save(boardLike.get());
        } else{
            BoardLike newBoardLike = new BoardLike(board, member);
            newBoardLike.setBoardLikeStatus(BoardLike.BoardLikeStatus.LIKE);
            boardLikeRepository.save(newBoardLike);
        }

        board.setBoardLike(boardLikeRepository.findByBoard(board));
    }
  


    public List<Board> findBoardsSortedByLike(){
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "boardLike"));

    }

    public List<Board> findBoardsSortedByComments(){

        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "commentCount"));
    }

    public List<Board> findBoardsSortedByLatest(){
        return boardRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public List<Board> findBoardsSortedByOldest(){
        return boardRepository.findAll(Sort.by(Sort.Direction.ASC, "createdAt"));
    }


    public List<Board> getGeneralSortedBoards(String orderBy){
        if(orderBy == null || orderBy.equalsIgnoreCase("latest")){
            return findBoardsSortedByLatest();
        } else if (orderBy.equalsIgnoreCase("oldest")){
            return findBoardsSortedByOldest();
        } else if (orderBy.equalsIgnoreCase("boardLike")){
            return findBoardsSortedByLike();
        } else if (orderBy.equalsIgnoreCase("comments")){
            return findBoardsSortedByComments();
        } else {
            throw new BusinessLogicException(ExceptionCode.INVALID_ORDER_BY_PARAMETER);
        }
    }


    public List<Board> getBoardsWithCheckbox(boolean calendarShare, String orderBy) {
        return getCheckboxSortedBoards(calendarShare, orderBy);
    }

    private List<Board> getCheckboxSortedBoards(boolean checkBoxValue, String orderBy) {
        if (orderBy == null || orderBy.equalsIgnoreCase("latest")){
            return checkBoxValue ? boardRepository.findAllByCalendarShareTrue(Sort.by(Sort.Direction.DESC, "createdAt"))
                                 : boardRepository.findAllByCalendarShareFalse(Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        else if (orderBy.equalsIgnoreCase("oldest")){
            return checkBoxValue ? boardRepository.findAllByCalendarShareTrue(Sort.by(Sort.Direction.ASC, "createdAt"))
                                 : boardRepository.findAllByCalendarShareFalse(Sort.by(Sort.Direction.ASC,"createdAt"));
        }
        else if (orderBy.equalsIgnoreCase("boardLike")){
            return checkBoxValue ? boardRepository.findAllByCalendarShareTrue(Sort.by(Sort.Direction.DESC, "boardLike"))
                                 : boardRepository.findAllByCalendarShareFalse(Sort.by(Sort.Direction.DESC, "boardLike"));
        }
        else if (orderBy.equalsIgnoreCase("comments")){
            return checkBoxValue ? boardRepository.findAllByCalendarShareTrue(Sort.by(Sort.Direction.DESC, "commentCount"))
                                 : boardRepository.findAllByCalendarShareFalse(Sort.by(Sort.Direction.DESC, "commentCount"));
        }
        else {
            throw new BusinessLogicException(ExceptionCode.INVALID_ORDER_BY_PARAMETER);
        }

    }

    //현재 로그인한 회원 정보 가지고오기
    private Member getCurrentMember() {
        String email = LoginUtils.checkLogin();
        return memberRepository.findByEmail(email)
                .orElseThrow(() -> new BusinessLogicException(ExceptionCode.MEMBER_NOT_FOUND));
    }


    //보드 좋아요 개수 체크
    public int getBoardLikesCount(long boardId){
        Board board = findVerifiedBoard(boardId);
        int boardLike = board.getBoardLikeCount();
        return boardLike ;
    }

    public boolean canCalendarShare(Long memberId){
        YearMonth currentYearMonth = YearMonth.now();
        LocalDateTime startOfMonth = currentYearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = currentYearMonth.atEndOfMonth().atTime(23, 59, 59);

        boolean canPost = boardRepository.existsByMemberIdAndCalendarShareAndCreatedAtBetween(memberId, true, startOfMonth, endOfMonth);
        // 이번 달에 체크박스가 true로 설정된 게시글을 작성했다면, false반환.
        // 작성가능하면 true반환.
        return canPost;
    }


    //이미지 저장
    private String uploadImageAndGetUrl(MultipartFile image, String fileName){
        if(image != null && !image.isEmpty()){
            return amazonS3ClientService.uploadFileToS3Bucket(image, fileName);
        }
        return null;
    }


}
