package org.hyperskill.app.android.profile.view.fragment

import android.graphics.Paint
import android.os.Bundle
import android.view.View
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import by.kirich1409.viewbindingdelegate.viewBinding
import coil.ImageLoader
import coil.load
import coil.transform.CircleCropTransformation
import java.util.Locale
import org.hyperskill.app.android.HyperskillApp
import org.hyperskill.app.android.R
import org.hyperskill.app.android.core.extensions.checkNotificationChannelAvailability
import org.hyperskill.app.android.core.extensions.isChannelNotificationsEnabled
import org.hyperskill.app.android.core.extensions.openUrl
import org.hyperskill.app.android.core.view.ui.dialog.LoadingProgressDialogFragment
import org.hyperskill.app.android.core.view.ui.dialog.dismissDialogFragmentIfExists
import org.hyperskill.app.android.core.view.ui.setHyperskillColors
import org.hyperskill.app.android.core.view.ui.updateIsRefreshing
import org.hyperskill.app.android.databinding.FragmentProfileBinding
import org.hyperskill.app.android.home.view.ui.screen.HomeScreen
import org.hyperskill.app.android.main.view.ui.navigation.MainScreenRouter
import org.hyperskill.app.android.notification.model.HyperskillNotificationChannel
import org.hyperskill.app.android.profile.view.delegate.StreakCardFormDelegate
import org.hyperskill.app.android.profile.view.dialog.StreakFreezeDialogFragment
import org.hyperskill.app.android.profile_settings.view.dialog.ProfileSettingsDialogFragment
import org.hyperskill.app.android.view.base.ui.extension.redirectToUsernamePage
import org.hyperskill.app.android.view.base.ui.extension.setElevationOnCollapsed
import org.hyperskill.app.android.view.base.ui.extension.snackbar
import org.hyperskill.app.profile.domain.model.Profile
import org.hyperskill.app.profile.presentation.ProfileFeature
import org.hyperskill.app.profile.presentation.ProfileViewModel
import org.hyperskill.app.profile.view.social_redirect.SocialNetworksRedirect
import ru.nobird.android.view.base.ui.delegate.ViewStateDelegate
import ru.nobird.android.view.base.ui.extension.argument
import ru.nobird.android.view.base.ui.extension.setTextIfChanged
import ru.nobird.android.view.base.ui.extension.showIfNotExists
import ru.nobird.android.view.redux.ui.extension.reduxViewModel
import ru.nobird.app.presentation.redux.container.ReduxView

