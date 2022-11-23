package org.hyperskill.app.profile_settings.presentation

import org.hyperskill.app.analytic.domain.model.AnalyticEvent
import org.hyperskill.app.core.domain.url.HyperskillUrlPath
import org.hyperskill.app.profile_settings.domain.model.FeedbackEmailData
import org.hyperskill.app.profile_settings.domain.model.ProfileSettings
import org.hyperskill.app.profile_settings.domain.model.Theme

interface ProfileSettingsFeature {
    sealed interface State {
        object Idle : State
        object Loading : State

        /**
         * @property isLinkLoadingShown A boolean flag that indicates about magic link loading.
         */
        data class Content(
            val profileSettings: ProfileSettings,
            val isLinkLoadingShown: Boolean = false
        ) : State

        object Error : State
    }

    sealed interface Message {
        data class InitMessage(val forceUpdate: Boolean = false) : Message
        data class ProfileSettingsSuccess(val profileSettings: ProfileSettings) : Message
        object ProfileSettingsError : Message
        data class ThemeChanged(val theme: Theme) : Message
        object SignOutConfirmed : Message
        object DismissScreen : Message

        object ClickedSendFeedback : Message
        data class FeedbackEmailDataPrepared(val feedbackEmailData: FeedbackEmailData) : Message

        data class LinkReceived(val url: String) : Message
        object LinkReceiveFailed : Message

        /**
         * Analytic
         */
        object ViewedEventMessage : Message

        object ClickedDoneEventMessage : Message
        object ClickedThemeEventMessage : Message
        object ClickedTermsOfServiceEventMessage : Message
        object ClickedPrivacyPolicyEventMessage : Message
        object ClickedReportProblemEventMessage : Message

        object ClickedSignOutEventMessage : Message
        object SignOutNoticeShownEventMessage : Message
        data class SignOutNoticeHiddenEventMessage(val isConfirmed: Boolean) : Message

        object ClickedDeleteAccountEventMessage : Message
        object DeleteAccountNoticeShownEventMessage : Message
        data class DeleteAccountNoticeHiddenEventMessage(val isConfirmed: Boolean) : Message
    }

    sealed interface Action {
        object FetchProfileSettings : Action
        data class ChangeTheme(val theme: Theme) : Action
        object SignOut : Action
        object PrepareFeedbackEmailData : Action

        data class LogAnalyticEvent(val analyticEvent: AnalyticEvent) : Action

        data class GetLink(val path: HyperskillUrlPath) : Action

        sealed interface ViewAction : Action {
            data class SendFeedback(val feedbackEmailData: FeedbackEmailData) : ViewAction

            data class FollowLink(val url: String) : ViewAction

            object ShowFollowLinkError : ViewAction

            sealed interface NavigateTo : ViewAction {
                object ParentScreen : NavigateTo
            }
        }
    }
}