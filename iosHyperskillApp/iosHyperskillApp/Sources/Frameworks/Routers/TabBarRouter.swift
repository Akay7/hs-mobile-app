import UIKit

final class TabBarRouter: SourcelessRouter, DeepLinkRouterProtocol {
    private let tab: Tab
    private let popToRoot: Bool

    /// Initializes a new instance of a `TabBarRouter`.
    /// - Parameters:
    ///   - tab: Target tab to route to.
    ///   - popToRoot: Flag indicating whether to pop to root view controller of the target tab.
    init(tab: Tab, popToRoot: Bool = true) {
        self.tab = tab
        self.popToRoot = popToRoot
    }

    func route() {
        currentTabBarController?.selectedIndex = tab.index

        guard popToRoot else {
            return
        }

        DispatchQueue.main.async {
            self.currentNavigation?.popToRootViewController(animated: true)
        }
    }

    enum Tab: Equatable {
        case home
        case studyPlan
        case profile

        var index: Int {
            switch self {
            case .home:
                return 0
            case .studyPlan:
                return 1
            case .profile:
                return 2
            }
        }
    }
}
