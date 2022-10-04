package org.hyperskill.app.profile.presentation

import org.hyperskill.app.profile.domain.analytic.ProfileClickedDailyStudyRemindsHyperskillAnalyticEvent
import org.hyperskill.app.profile.domain.analytic.ProfileClickedDailyStudyRemindsTimeHyperskillAnalyticEvent
import org.hyperskill.app.profile.domain.analytic.ProfileClickedSettingsHyperskillAnalyticEvent
import org.hyperskill.app.profile.domain.analytic.ProfileClickedViewFullProfileHyperskillAnalyticEvent
import org.hyperskill.app.profile.domain.analytic.ProfileViewedHyperskillAnalyticEvent
import org.hyperskill.app.profile.presentation.ProfileFeature.Action
import org.hyperskill.app.profile.presentation.ProfileFeature.Message
import org.hyperskill.app.profile.presentation.ProfileFeature.State
import ru.nobird.app.presentation.redux.reducer.StateReducer

class ProfileReducer : StateReducer<State, Message, Action> {
    override fun reduce(state: State, message: Message): Pair<State, Set<Action>> =
        when (message) {
            is Message.Init -> {
                if (state is State.Idle ||
                    (message.forceUpdate && (state is State.Content || state is State.Error))
                ) {
                    if (message.isInitCurrent) {
                        State.Loading to setOf(Action.FetchCurrentProfile)
                    } else {
                        State.Loading to setOf(Action.FetchProfile(message.profileId!!))
                    }
                } else {
                    null
                }
            }
            is Message.ProfileLoaded.Success ->
                State.Content(message.profile, message.streak) to emptySet()
            is Message.ProfileLoaded.Error ->
                State.Error to emptySet()
            is Message.StepSolved -> {
                if (state is State.Content) {
                    state to setOf(Action.UpdateStreakInfo(state.streak))
                } else {
                    null
                }
            }
            is Message.ViewedEventMessage ->
                state to setOf(Action.LogAnalyticEvent(ProfileViewedHyperskillAnalyticEvent()))
            is Message.ClickedSettingsEventMessage ->
                state to setOf(Action.LogAnalyticEvent(ProfileClickedSettingsHyperskillAnalyticEvent()))
            is Message.ClickedDailyStudyRemindsEventMessage ->
                state to setOf(Action.LogAnalyticEvent(ProfileClickedDailyStudyRemindsHyperskillAnalyticEvent(message.isEnabled)))
            is Message.ClickedDailyStudyRemindsTimeEventMessage ->
                state to setOf(Action.LogAnalyticEvent(ProfileClickedDailyStudyRemindsTimeHyperskillAnalyticEvent()))
            is Message.ClickedViewFullProfileEventMessage ->
                state to setOf(Action.LogAnalyticEvent(ProfileClickedViewFullProfileHyperskillAnalyticEvent()))
        } ?: (state to emptySet())
}