package org.hyperskill.app.placeholder_new_user.presentation

import org.hyperskill.app.analytic.domain.model.AnalyticEvent

interface PlaceholderNewUserFeature {
    sealed interface State {
        object Content : State
    }

    sealed interface Message {
        object PlaceholderSignInTappedMessage : Message
        object OpenAuthScreen : Message

        /**
         * Analytic
         */
        object ViewedEventMessage : Message
        object ClickedContinueEventMessage : Message
    }

    sealed interface Action {
        object Logout : Action

        sealed interface ViewAction : Action {
            sealed interface NavigateTo : ViewAction {
                object AuthScreen : NavigateTo
            }
        }

        /**
         * Analytic
         */
        data class LogAnalyticEvent(val analyticEvent: AnalyticEvent) : Action
    }
}