package com.codestates.board.entity;


import com.codestates.audit.Auditable;
import com.codestates.comment.entity.Comment;
import com.codestates.member.entity.Member;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;


@NoArgsConstructor
@Getter
@Setter
@Entity
public class Board extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long boardId;

    @Column(nullable = false, length = 45)
    private String title;

    @Column(columnDefinition = "text")
    private String content;

    @Column(length = 255)
    private String boardImageAddress;

    @Column
    private int viewCount;

    @Column
    private Boolean calendarShare = false;

    @Column
    private Boolean workoutRecordShare = false;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<BoardLike> boardLike;

    @OneToMany(mappedBy = "board", cascade = CascadeType.REMOVE)
    private List<Comment> comments = new ArrayList<>();

    public void addComment(Comment comment){
        this.comments.add(comment);
        comment.setBoard(this);
    }

    public int getBoardLikeCount() {
        return (int) boardLike.stream()
                .filter(boardLike -> boardLike.getBoardLikeStatus() == BoardLike.BoardLikeStatus.LIKE)
                .count();
    }

    public int getCommentCount() {
        return comments.size();
    }

}
