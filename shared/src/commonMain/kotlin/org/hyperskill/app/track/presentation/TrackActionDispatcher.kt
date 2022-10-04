package org.hyperskill.app.track.presentation

import kotlinx.coroutines.async
import org.hyperskill.app.analytic.domain.interactor.AnalyticInteractor
import org.hyperskill.app.core.domain.DataSourceType
import org.hyperskill.app.core.presentation.ActionDispatcherOptions
import org.hyperskill.app.profile.domain.interactor.ProfileInteractor
import org.hyperskill.app.track.domain.interactor.TrackInteractor
import org.hyperskill.app.track.presentation.TrackFeature.Action
import org.hyperskill.app.track.presentation.TrackFeature.Message
import ru.nobird.app.presentation.redux.dispatcher.CoroutineActionDispatcher

class TrackActionDispatcher(
    config: ActionDispatcherOptions,
    private val trackInteractor: TrackInteractor,
    private val profileInteractor: ProfileInteractor,
    private val analyticInteractor: AnalyticInteractor
) : CoroutineActionDispatcher<Action, Message>(config.createConfig()) {
    override suspend fun doSuspendableAction(action: Action) {
        when (action) {
            is Action.FetchTrack -> {
                val trackId = profileInteractor
                    .getCurrentProfile(sourceType = DataSourceType.CACHE)
                    .map { profile -> profile.trackId }
                    .getOrElse {
                        Message.TrackError(message = it.message ?: "")
                        return
                    }

                if (trackId == null) {
                    Message.TrackError("")
                    return
                }

                val trackResult = actionScope.async {
                    trackInteractor.getTrack(trackId)
                }
                val trackProgressResult = actionScope.async {
                    trackInteractor.getTrackProgress(trackId)
                }
                val studyPlanResult = actionScope.async {
                    trackInteractor.getStudyPlanByTrackId(trackId)
                }

                val track = trackResult.await().getOrElse {
                    onNewMessage(
                        Message.TrackError(message = it.message ?: "")
                    )
                    return
                }
                val trackProgress = trackProgressResult.await().getOrElse {
                    onNewMessage(
                        Message.TrackError(message = it.message ?: "")
                    )
                    return
                }
                val studyPlan = studyPlanResult.await().getOrNull()

                onNewMessage(
                    Message.TrackSuccess(track, trackProgress, studyPlan)
                )
            }
            is Action.LogAnalyticEvent ->
                analyticInteractor.logEvent(action.analyticEvent)
        }
    }
}