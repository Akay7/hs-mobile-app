package org.hyperskill.app.main.presentation

import org.hyperskill.app.profile.domain.model.Profile

interface AppFeature {
    sealed interface State {
        object Idle : State
        object Loading : State
        object NetworkError : State
        data class Ready(val isAuthorized: Boolean) : State
    }

    sealed interface Message {
        data class Init(val forceUpdate: Boolean = false) : Message
        data class UserAuthorized(val isNewUser: Boolean) : Message
        object UserDeauthorized : Message
        data class UserAccountStatus(val profile: Profile, val isOnboardingShowed: Boolean) : Message
        object UserAccountStatusError : Message
        object OpenAuthScreen : Message
        object OpenNewUserScreen : Message
    }

    sealed interface Action {
        object DetermineUserAccountStatus : Action
        sealed interface ViewAction : Action {
            sealed interface NavigateTo : ViewAction {
                object HomeScreen : NavigateTo
                object AuthScreen : NavigateTo
                object NewUserScreen : NavigateTo
                object OnboardingScreen : NavigateTo
            }
        }
    }
}