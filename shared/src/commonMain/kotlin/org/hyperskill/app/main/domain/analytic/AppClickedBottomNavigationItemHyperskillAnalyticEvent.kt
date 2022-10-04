package org.hyperskill.app.main.domain.analytic

import org.hyperskill.app.analytic.domain.model.hyperskill.HyperskillAnalyticAction
import org.hyperskill.app.analytic.domain.model.hyperskill.HyperskillAnalyticEvent
import org.hyperskill.app.analytic.domain.model.hyperskill.HyperskillAnalyticPart
import org.hyperskill.app.analytic.domain.model.hyperskill.HyperskillAnalyticRoute
import org.hyperskill.app.analytic.domain.model.hyperskill.HyperskillAnalyticTarget

class AppClickedBottomNavigationItemHyperskillAnalyticEvent(
    oldNavigationItem: NavigationItem,
    newNavigationItem: NavigationItem
) : HyperskillAnalyticEvent(
    oldNavigationItem.route,
    HyperskillAnalyticAction.CLICK,
    HyperskillAnalyticPart.TABS,
    newNavigationItem.target
) {
    enum class NavigationItem(val route: HyperskillAnalyticRoute, val target: HyperskillAnalyticTarget) {
        HOME(HyperskillAnalyticRoute.Home(), HyperskillAnalyticTarget.HOME),
        TRACK(HyperskillAnalyticRoute.Track(), HyperskillAnalyticTarget.TRACK),
        PROFILE(HyperskillAnalyticRoute.Profile(), HyperskillAnalyticTarget.PROFILE)
    }
}