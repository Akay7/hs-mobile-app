import Foundation
import shared

final class AuthCredentialsViewModel: FeatureViewModel<
  AuthCredentialsFeatureState,
  AuthCredentialsFeatureMessage,
  AuthCredentialsFeatureActionViewAction
> {
    weak var moduleOutput: AuthOutputProtocol?

    private let authCredentialsErrorMapper: AuthCredentialsErrorMapper

    var formErrorMessage: String? {
        guard let errorState = self.state.formState as? AuthCredentialsFeatureFormStateError else {
            return nil
        }

        let authCredentialsError = errorState.credentialsError

        return authCredentialsErrorMapper.getAuthCredentialsErrorText(authCredentialsError: authCredentialsError)
    }

    init(authCredentialsErrorMapper: AuthCredentialsErrorMapper, feature: Presentation_reduxFeature) {
        self.authCredentialsErrorMapper = authCredentialsErrorMapper
        super.init(feature: feature)
    }

    func doFormInputChange(email: String, password: String) {
        let message = AuthCredentialsFeatureMessageAuthEditing(email: email, password: password)
        onNewMessage(message)
    }

    func doLogIn() {
        logClickedSignInEvent()
        onNewMessage(AuthCredentialsFeatureMessageSubmitFormClicked())
    }

    func doResetPassword() {
        logClickedResetPasswordEvent()

        guard let url = HyperskillURLFactory.makeResetPassword() else {
            return
        }

        WebControllerManager.shared.presentWebControllerWithURL(
            url,
            withKey: .externalLink,
            allowsSafari: true,
            backButtonStyle: .done
        )
    }

    func doCompleteAuthFlow(isNewUser: Bool) {
        moduleOutput?.handleUserAuthorized(isNewUser: isNewUser)
    }

    // MARK: Analytic

    func logViewedEvent() {
        onNewMessage(AuthCredentialsFeatureMessageAuthViewedEventMessage())
    }

    private func logClickedSignInEvent() {
        onNewMessage(AuthCredentialsFeatureMessageAuthClickedSignInEventMessage())
    }

    private func logClickedResetPasswordEvent() {
        onNewMessage(AuthCredentialsFeatureMessageAuthClickedResetPasswordEventMessage())
    }

    func logClickedContinueWithSocialEvent() {
        onNewMessage(AuthCredentialsFeatureMessageAuthClickedContinueWithSocialEventMessage())
    }
}
