package com.avoworld.repository;

import com.avoworld.entity.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.time.LocalDateTime;

@Repository
public class CommentRepository {
    private final JdbcTemplate jdbcTemplate;

    public CommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Comment> commentRowMapper = (rs, rowNum) -> {
        Comment comment = new Comment();
        comment.setId(rs.getInt("id"));
        comment.setPostId(rs.getInt("post_id"));
        comment.setUserId(rs.getInt("user_id"));
        comment.setCommentContent(rs.getString("comment_content"));
        comment.setCreateAt(rs.getTimestamp("create_at").toLocalDateTime());
        comment.setEditedCommentContent(rs.getString("edited_comment_content"));
        comment.setUpdateAt(rs.getTimestamp("update_at") != null ? rs.getTimestamp("update_at").toLocalDateTime() : null);
        return comment;
    };

    public List<Comment> findByPostId(int postId) {
        String sql = "SELECT c.id, c.post_id, c.comment_content, c.edited_comment_content, c.user_id, c.create_at, u.nickname, u.profile_picture, u.update_at " +
                "FROM post_comment c " +
                "LEFT JOIN community_user u ON c.user_id = u.user_id " +
                "WHERE c.post_id = ?";
        return jdbcTemplate.query(sql, commentRowMapper, postId);
    }

    public Comment findById(int commentId) {
        String sql = "SELECT * FROM post_comment WHERE id = ?";
        return jdbcTemplate.queryForObject(sql, commentRowMapper, commentId);
    }

    public void deleteById(int commentId) {
        String sql = "DELETE FROM post_comment WHERE id = ?";
        jdbcTemplate.update(sql, commentId);
    }

    public void save(Comment comment) {
        String sql = "INSERT INTO post_comment (post_id, comment_content, user_id, create_at) VALUES (?, ?, ?, ?)";
        LocalDateTime now = LocalDateTime.now();
        jdbcTemplate.update(sql, comment.getPostId(), comment.getCommentContent(), comment.getUserId(), now);
    }

    public void update(Comment comment) {
        String sql = "UPDATE post_comment SET comment_content = ?, edited_comment_content = ?, update_at = ? WHERE id = ?";
        jdbcTemplate.update(sql, comment.getCommentContent(), comment.getEditedCommentContent(),  LocalDateTime.now(), comment.getId());
    }
}



