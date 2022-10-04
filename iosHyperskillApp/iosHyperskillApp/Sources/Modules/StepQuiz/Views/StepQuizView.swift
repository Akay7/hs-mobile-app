import shared
import SwiftUI

extension StepQuizView {
    struct Appearance {
        let interItemSpacing = LayoutInsets.defaultInset

        let stepTextFont = UIFont.preferredFont(forTextStyle: .subheadline)
        let stepTextColor = UIColor.primaryText
    }
}

struct StepQuizView: View {
    private(set) var appearance = Appearance()

    @StateObject var viewModel: StepQuizViewModel

    @State private var isPresentingDailyStudyRemindersPermissionAlert = false

    @Environment(\.presentationMode) private var presentationMode

    var body: some View {
        ZStack {
            UIViewControllerEventsWrapper(onViewDidAppear: viewModel.logViewedEvent)

            buildBody()
        }
        .navigationBarTitleDisplayMode(.inline)
        .onAppear {
            viewModel.startListening()
            viewModel.onViewAction = handleViewAction(_:)

            if viewModel.state is StepQuizFeatureStateIdle {
                viewModel.loadAttempt()
            }
        }
        .onDisappear(perform: viewModel.stopListening)
    }

    // MARK: Private API

    @ViewBuilder
    private func buildBody() -> some View {
        if viewModel.state is StepQuizFeatureStateNetworkError {
            PlaceholderView(
                configuration: .networkError {
                    viewModel.loadAttempt(forceUpdate: true)
                }
            )
        } else {
            let viewData = viewModel.makeViewData()

            ScrollView {
                LazyVStack(alignment: .leading, spacing: appearance.interItemSpacing) {
                    StepQuizStatsView(text: viewData.formattedStats)

                    LatexView(
                        text: .constant(viewData.stepText),
                        configuration: .init(
                            appearance: .init(labelFont: appearance.stepTextFont),
                            contentProcessor: ContentProcessor(
                                injections: ContentProcessor.defaultInjections + [
                                    StepStylesInjection(),
                                    FontInjection(font: appearance.stepTextFont),
                                    TextColorInjection(dynamicColor: appearance.stepTextColor)
                                ]
                            )
                        )
                    )

                    buildQuizContent(
                        state: viewModel.state,
                        step: viewModel.step,
                        stepQuizName: viewData.quizName,
                        stepBlockName: viewData.stepBlockName,
                        hintText: viewData.hintText
                    )
                    .alert(isPresented: $isPresentingDailyStudyRemindersPermissionAlert) {
                        Alert(
                            title: Text(Strings.StepQuiz.afterDailyStepCompletedDialogTitle),
                            message: Text(Strings.StepQuiz.afterDailyStepCompletedDialogText),
                            primaryButton: .default(
                                Text(Strings.General.ok),
                                action: {
                                    viewModel.handleDailyStudyRemindersPermissionRequestResult(isGranted: true)
                                }
                            ),
                            secondaryButton: .cancel(
                                Text(Strings.General.later),
                                action: {
                                    viewModel.handleDailyStudyRemindersPermissionRequestResult(isGranted: false)
                                }
                            )
                        )
                    }
                }
                .padding()
            }
        }
    }

    @ViewBuilder
    private func buildQuizContent(
        state: StepQuizFeatureState,
        step: Step,
        stepQuizName: String?,
        stepBlockName: String,
        hintText: String?
    ) -> some View {
        if let stepQuizName = stepQuizName {
            StepQuizNameView(text: stepQuizName)
        }

        let quizType = StepQuizChildQuizType(blockName: stepBlockName)

        if let attemptLoadedState = state as? StepQuizFeatureStateAttemptLoaded {
            if case .unsupported = quizType {
                StepQuizStatusView(state: .unsupportedQuiz)
            } else {
                buildChildQuiz(quizType: quizType, step: step, attemptLoadedState: attemptLoadedState)
                buildQuizStatusView(attemptLoadedState: attemptLoadedState)

                if let hintText = hintText {
                    StepQuizFeedbackView(text: hintText)
                }

                buildQuizActionButtons(quizType: quizType, attemptLoadedState: attemptLoadedState)
            }
        } else {
            StepQuizSkeletonViewFactory.makeSkeleton(for: quizType)
        }
    }

