package org.hyperskill.app.android.user_list.injection

import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.hyperskill.app.android.user_list.presentation.UserListViewModel
import org.hyperskill.app.user_list.domain.interactor.UserListInteractor
import org.hyperskill.app.user_list.injection.UsersListFeatureBuilder
import ru.nobird.android.view.injection.base.presentation.ViewModelKey
import ru.nobird.app.presentation.redux.container.wrapWithViewContainer

@Module
object UserListModule {
    @Provides
    @IntoMap
    @ViewModelKey(UserListViewModel::class)
    internal fun provideUserListPresenter(usersListInteractor: UserListInteractor): ViewModel =
        UserListViewModel(
                UsersListFeatureBuilder
                    .build(usersListInteractor)
                    .wrapWithViewContainer()
        )
}