package org.hyperskill.app.track.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.math.roundToInt

@Serializable
data class TrackProgress(
    @SerialName("id")
    val id: String,
    @SerialName("vid")
    val vid: String,
    @SerialName("user_id")
    val userId: Long,
    @SerialName("clarity")
    val clarity: Float?,
    @SerialName("fun")
    val funMeasure: Float?,
    @SerialName("usefulness")
    val usefulness: Float?,
    @SerialName("completed_projects")
    val completedProjects: List<Long>,
    @SerialName("completed_capstone_projects")
    val completedCapstoneProjects: List<Long>,
    @SerialName("applied_capstone_topics_count")
    val appliedCapstoneTopicsCount: Int,
    @SerialName("is_completed")
    val isCompleted: Boolean,
    @SerialName("learned_topics_count")
    val learnedTopicsCount: Int,
    @SerialName("skipped_topics_count")
    val skippedTopicsCount: Int,
    @SerialName("members_count")
    val membersCount: Int,
    @SerialName("seconds_to_complete")
    val secondsToComplete: Double,
    @SerialName("selected_projects")
    val selectedProjects: List<Long>,
    @SerialName("reviews_count")
    val reviewsCount: Int,
    @SerialName("latest_project")
    val latestProject: Long?,
    @SerialName("last_activity_at")
    val lastActivityAt: String?,
    @SerialName("can_issue_certificate")
    val canIssueCertificate: Boolean,
    @SerialName("certificate_url")
    val certificateUrl: String?
) {
    private val internalfunMeasure = (funMeasure ?: 0).toDouble()
    private val internalClarity = (clarity ?: 0).toDouble()
    private val internalUsefulness = (usefulness ?: 0).toDouble()

    private val avgRating = (internalfunMeasure + internalClarity + internalUsefulness) / 3

    val trackRating = (avgRating * 10).roundToInt() / 10.0
}