class ProfileFragment :
    Fragment(R.layout.fragment_profile),
    ReduxView<ProfileFeature.State, ProfileFeature.Action.ViewAction>,
    TimeIntervalPickerDialogFragment.Companion.Callback {
    companion object {
        fun newInstance(profileId: Long? = null, isInitCurrent: Boolean = true): Fragment =
            ProfileFragment()
                .apply {
                    this.profileId = profileId ?: 0
                    this.isInitCurrent = isInitCurrent
                }
    }

    private var profileId: Long by argument()
    private var isInitCurrent: Boolean by argument()

    private val viewBinding: FragmentProfileBinding by viewBinding(FragmentProfileBinding::bind)

    private lateinit var viewModelFactory: ViewModelProvider.Factory
    private val profileViewModel: ProfileViewModel by reduxViewModel(this) { viewModelFactory }
    private val viewStateDelegate: ViewStateDelegate<ProfileFeature.State> = ViewStateDelegate()

    private var streakFormDelegate: StreakCardFormDelegate? = null

    private val platformNotificationComponent =
        HyperskillApp.graph().platformNotificationComponent

    private val imageLoader: ImageLoader by lazy(LazyThreadSafetyMode.NONE) {
        HyperskillApp.graph().imageLoadingComponent.imageLoader
    }

    private val notificationManager: NotificationManagerCompat by lazy(LazyThreadSafetyMode.NONE) {
        NotificationManagerCompat.from(requireContext())
    }

    private val mainScreenRouter: MainScreenRouter =
        HyperskillApp.graph().navigationComponent.mainScreenCicerone.router

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        injectComponents()
    }

    private fun injectComponents() {
        val profileComponent = HyperskillApp.graph().buildProfileComponent()
        val platformProfileComponent = HyperskillApp.graph().buildPlatformProfileComponent(profileComponent)
        viewModelFactory = platformProfileComponent.reduxViewModelFactory
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewStateDelegate()
        initToolbar()
        initRemindersSchedule()
        initProfileViewFullVersionTextView()

        streakFormDelegate =
            StreakCardFormDelegate(
                context = requireContext(),
                binding = viewBinding.profileStreakLayout,
                onFreezeButtonClick = {
                    profileViewModel.onNewMessage(ProfileFeature.Message.StreakFreezeCardButtonClicked)
                }
            )

        viewBinding.profileError.tryAgain.setOnClickListener {
            profileViewModel.onNewMessage(
                ProfileFeature.Message.Initialize(
                    profileId = profileId,
                    forceUpdate = true,
                    isInitCurrent = isInitCurrent
                )
            )
        }

        with(viewBinding.profileSwipeRefreshLayout) {
            setHyperskillColors()
            setOnRefreshListener {
                profileViewModel.onNewMessage(
                    ProfileFeature.Message.PullToRefresh(
                        isRefreshCurrent = isInitCurrent,
                        profileId = profileId
                    )
                )
            }
        }

        profileViewModel.onNewMessage(
            ProfileFeature.Message.Initialize(
                profileId = profileId,
                isInitCurrent = isInitCurrent
            )
        )
        profileViewModel.onNewMessage(ProfileFeature.Message.ViewedEventMessage)
    }

    private fun initViewStateDelegate() {
        with(viewStateDelegate) {
            addState<ProfileFeature.State.Idle>()
            addState<ProfileFeature.State.Loading>(viewBinding.profileSkeleton.root)
            addState<ProfileFeature.State.Error>(viewBinding.profileError.root)
            addState<ProfileFeature.State.Content>(
                viewBinding.profileContentNestedScrollView,
                viewBinding.profileSettingsButton
            )
        }
    }

    private fun initRemindersSchedule() {
        with(viewBinding.profileDailyReminder) {
            profileScheduleTextView.setOnClickListener {
                profileViewModel.onNewMessage(ProfileFeature.Message.ClickedDailyStudyRemindsTimeEventMessage)
                TimeIntervalPickerDialogFragment
                    .newInstance()
                    .showIfNotExists(childFragmentManager, TimeIntervalPickerDialogFragment.TAG)
            }

            profileDailyRemindersSwitchCompat.setOnCheckedChangeListener { _, isChecked ->
                onDailyReminderCheckClicked(
                    isChecked = isChecked,
                    notificationManager = notificationManager
                )
            }
        }
    }

    private fun onDailyReminderCheckClicked(isChecked: Boolean, notificationManager: NotificationManagerCompat) {
        profileViewModel.onNewMessage(ProfileFeature.Message.DailyStudyRemindersToggleClicked(isChecked))

        if (isChecked) {
            platformNotificationComponent.dailyStudyReminderNotificationDelegate.scheduleDailyNotification()

            val isEnabled = notificationManager.checkNotificationChannelAvailability(
                requireContext(),
                HyperskillNotificationChannel.DailyReminder
            ) {
                viewBinding.root.snackbar(org.hyperskill.app.R.string.common_error)
            }
            viewBinding.profileDailyReminder.profileDailyRemindersSwitchCompat.isChecked = isEnabled
            viewBinding.profileDailyReminder.profileScheduleTextView.isVisible = isEnabled
        } else {
            viewBinding.profileDailyReminder.profileScheduleTextView.isVisible = false
        }
    }

    private fun initProfileViewFullVersionTextView() {
        with(viewBinding.profileFooterLayout.profileViewFullVersionTextView) {
            paintFlags = paintFlags or Paint.UNDERLINE_TEXT_FLAG
            setOnClickListener {
                profileViewModel.onNewMessage(ProfileFeature.Message.ClickedViewFullProfile)
            }
        }
    }

    private fun initToolbar() {
        with(viewBinding.profileAppBar) {
            setElevationOnCollapsed(viewLifecycleOwner.lifecycle)
            setExpanded(true)
        }
        viewBinding.profileSettingsButton.setOnClickListener {
            profileViewModel.onNewMessage(ProfileFeature.Message.ClickedSettingsEventMessage)
            ProfileSettingsDialogFragment
                .newInstance()
                .showIfNotExists(childFragmentManager, ProfileSettingsDialogFragment.TAG)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        streakFormDelegate = null
    }

    override fun onTimeIntervalPicked(chosenInterval: Int) {
        profileViewModel.onNewMessage(
            ProfileFeature.Message.DailyStudyRemindersIntervalStartHourChanged(chosenInterval)
        )
        platformNotificationComponent.dailyStudyReminderNotificationDelegate.scheduleDailyNotification()
    }

    override fun onAction(action: ProfileFeature.Action.ViewAction) {
        when (action) {
            is ProfileFeature.Action.ViewAction.OpenUrl ->
                requireContext().openUrl(action.url)

            is ProfileFeature.Action.ViewAction.ShowGetMagicLinkError ->
                viewBinding.root.snackbar(org.hyperskill.app.R.string.common_error)

            is ProfileFeature.Action.ViewAction.ShowStreakFreezeModal -> {
                StreakFreezeDialogFragment.newInstance(action.streakFreezeState)
                    .showIfNotExists(childFragmentManager, StreakFreezeDialogFragment.Tag)
            }
            ProfileFeature.Action.ViewAction.HideStreakFreezeModal -> {
                childFragmentManager
                    .dismissDialogFragmentIfExists(StreakFreezeDialogFragment.Tag)
            }
            ProfileFeature.Action.ViewAction.ShowStreakFreezeBuyingStatus.Loading -> {
                childFragmentManager
                    .dismissDialogFragmentIfExists(LoadingProgressDialogFragment.TAG)
            }
            ProfileFeature.Action.ViewAction.ShowStreakFreezeBuyingStatus.Success -> {
                viewBinding.root.snackbar(org.hyperskill.app.R.string.streak_freeze_bought_success)
                childFragmentManager
                    .dismissDialogFragmentIfExists(LoadingProgressDialogFragment.TAG)
            }
            ProfileFeature.Action.ViewAction.ShowStreakFreezeBuyingStatus.Error ->
                viewBinding.root.snackbar(org.hyperskill.app.R.string.streak_freeze_bought_error)

            ProfileFeature.Action.ViewAction.NavigateTo.HomeScreen -> {
                mainScreenRouter.switch(HomeScreen)
            }
        }
    }

    override fun render(state: ProfileFeature.State) {
        viewStateDelegate.switchState(state)
        renderSwipeRefresh(state)
        when (state) {
            is ProfileFeature.State.Content -> {
                profileId = state.profile.id
                renderContent(state)
            }
            else -> {
                // no op
            }
        }
    }

    private fun renderContent(content: ProfileFeature.State.Content) {
        streakFormDelegate?.render(content.streak, content.streakFreezeState)

        renderStatistics(content.profile)
        renderNameProfileBadge(content.profile)
        renderRemindersSchedule(content.dailyStudyRemindersState, notificationManager)
        renderAboutMeSection(content.profile)
        renderBioSection(content.profile)
        renderExperienceSection(content.profile)
        renderSocialButtons(content.profile)

        if (content.isLoadingMagicLink) {
            LoadingProgressDialogFragment.newInstance()
                .showIfNotExists(childFragmentManager, LoadingProgressDialogFragment.TAG)
        } else {
            childFragmentManager.dismissDialogFragmentIfExists(LoadingProgressDialogFragment.TAG)
        }
    }

    private fun renderSwipeRefresh(content: ProfileFeature.State) {
        with(viewBinding.profileSwipeRefreshLayout) {
            isEnabled = content is ProfileFeature.State.Content
            if (content is ProfileFeature.State.Content) {
                updateIsRefreshing(content.isRefreshing)
            }
        }
    }

    private fun renderNameProfileBadge(profile: Profile) {
        with(viewBinding.profileHeader) {
            profileAvatarImageView.load(profile.avatar, imageLoader) {
                transformations(CircleCropTransformation())
            }
            profileNameTextView.setTextIfChanged(profile.fullname)
            profileRoleTextView.setTextIfChanged(
                if (profile.isStaff) {
                    resources.getString(org.hyperskill.app.R.string.profile_role_staff_text)
                } else {
                    resources.getString(org.hyperskill.app.R.string.profile_role_learner_text)
                }
            )
        }
    }

    private fun renderRemindersSchedule(
        remindersState: ProfileFeature.DailyStudyRemindersState,
        notificationManager: NotificationManagerCompat
    ) {
        with(viewBinding.profileDailyReminder) {
            profileScheduleTextView.text = getScheduleTimeText(
                time = remindersState.startHour
            )

            val isDailyNotificationEnabled = notificationManager.isChannelNotificationsEnabled(
                HyperskillNotificationChannel.DailyReminder.channelId
            ) && remindersState.isEnabled
            profileDailyRemindersSwitchCompat.isChecked = isDailyNotificationEnabled
            profileScheduleTextView.isVisible = isDailyNotificationEnabled
        }
    }

    private fun renderStatistics(profile: Profile) {
        with(viewBinding.profileStatisticsLayout) {
            profileGemsCountTextView.setTextIfChanged(profile.gamification.hypercoinsBalance.toString())
            profileTracksCountTextView.setTextIfChanged(profile.completedTracks.size.toString())
            profileProjectsCountTextView.setTextIfChanged(profile.gamification.passedProjectsCount.toString())
        }
    }

    private fun renderAboutMeSection(profile: Profile) {
        with(viewBinding.profileFooterLayout) {
            if (profile.country != null) {
                profileAboutLivesTextView.text =
                    "${resources.getString(org.hyperskill.app.R.string.profile_lives_in_text)} ${
                    Locale(
                        Locale.ENGLISH.language,
                        profile.country!!
                    ).displayCountry
                    }"
            } else {
                profileAboutLivesTextView.visibility = View.GONE
            }

            if (profile.languages?.isEmpty() == false) {
                val languages = profile.languages!!.joinToString(", ") {
                    Locale(it).getDisplayLanguage(Locale.ENGLISH)
                }
                profileAboutSpeaksTextView.text =
                    "${resources.getString(org.hyperskill.app.R.string.profile_speaks_text)} $languages"
            } else {
                profileAboutSpeaksTextView.visibility = View.GONE
            }
        }
    }

    private fun renderBioSection(profile: Profile) {
        with(viewBinding.profileFooterLayout) {
            if (profile.bio != "") {
                profileAboutBioTextTextView.text = profile.bio
            } else {
                profileAboutBioTextTextView.visibility = View.GONE
                profileAboutBioBarTextView.visibility = View.GONE
            }
        }
    }

    private fun renderExperienceSection(profile: Profile) {
        with(viewBinding.profileFooterLayout) {
            if (profile.experience != "") {
                profileAboutExperienceTextTextView.text = profile.experience
            } else {
                profileAboutExperienceTextTextView.visibility = View.GONE
                profileAboutExperienceBarTextView.visibility = View.GONE
            }
        }
    }

    private fun renderSocialButtons(profile: Profile) {
        with(viewBinding.profileFooterLayout) {
            if (profile.facebookUsername != "") {
                profileFacebookButton.setOnClickListener {
                    SocialNetworksRedirect.FACEBOOK.redirectToUsernamePage(requireContext(), profile.facebookUsername)
                }
            } else {
                profileFacebookButton.visibility = View.GONE
            }

            if (profile.twitterUsername != "") {
                profileTwitterButton.setOnClickListener {
                    SocialNetworksRedirect.TWITTER.redirectToUsernamePage(requireContext(), profile.twitterUsername)
                }
            } else {
                profileTwitterButton.visibility = View.GONE
            }

            if (profile.linkedinUsername != "") {
                profileLinkedinButton.setOnClickListener {
                    SocialNetworksRedirect.LINKEDIN.redirectToUsernamePage(requireContext(), profile.linkedinUsername)
                }
            } else {
                profileLinkedinButton.visibility = View.GONE
            }

            if (profile.redditUsername != "") {
                profileRedditButton.setOnClickListener {
                    SocialNetworksRedirect.REDDIT.redirectToUsernamePage(requireContext(), profile.redditUsername)
                }
            } else {
                profileRedditButton.visibility = View.GONE
            }

            if (profile.githubUsername != "") {
                profileGithubButton.setOnClickListener {
                    SocialNetworksRedirect.GITHUB.redirectToUsernamePage(requireContext(), profile.githubUsername)
                }
            } else {
                profileGithubButton.visibility = View.GONE
            }
        }
    }

    private fun getScheduleTimeText(time: Int) =
        requireContext().resources.getString(org.hyperskill.app.R.string.profile_daily_study_reminders_schedule_text) +
            "${time.toString().padStart(2, '0')}:00 - ${(time + 1).toString().padStart(2, '0')}:00"
}