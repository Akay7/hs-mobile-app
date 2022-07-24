package org.hyperskill.app.home.presentation

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.datetime.*
import org.hyperskill.app.core.presentation.ActionDispatcherOptions
import org.hyperskill.app.home.presentation.HomeFeature.Action
import org.hyperskill.app.home.presentation.HomeFeature.Message
import org.hyperskill.app.profile.domain.interactor.ProfileInteractor
import org.hyperskill.app.step.domain.interactor.StepInteractor
import org.hyperskill.app.streak.domain.interactor.StreakInteractor
import ru.nobird.app.presentation.redux.dispatcher.CoroutineActionDispatcher
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class HomeActionDispatcher(
    config: ActionDispatcherOptions,
    private val streakInteractor: StreakInteractor,
    private val profileInteractor: ProfileInteractor,
    private val stepInteractor: StepInteractor
) : CoroutineActionDispatcher<Action, Message>(config.createConfig()) {

    private var nextProblemIn = calculateNextProblemIn()

    init {
        flow {
            val delay = 1.toDuration(DurationUnit.MINUTES)

            while (true) {
                emit(nextProblemIn)
                delay(delay)
                if (nextProblemIn > 0) {
                    nextProblemIn -= delay.inWholeSeconds
                }
            }
        }
            .onEach { seconds -> onNewMessage(Message.HomeNextProblemInUpdate(seconds)) }
            .launchIn(actionScope)
    }

    override suspend fun doSuspendableAction(action: Action) {
        when (action) {
            is Action.FetchHomeScreenData -> {
                val currentProfile = profileInteractor
                    .getCurrentProfile()
                    .getOrElse {
                        onNewMessage(Message.HomeFailure)
                        return
                    }

                val problemOfDayState = getProblemOfDayState(currentProfile.dailyStep)
                    .getOrElse {
                        onNewMessage(Message.HomeFailure)
                        return
                    }

                val message = streakInteractor
                    .getStreaks(currentProfile.id)
                    .map { Message.HomeSuccess(it.firstOrNull(), problemOfDayState) }
                    .getOrElse { Message.HomeFailure }

                onNewMessage(message)
            }
        }
    }

    private suspend fun getProblemOfDayState(dailyStepId: Long?): Result<HomeFeature.ProblemOfDayState> =
        if (dailyStepId == null) {
            Result.success(HomeFeature.ProblemOfDayState.Empty)
        } else {
            nextProblemIn = calculateNextProblemIn()
            stepInteractor
                .getStep(dailyStepId)
                .map { step ->
                    if (step.isCompleted) {
                        HomeFeature.ProblemOfDayState.Solved(step, nextProblemIn)
                    } else {
                        HomeFeature.ProblemOfDayState.NeedToSolve(step, nextProblemIn)
                    }
                }
        }

    private fun calculateNextProblemIn(): Long {
        val tzNewYork = TimeZone.of("America/New_York")
        val nowInNewYork = Clock.System.now().toLocalDateTime(tzNewYork).toInstant(tzNewYork)
        val tomorrowInNewYork = nowInNewYork.plus(1, DateTimeUnit.DAY, tzNewYork).toLocalDateTime(tzNewYork)
        val startOfTomorrow = LocalDateTime(tomorrowInNewYork.year, tomorrowInNewYork.month, tomorrowInNewYork.dayOfMonth, 0, 0, 0, 0)
        return (startOfTomorrow.toInstant(tzNewYork) - nowInNewYork).inWholeSeconds
    }
}