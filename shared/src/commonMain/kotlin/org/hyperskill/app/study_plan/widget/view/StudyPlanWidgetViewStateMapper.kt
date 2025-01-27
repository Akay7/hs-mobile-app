package org.hyperskill.app.study_plan.widget.view

import kotlin.math.roundToLong
import org.hyperskill.app.core.view.mapper.SharedDateFormatter
import org.hyperskill.app.learning_activities.domain.model.LearningActivity
import org.hyperskill.app.learning_activities.domain.model.LearningActivityState
import org.hyperskill.app.study_plan.widget.presentation.StudyPlanWidgetFeature
import org.hyperskill.app.study_plan.widget.presentation.getCurrentActivity
import org.hyperskill.app.study_plan.widget.presentation.getCurrentSection
import org.hyperskill.app.study_plan.widget.presentation.getSectionActivities
import org.hyperskill.app.study_plan.widget.view.StudyPlanWidgetViewState.SectionContent

class StudyPlanWidgetViewStateMapper(private val dateFormatter: SharedDateFormatter) {
    fun map(state: StudyPlanWidgetFeature.State): StudyPlanWidgetViewState =
        when (state.sectionsStatus) {
            StudyPlanWidgetFeature.ContentStatus.IDLE -> StudyPlanWidgetViewState.Idle
            StudyPlanWidgetFeature.ContentStatus.LOADING -> StudyPlanWidgetViewState.Loading
            StudyPlanWidgetFeature.ContentStatus.ERROR -> StudyPlanWidgetViewState.Error
            StudyPlanWidgetFeature.ContentStatus.LOADED -> getLoadedWidgetContent(state)
        }

    private fun getLoadedWidgetContent(state: StudyPlanWidgetFeature.State): StudyPlanWidgetViewState.Content {
        val currentSectionId = state.getCurrentSection()?.id
        val currentActivityId = state.getCurrentActivity()?.id

        return StudyPlanWidgetViewState.Content(
            sections = state.studyPlan?.sections?.mapNotNull { sectionId ->
                val sectionInfo = state.studyPlanSections[sectionId] ?: return@mapNotNull null
                val section = sectionInfo.studyPlanSection

                val shouldShowSectionStatistics = currentSectionId == section.id || sectionInfo.isExpanded

                StudyPlanWidgetViewState.Section(
                    id = section.id,
                    title = section.title,
                    subtitle = section.subtitle.takeIf { it.isNotEmpty() },
                    isCurrent = section.id == currentSectionId,
                    content = getSectionContent(
                        state = state,
                        sectionInfo = sectionInfo,
                        currentActivityId = currentActivityId
                    ),
                    formattedTopicsCount = if (shouldShowSectionStatistics) {
                        formatTopicsCount(
                            completedTopicsCount = section.completedTopicsCount,
                            topicsCount = section.topicsCount
                        )
                    } else {
                        null
                    },
                    formattedTimeToComplete = if (shouldShowSectionStatistics) {
                        section.secondsToComplete
                            ?.roundToLong()
                            ?.let(dateFormatter::formatHoursWithMinutesCount)
                            ?.takeIf { it.isNotEmpty() }
                    } else {
                        null
                    }
                )
            } ?: emptyList()
        )
    }

    private fun getSectionContent(
        sectionInfo: StudyPlanWidgetFeature.StudyPlanSectionInfo,
        state: StudyPlanWidgetFeature.State,
        currentActivityId: Long?
    ): SectionContent =
        if (sectionInfo.isExpanded) {
            when (sectionInfo.contentStatus) {
                StudyPlanWidgetFeature.ContentStatus.IDLE -> SectionContent.Collapsed
                StudyPlanWidgetFeature.ContentStatus.LOADING -> {
                    val activities = state.getSectionActivities(sectionInfo.studyPlanSection.id)
                    if (activities.isEmpty()) {
                        SectionContent.Loading
                    } else {
                        getContent(activities, currentActivityId)
                    }
                }
                StudyPlanWidgetFeature.ContentStatus.ERROR -> SectionContent.Error
                StudyPlanWidgetFeature.ContentStatus.LOADED -> {
                    val activities = state.getSectionActivities(sectionInfo.studyPlanSection.id)
                    if (activities.isNotEmpty()) {
                        getContent(activities, currentActivityId)
                    } else {
                        SectionContent.Error
                    }
                }
            }
        } else {
            SectionContent.Collapsed
        }

    private fun getContent(activities: List<LearningActivity>, currentActivityId: Long?): SectionContent.Content =
        SectionContent.Content(
            sectionItems = activities.map { activity ->
                StudyPlanWidgetViewState.SectionItem(
                    id = activity.id,
                    title = formatActivityTitle(activity),
                    subtitle = formatActivitySubtitle(activity),
                    state = when (activity.state) {
                        LearningActivityState.TODO -> if (activity.id == currentActivityId) {
                            StudyPlanWidgetViewState.SectionItemState.NEXT
                        } else {
                            StudyPlanWidgetViewState.SectionItemState.LOCKED
                        }
                        LearningActivityState.SKIPPED -> StudyPlanWidgetViewState.SectionItemState.SKIPPED
                        LearningActivityState.COMPLETED -> StudyPlanWidgetViewState.SectionItemState.COMPLETED
                        null -> StudyPlanWidgetViewState.SectionItemState.IDLE
                    },
                    isIdeRequired = activity.isIdeRequired,
                    progress = null, // TODO: ALTAPPS-713 add data with new activities API
                    formattedProgress = null, // TODO: ALTAPPS-713 add data with new activities API
                    hypercoinsAward = activity.hypercoinsAward.takeIf { it > 0 }
                )
            }
        )

    private fun formatActivityTitle(activity: LearningActivity): String {
        val defaultTitle = activity.title.ifBlank { activity.id.toString() }
        return if (activity.description.isNullOrBlank()) {
            defaultTitle
        } else {
            activity.description
        }
    }

    private fun formatActivitySubtitle(activity: LearningActivity): String? =
        if (activity.description.isNullOrBlank()) {
            null
        } else {
            activity.title.ifBlank { null }
        }

    private fun formatTopicsCount(completedTopicsCount: Int, topicsCount: Int): String? =
        if (topicsCount > 0) {
            "$completedTopicsCount / $topicsCount"
        } else {
            null
        }
}