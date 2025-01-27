package org.hyperskill.app.android.debug

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import com.github.terrakok.cicerone.androidx.FragmentScreen
import org.hyperskill.app.android.main.view.ui.navigation.Tabs

object DebugScreen : FragmentScreen {
    override val screenKey: String = Tabs.DEBUG.name

    override fun createFragment(factory: FragmentFactory): Fragment =
        Fragment()
}