import Foundation
import shared

final class StepQuizViewDataMapper {
    private let stepQuizStatsTextMapper: StepQuizStatsTextMapper

    private let stepQuizTitleMapper: StepQuizTitleMapper

    init(stepQuizStatsTextMapper: StepQuizStatsTextMapper, stepQuizTitleMapper: StepQuizTitleMapper) {
        self.stepQuizStatsTextMapper = stepQuizStatsTextMapper
        self.stepQuizTitleMapper = stepQuizTitleMapper
    }

    func mapStepDataToViewData(step: Step, state: StepQuizFeatureStepQuizStateKs) -> StepQuizViewData {
        let quizType: StepQuizChildQuizType = {
            if state == .unsupported {
                return .unsupported(blockName: step.block.name)
            }
            return StepQuizChildQuizType(step: step)
        }()

        if case .unsupported = quizType {
            return StepQuizViewData(
                formattedStats: nil,
                stepText: step.block.text,
                quizType: quizType,
                quizName: nil,
                feedbackHintText: nil,
                stepHasHints: false
            )
        }

        let formattedStats = stepQuizStatsTextMapper.getFormattedStepQuizStats(
            solvedByUsersCount: step.solvedBy,
            millisSinceLastCompleted: step.millisSinceLastCompleted
        )

        let attemptLoadedState: StepQuizFeatureStepQuizStateAttemptLoaded? = {
            switch state {
            case .attemptLoading(let attemptLoadingState):
                return attemptLoadingState.oldState
            case .attemptLoaded(let attemptLoadedState):
                return attemptLoadedState
            default:
                return nil
            }
        }()

        let quizName: String? = {
            guard let dataset = attemptLoadedState?.attempt.dataset else {
                return nil
            }

            // Custom title rendering by code quiz
            if step.block.name == BlockName.shared.CODE {
                return nil
            }

            return stepQuizTitleMapper.getStepQuizTitle(
                blockName: step.block.name,
                isMultipleChoice: KotlinBoolean(bool: dataset.isMultipleChoice),
                isCheckbox: KotlinBoolean(bool: dataset.isCheckbox)
            )
        }()

        let feedbackHintText: String? = {
            guard let submissionStateLoaded =
               attemptLoadedState?.submissionState as? StepQuizFeatureSubmissionStateLoaded else {
                return nil
            }

            if submissionStateLoaded.submission.status == SubmissionStatus.rejected,
               let feedback = submissionStateLoaded.submission.feedback {
                let formattedText = FeedbackKt.formattedText(feedback)
                return formattedText.isEmpty ? nil : formattedText
            } else if let hint = submissionStateLoaded.submission.hint {
                return hint.isEmpty ? nil : hint
            } else {
                return nil
            }
        }()

        let stepHasHints: Bool = {
            guard let hintsStatistic = step.commentsStatistics.first(where: { $0.thread == CommentThread.hint }) else {
                return false
            }
            return hintsStatistic.totalCount > 0
        }()

        return StepQuizViewData(
            formattedStats: formattedStats,
            stepText: step.block.text,
            quizType: quizType,
            quizName: quizName,
            feedbackHintText: feedbackHintText,
            stepHasHints: stepHasHints
        )
    }
}