    @ViewBuilder
    private func buildChildQuiz(
        quizType: StepQuizChildQuizType,
        step: Step,
        attemptLoadedState: StepQuizFeatureStateAttemptLoaded
    ) -> some View {
        if let dataset = attemptLoadedState.attempt.dataset {
            let submissionStateEmpty = attemptLoadedState.submissionState as? StepQuizFeatureSubmissionStateEmpty
            let submissionStateLoaded = attemptLoadedState.submissionState as? StepQuizFeatureSubmissionStateLoaded

            let reply = submissionStateLoaded?.submission.reply ?? submissionStateEmpty?.reply
            let isDisabled: Bool = {
                if let submissionStateLoaded = submissionStateLoaded {
                    return !submissionStateLoaded.submission.isSubmissionEditable
                }
                return false
            }()

            StepQuizChildQuizViewFactory.make(
                quizType: quizType,
                step: step,
                dataset: dataset,
                reply: reply,
                onModuleInputDidSet: { viewModel.childQuizModuleInput = $0 },
                moduleOutput: viewModel
            )
            .disabled(isDisabled)
        }
    }

    @ViewBuilder
    private func buildQuizStatusView(attemptLoadedState: StepQuizFeatureStateAttemptLoaded) -> some View {
        if let submissionLoadedState = attemptLoadedState.submissionState as? StepQuizFeatureSubmissionStateLoaded {
            if let replyValidationError = submissionLoadedState.replyValidation as? ReplyValidationResultError {
                StepQuizStatusView(state: .invalidReply(message: replyValidationError.message))
            } else if let submissionStatus = submissionLoadedState.submission.status {
                switch submissionStatus {
                case SubmissionStatus.evaluation:
                    StepQuizStatusView(state: .evaluation)
                case SubmissionStatus.wrong:
                    StepQuizStatusView(state: .wrong)
                case SubmissionStatus.correct:
                    StepQuizStatusView(state: .correct)
                default:
                    EmptyView()
                }
            }
        }
    }

    @ViewBuilder
    private func buildQuizActionButtons(
        quizType: StepQuizChildQuizType,
        attemptLoadedState: StepQuizFeatureStateAttemptLoaded
    ) -> some View {
        let submissionStatus: SubmissionStatus? = {
            if let submissionStateLoaded = attemptLoadedState.submissionState as? StepQuizFeatureSubmissionStateLoaded {
                return submissionStateLoaded.submission.status
            }
            return SubmissionStatus.local
        }()

        let isCodeQuiz: Bool = {
            if case .code = quizType {
                return true
            }
            return false
        }()

        let retryButtonDescription: StepQuizActionButtons.RetryButton? = {
            guard isCodeQuiz else {
                return nil
            }

            return .init(appearance: .init(backgroundColor: .clear), action: viewModel.doQuizRetryAction)
        }()

        let continueButtonDescription: StepQuizActionButtons.ContinueButton? = {
            if submissionStatus == SubmissionStatus.correct {
                return .init(action: viewModel.doQuizContinueAction)
            }
            return nil
        }()

        let primaryButtonDescription: StepQuizActionButtons.PrimaryButton = {
            let codeQuizCustomParamsForState = { (state: StepQuizActionButton.State) -> (String, String)? in
                guard isCodeQuiz else {
                    return nil
                }

                return StepQuizActionButtonCodeQuizDelegate.getParamsForState(state)
            }

            return StepQuizActionButtons.PrimaryButton(
                state: .init(submissionStatus: submissionStatus),
                titleForState: { codeQuizCustomParamsForState($0)?.0 },
                systemImageNameForState: { codeQuizCustomParamsForState($0)?.1 },
                action: viewModel.doMainQuizAction
            )
        }()

        let isDisabled: Bool = {
            if submissionStatus == SubmissionStatus.correct {
                return false
            }
            return !StepQuizResolver.shared.isQuizEnabled(state: attemptLoadedState)
        }()

        StepQuizActionButtons(
            retryButton: retryButtonDescription,
            continueButton: continueButtonDescription,
            primaryButton: primaryButtonDescription
        )
        .disabled(isDisabled)
    }

    private func handleViewAction(_ viewAction: StepQuizFeatureActionViewAction) {
        switch viewAction {
        case is StepQuizFeatureActionViewActionShowNetworkError:
            ProgressHUD.showError(status: Strings.General.connectionError)
        case is StepQuizFeatureActionViewActionAskUserToEnableDailyReminders:
            isPresentingDailyStudyRemindersPermissionAlert = true
        case is StepQuizFeatureActionViewActionNavigateToHomeScreen:
            presentationMode.wrappedValue.dismiss()
        default:
            print("StepQuizView :: unhandled viewAction = \(viewAction)")
        }
    }
}