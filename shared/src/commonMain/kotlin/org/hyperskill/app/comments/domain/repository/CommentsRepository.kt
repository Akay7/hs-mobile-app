package org.hyperskill.app.comments.domain.repository

import org.hyperskill.app.comments.domain.model.Comment

interface CommentsRepository {
    suspend fun getHintsIDs(stepID: Long): List<Long>
    suspend fun getCommentDetails(commentID: Long): Result<Comment>
    suspend fun abuseComment(commentID: Long)
    suspend fun createReaction(commentID: Long, reaction: String)
}