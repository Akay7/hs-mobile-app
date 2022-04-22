package org.hyperskill.app.auth.presentation

import org.hyperskill.app.auth.domain.model.SocialAuthProvider

interface AuthSocialFeature {
    sealed interface State {
        object Idle : State
        object Loading : State
        object Error : State
        object Authenticated : State
    }

    sealed interface Message {
        data class AuthWithSocial(val authCode: String, val socialAuthProvider: SocialAuthProvider) : Message
        object AuthSuccess : Message
        data class AuthFailure(val errorMessage: String) : Message
    }

    sealed interface Action {
        data class AuthWithSocial(val authCode: String, val socialAuthProvider: SocialAuthProvider) : Action
        sealed interface ViewAction : Action {
            object NavigateToHomeScreen : ViewAction
            data class ShowAuthError(val errorMessage: String) : ViewAction
        }
    }
}