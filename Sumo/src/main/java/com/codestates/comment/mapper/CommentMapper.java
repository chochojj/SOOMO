package com.codestates.comment.mapper;


import com.codestates.comment.dto.CommentPatchDto;
import com.codestates.comment.dto.CommentPostDto;
import com.codestates.comment.dto.CommentResponseDto;
import com.codestates.comment.entity.Comment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    default Comment commentPostDtoToComment(CommentPostDto commentPostDto){

        Comment comment = new Comment();
        comment.setCommentContent(commentPostDto.getCommentContent());

        return comment;
    }

    default Comment commentPatchDtoToComment(CommentPatchDto commentPatchDto){

        Comment comment = new Comment();
        comment.setCommentId(commentPatchDto.getCommentId());
        comment.setCommentContent(commentPatchDto.getCommentContent());

        return comment;
    }


    default CommentResponseDto commentToCommentResponseDto(Comment comment){

        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setCommentId(comment.getCommentId());
        commentResponseDto.setCommentContent(comment.getCommentContent());
        commentResponseDto.setCommentLikeCount(comment.getCommentLikeCount());
        return commentResponseDto;
    }

}
