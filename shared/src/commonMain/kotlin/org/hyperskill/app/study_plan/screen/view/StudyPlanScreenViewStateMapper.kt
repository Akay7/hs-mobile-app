package org.hyperskill.app.study_plan.screen.view

import org.hyperskill.app.SharedResources
import org.hyperskill.app.core.view.mapper.ResourceProvider
import org.hyperskill.app.study_plan.screen.presentation.StudyPlanScreenFeature
import org.hyperskill.app.study_plan.widget.view.StudyPlanWidgetViewStateMapper

internal class StudyPlanScreenViewStateMapper(
    private val studyPlanWidgetViewStateMapper: StudyPlanWidgetViewStateMapper,
    private val resourceProvider: ResourceProvider
) {
    fun map(state: StudyPlanScreenFeature.State): StudyPlanScreenViewState =
        StudyPlanScreenViewState(
            trackTitle = state.studyPlanWidgetState.track?.title?.let { title ->
                resourceProvider.getString(
                    SharedResources.strings.study_plan_track_title_template,
                    title
                )
            },
            toolbarState = state.toolbarState,
            studyPlanWidgetViewState = studyPlanWidgetViewStateMapper.map(state.studyPlanWidgetState),
            isRefreshing = state.isRefreshing
        )
}