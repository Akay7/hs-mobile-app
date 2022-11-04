import Foundation
import shared

final class StepQuizHintsViewModel: FeatureViewModel<
  StepQuizHintsFeatureState,
  StepQuizHintsFeatureMessage,
  StepQuizHintsFeatureActionViewAction
> {
    let stepID: Int64

    init(stepID: Int64, feature: Presentation_reduxFeature) {
        self.stepID = stepID
        super.init(feature: feature)
    }

    func loadHintsIDs() {
        onNewMessage(StepQuizHintsFeatureMessageInitWithStepId(stepId: stepID))
    }

    func onHintReactionButtonTap(reaction: ReactionType) {
        onNewMessage(StepQuizHintsFeatureMessageReactionButtonClicked(reaction: reaction))
    }

    func onHintReportButtonTap() {
        onNewMessage(StepQuizHintsFeatureMessageHintReported())
    }

    func onLoadHintButtonTap() {
        onNewMessage(StepQuizHintsFeatureMessageLoadHintButtonClicked())
    }
}
