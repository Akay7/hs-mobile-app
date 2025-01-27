package org.hyperskill.app.home.injection

import org.hyperskill.app.analytic.domain.interactor.AnalyticInteractor
import org.hyperskill.app.core.presentation.ActionDispatcherOptions
import org.hyperskill.app.core.presentation.transformState
import org.hyperskill.app.core.view.mapper.SharedDateFormatter
import org.hyperskill.app.freemium.domain.interactor.FreemiumInteractor
import org.hyperskill.app.gamification_toolbar.presentation.GamificationToolbarActionDispatcher
import org.hyperskill.app.gamification_toolbar.presentation.GamificationToolbarFeature
import org.hyperskill.app.gamification_toolbar.presentation.GamificationToolbarReducer
import org.hyperskill.app.home.domain.interactor.HomeInteractor
import org.hyperskill.app.home.presentation.HomeActionDispatcher
import org.hyperskill.app.home.presentation.HomeFeature
import org.hyperskill.app.home.presentation.HomeReducer
import org.hyperskill.app.home.view.HomeFeatureViewStateMapper
import org.hyperskill.app.magic_links.domain.interactor.UrlPathProcessor
import org.hyperskill.app.problems_limit.presentation.ProblemsLimitActionDispatcher
import org.hyperskill.app.problems_limit.presentation.ProblemsLimitFeature
import org.hyperskill.app.problems_limit.presentation.ProblemsLimitReducer
import org.hyperskill.app.profile.domain.interactor.ProfileInteractor
import org.hyperskill.app.sentry.domain.interactor.SentryInteractor
import org.hyperskill.app.step.domain.interactor.StepInteractor
import org.hyperskill.app.topics_repetitions.domain.flow.TopicRepeatedFlow
import org.hyperskill.app.topics_repetitions.domain.interactor.TopicsRepetitionsInteractor
import org.hyperskill.app.topics_to_discover_next.presentation.TopicsToDiscoverNextActionDispatcher
import org.hyperskill.app.topics_to_discover_next.presentation.TopicsToDiscoverNextFeature
import org.hyperskill.app.topics_to_discover_next.presentation.TopicsToDiscoverNextReducer
import ru.nobird.app.core.model.safeCast
import ru.nobird.app.presentation.redux.dispatcher.transform
import ru.nobird.app.presentation.redux.dispatcher.wrapWithActionDispatcher
import ru.nobird.app.presentation.redux.feature.Feature
import ru.nobird.app.presentation.redux.feature.ReduxFeature

object HomeFeatureBuilder {
    fun build(
        homeInteractor: HomeInteractor,
        profileInteractor: ProfileInteractor,
        topicsRepetitionsInteractor: TopicsRepetitionsInteractor,
        stepInteractor: StepInteractor,
        freemiumInteractor: FreemiumInteractor,
        analyticInteractor: AnalyticInteractor,
        sentryInteractor: SentryInteractor,
        urlPathProcessor: UrlPathProcessor,
        dateFormatter: SharedDateFormatter,
        topicRepeatedFlow: TopicRepeatedFlow,
        gamificationToolbarReducer: GamificationToolbarReducer,
        gamificationToolbarActionDispatcher: GamificationToolbarActionDispatcher,
        problemsLimitReducer: ProblemsLimitReducer,
        problemsLimitActionDispatcher: ProblemsLimitActionDispatcher,
        topicsToDiscoverNextReducer: TopicsToDiscoverNextReducer,
        topicsToDiscoverNextActionDispatcher: TopicsToDiscoverNextActionDispatcher
    ): Feature<HomeFeature.State, HomeFeature.Message, HomeFeature.Action> {
        val homeReducer = HomeReducer(gamificationToolbarReducer, problemsLimitReducer, topicsToDiscoverNextReducer)
        val homeActionDispatcher = HomeActionDispatcher(
            ActionDispatcherOptions(),
            homeInteractor,
            profileInteractor,
            topicsRepetitionsInteractor,
            stepInteractor,
            freemiumInteractor,
            analyticInteractor,
            sentryInteractor,
            urlPathProcessor,
            dateFormatter,
            topicRepeatedFlow
        )

        return ReduxFeature(
            HomeFeature.State(
                homeState = HomeFeature.HomeState.Idle,
                toolbarState = GamificationToolbarFeature.State.Idle,
                problemsLimitState = ProblemsLimitFeature.State.Idle,
                topicsToDiscoverNextState = TopicsToDiscoverNextFeature.State.Idle
            ),
            homeReducer
        )
            .wrapWithActionDispatcher(homeActionDispatcher)
            .wrapWithActionDispatcher(
                gamificationToolbarActionDispatcher.transform(
                    transformAction = { it.safeCast<HomeFeature.Action.GamificationToolbarAction>()?.action },
                    transformMessage = HomeFeature.Message::GamificationToolbarMessage
                )
            )
            .wrapWithActionDispatcher(
                problemsLimitActionDispatcher.transform(
                    transformAction = { it.safeCast<HomeFeature.Action.ProblemsLimitAction>()?.action },
                    transformMessage = HomeFeature.Message::ProblemsLimitMessage
                )
            )
            .wrapWithActionDispatcher(
                topicsToDiscoverNextActionDispatcher.transform(
                    transformAction = { it.safeCast<HomeFeature.Action.TopicsToDiscoverNextAction>()?.action },
                    transformMessage = HomeFeature.Message::TopicsToDiscoverNextMessage
                )
            )
            .transformState(HomeFeatureViewStateMapper::map)
    }
}