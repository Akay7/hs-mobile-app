import SwiftUI

struct QuizStatView: View {
    var text: String

    var body: some View {
        HStack(alignment: .top, spacing: 4) {
            Image("choice-quiz-clock-icon")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(width: 12, height: 21)
                .foregroundColor(.secondaryText)
            Text(text)
                .font(.subheadline)
                .foregroundColor(.secondaryText)
        }
    }
}

struct QuizStatView_Previews: PreviewProvider {
    static var previews: some View {
        QuizStatView(text: Strings.choiceQuizStatText)
    }
}
