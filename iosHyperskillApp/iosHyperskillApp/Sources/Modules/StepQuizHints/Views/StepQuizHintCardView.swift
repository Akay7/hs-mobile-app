import shared
import SwiftUI

extension StepQuizHintCardView {
    struct Appearance {
        let authorAvatarWidthHeight: CGFloat = 20
        let reactionImageWidthHeight: CGFloat = 16
        let shortHintTextLength = 48
    }
}

struct StepQuizHintCardView: View {
    private(set) var appearance = Appearance()

    let authorAvatarSource: String?

    let authorName: String

    let hintText: String

    let hintHasReaction: Bool

    let onReactionTapped: (ReactionType) -> Void

    let onReportTapped: () -> Void

    let onReportAlertAppeared: () -> Void

    let onReportAlertConfirmed: () -> Void

    let onReportAlertCanceled: () -> Void

    let hasNextHints: Bool

    let onNextHintTapped: (() -> Void)

    @State private var isPresentingReportAlert = false

    @State private var isShowingMore = false

    private var isDisplaingShortHintText: Bool {
        !isShowingMore && hintText.count > appearance.shortHintTextLength
    }

    var body: some View {
        VStack(alignment: .leading, spacing: LayoutInsets.defaultInset) {
            HStack {
                LazyAvatarView(authorAvatarSource)
                    .frame(widthHeight: appearance.authorAvatarWidthHeight)

                Text(authorName)
                    .font(.caption)
                    .foregroundColor(.disabledText)

                Spacer()

                if !hintHasReaction {
                    Button(Strings.StepQuiz.Hints.reportButton) {
                        isPresentingReportAlert = true
                        onReportTapped()
                    }
                    .font(.subheadline)
                    .foregroundColor(Color(ColorPalette.primaryAlpha60))
                }
            }

            Text(
                isDisplaingShortHintText
                    ? "\(hintText.prefix(appearance.shortHintTextLength))..."
                    : hintText
            )
            .font(.subheadline)
            .foregroundColor(.primary)

            if isDisplaingShortHintText {
                ShowMoreButton {
                    isShowingMore = true
                }
            }

            if hintHasReaction {
                if hasNextHints {
                    StepQuizShowHintButton(text: Strings.StepQuiz.Hints.seeNextHint) {
                        isShowingMore = false
                        onNextHintTapped()
                    }
                } else {
                    Text(Strings.StepQuiz.Hints.lastHint)
                        .font(.caption)
                        .foregroundColor(.secondaryText)
                }
            } else {
                HStack(spacing: LayoutInsets.smallInset) {
                    Text(Strings.StepQuiz.Hints.helpfulQuestion)
                        .font(.caption)
                        .foregroundColor(.secondaryText)

                    Spacer()

                    StepQuizHintReactionButtonView(
                        reactionImage: Images.StepQuiz.Hints.unhelpfulReaction,
                        reactionText: Strings.General.no,
                        onReactionButtonTap: { onReactionTapped(ReactionType.unhelpful) }
                    )

                    StepQuizHintReactionButtonView(
                        reactionImage: Images.StepQuiz.Hints.helpfulReaction,
                        reactionText: Strings.General.yes,
                        onReactionButtonTap: { onReactionTapped(ReactionType.helpful) }
                    )
                }
            }
        }
        .padding()
        .background(Color.background)
        .addBorder(color: Color(ColorPalette.onSurfaceAlpha9))
        .alert(isPresented: $isPresentingReportAlert) {
            Alert(
                title: Text(Strings.StepQuiz.Hints.reportAlertTitle),
                message: Text(Strings.StepQuiz.Hints.reportAlertText),
                primaryButton: .default(
                    Text(Strings.General.no),
                    action: onReportAlertCanceled
                ),
                secondaryButton: .default(
                    Text(Strings.General.yes),
                    action: onReportAlertConfirmed
                )
            )
        }
        .onChange(of: isPresentingReportAlert) { newValue in
            if newValue {
                onReportAlertAppeared()
            }
        }
    }
}

struct StepQuizHintCardView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            StepQuizHintCardView(
                authorAvatarSource: nil,
                authorName: "Name Surname",
                hintText: "Python is used for almost everything in programming.",
                hintHasReaction: true,
                onReactionTapped: { _ in },
                onReportTapped: {},
                onReportAlertAppeared: {},
                onReportAlertConfirmed: {},
                onReportAlertCanceled: {},
                hasNextHints: true,
                onNextHintTapped: {}
            )

            StepQuizHintCardView(
                authorAvatarSource: nil,
                authorName: "Name Surname",
                hintText: "Python is used for almost everything in programming.",
                hintHasReaction: false,
                onReactionTapped: { _ in },
                onReportTapped: {},
                onReportAlertAppeared: {},
                onReportAlertConfirmed: {},
                onReportAlertCanceled: {},
                hasNextHints: true,
                onNextHintTapped: {}
            )
        }
        .previewLayout(.sizeThatFits)
    }
}
