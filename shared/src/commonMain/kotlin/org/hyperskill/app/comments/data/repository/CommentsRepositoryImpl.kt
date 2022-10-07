package org.hyperskill.app.comments.data.repository

import org.hyperskill.app.comments.data.source.CommentsRemoteDataSource
import org.hyperskill.app.comments.domain.model.Comment
import org.hyperskill.app.comments.domain.repository.CommentsRepository

class CommentsRepositoryImpl(
    private val commentsRemoteDataSource: CommentsRemoteDataSource
) : CommentsRepository {
    override suspend fun getHintsIDs(stepID: Long): List<Long> =
        commentsRemoteDataSource
            .getDiscussions("step", stepID, "hint", "popular", false)
            .getOrDefault(emptyList())
            .map { it.id }

    override suspend fun getCommentDetails(commentID: Long): Result<Comment> =
        commentsRemoteDataSource.getCommentDetails(commentID)

    override suspend fun abuseComment(commentID: Long) {
        commentsRemoteDataSource.createLike("abuse", "comment", commentID, 1)
    }

    override suspend fun createReaction(commentID: Long, reaction: String) {
        commentsRemoteDataSource.createReaction(commentID, reaction)
    }
}