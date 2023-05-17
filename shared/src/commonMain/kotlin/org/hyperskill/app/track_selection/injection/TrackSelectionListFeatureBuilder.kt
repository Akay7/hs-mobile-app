package org.hyperskill.app.track_selection.injection

import org.hyperskill.app.analytic.domain.interactor.AnalyticInteractor
import org.hyperskill.app.core.presentation.ActionDispatcherOptions
import org.hyperskill.app.core.presentation.transformState
import org.hyperskill.app.profile.domain.interactor.ProfileInteractor
import org.hyperskill.app.progresses.domain.interactor.ProgressesInteractor
import org.hyperskill.app.sentry.domain.interactor.SentryInteractor
import org.hyperskill.app.study_plan.domain.repository.CurrentStudyPlanStateRepository
import org.hyperskill.app.track.domain.interactor.TrackInteractor
import org.hyperskill.app.track_selection.presentation.TrackSelectionListActionDispatcher
import org.hyperskill.app.track_selection.presentation.TrackSelectionListFeature
import org.hyperskill.app.track_selection.presentation.TrackSelectionListFeature.Action
import org.hyperskill.app.track_selection.presentation.TrackSelectionListFeature.Message
import org.hyperskill.app.track_selection.presentation.TrackSelectionListReducer
import org.hyperskill.app.track_selection.view.TrackSelectionListViewStateMapper
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.Feature
import ru.nobird.app.presentation.redux.feature.ReduxFeature

internal object TrackSelectionListFeatureBuilder {
    fun build(
        analyticInteractor: AnalyticInteractor,
        sentryInteractor: SentryInteractor,
        trackInteractor: TrackInteractor,
        progressesInteractor: ProgressesInteractor,
        profileInteractor: ProfileInteractor,
        currentStudyPlanStateRepository: CurrentStudyPlanStateRepository,
        trackListViewStateMapper: TrackSelectionListViewStateMapper
    ): Feature<TrackSelectionListFeature.ViewState, Message, Action> {
        val trackSelectionListReducer = TrackSelectionListReducer()
        val trackSelectionListActionDispatcher = TrackSelectionListActionDispatcher(
            ActionDispatcherOptions(),
            analyticInteractor,
            sentryInteractor,
            trackInteractor,
            progressesInteractor,
            profileInteractor,
            currentStudyPlanStateRepository
        )

        return ReduxFeature(TrackSelectionListFeature.State.Idle, trackSelectionListReducer)
            .wrapWithActionDispatcher(trackSelectionListActionDispatcher)
            .transformState(trackListViewStateMapper::map)
    }
}