package org.hyperskill.app.profile.injection

import org.hyperskill.app.analytic.domain.interactor.AnalyticInteractor
import org.hyperskill.app.core.presentation.ActionDispatcherOptions
import org.hyperskill.app.magic_links.domain.interactor.UrlPathProcessor
import org.hyperskill.app.profile.domain.interactor.ProfileInteractor
import org.hyperskill.app.profile.presentation.ProfileActionDispatcher
import org.hyperskill.app.profile.presentation.ProfileFeature.Action
import org.hyperskill.app.profile.presentation.ProfileFeature.Message
import org.hyperskill.app.profile.presentation.ProfileFeature.State
import org.hyperskill.app.profile.presentation.ProfileReducer
import org.hyperskill.app.sentry.domain.interactor.SentryInteractor
import org.hyperskill.app.streak.domain.interactor.StreakInteractor
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.Feature
import ru.nobird.app.presentation.redux.feature.ReduxFeature

object ProfileFeatureBuilder {
    fun build(
        profileInteractor: ProfileInteractor,
        streakInteractor: StreakInteractor,
        analyticInteractor: AnalyticInteractor,
        sentryInteractor: SentryInteractor,
        urlPathProcessor: UrlPathProcessor
    ): Feature<State, Message, Action> {
        val profileReducer = ProfileReducer()
        val profileActionDispatcher = ProfileActionDispatcher(
            ActionDispatcherOptions(),
            profileInteractor,
            streakInteractor,
            analyticInteractor,
            sentryInteractor,
            urlPathProcessor
        )

        return ReduxFeature(State.Idle, profileReducer)
            .wrapWithActionDispatcher(profileActionDispatcher)
    }
}