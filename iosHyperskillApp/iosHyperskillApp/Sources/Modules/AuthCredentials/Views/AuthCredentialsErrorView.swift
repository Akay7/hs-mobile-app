import SwiftUI

struct AuthCredentialsErrorView: View {
    let message: String

    var body: some View {
        Text(message)
            .foregroundColor(Color(ColorPalette.error))
            .font(.caption)
            .frame(maxWidth: .infinity)
            .padding()
            .background(Color(ColorPalette.error).opacity(0.12))
            .cornerRadius(8)
    }
}

struct AuthEmailErrorView_Previews: PreviewProvider {
    static var previews: some View {
        AuthCredentialsErrorView(message: "Error message")
    }
}
