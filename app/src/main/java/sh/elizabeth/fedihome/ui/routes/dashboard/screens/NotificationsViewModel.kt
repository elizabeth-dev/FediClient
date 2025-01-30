package sh.elizabeth.fedihome.ui.routes.dashboard.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.elizabeth.fedihome.data.repository.AuthRepository
import sh.elizabeth.fedihome.data.repository.NotificationRepository
import sh.elizabeth.fedihome.data.repository.PostRepository
import sh.elizabeth.fedihome.domain.VotePollUseCase
import sh.elizabeth.fedihome.model.Notification
import javax.inject.Inject

sealed interface NotificationsUiState {
	val isLoading: Boolean
	val activeAccount: String

	data class NoNotifications(
		override val isLoading: Boolean,
		override val activeAccount: String,
	) : NotificationsUiState

	data class HasNotifications(
		val notifications: List<Notification>,
		override val isLoading: Boolean,
		override val activeAccount: String,
	) : NotificationsUiState
}

private data class NotificationsViewModelState(
	val isLoading: Boolean = false,
) {
	fun toUiState(
        notifications: List<Notification>?, activeAccount: String = "",
    ): NotificationsUiState = if (notifications.isNullOrEmpty()) {
		NotificationsUiState.NoNotifications(
			isLoading = isLoading,
			activeAccount = activeAccount
		)
	} else {
		NotificationsUiState.HasNotifications(
			notifications = notifications,
			isLoading = isLoading,
			activeAccount = activeAccount
		)
	}

}

@HiltViewModel
class NotificationsViewModel @Inject constructor(
	authRepository: AuthRepository,
	private val notificationRepository: NotificationRepository,
	private val votePollUseCase: VotePollUseCase,
	private val postRepository: PostRepository,
) : ViewModel() {
	private val viewModelState =
		MutableStateFlow(NotificationsViewModelState(isLoading = true))

	@OptIn(ExperimentalCoroutinesApi::class)
	private val notifications =
		authRepository.activeAccount.flatMapLatest {
			notificationRepository.getNotificationsFlow(
				it
			)
		}
			.distinctUntilChanged()

	val uiState = combine(
		viewModelState,
		authRepository.activeAccount,
		notifications,
	) { state, activeAccount, notifications ->
		state.toUiState(notifications, activeAccount)
	}.stateIn(
		viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState(
			notifications = null,
		)
	)

	fun refreshNotifications(profileIdentifier: String) {
		viewModelState.update { it.copy(isLoading = true) }
		viewModelScope.launch {
			notificationRepository.fetchNotifications(profileIdentifier)
			viewModelState.update { it.copy(isLoading = false) }
		}
	}

	fun votePoll(
        profileIdentifier: String,
        postId: String,
        pollId: String?,
        choices: List<Int>,
    ) {
		viewModelScope.launch {
			votePollUseCase(profileIdentifier, postId, pollId, choices)
		}
	}

	fun addFavorite(activeAccount: String, postId: String) {
		viewModelScope.launch {
			postRepository.createReaction(activeAccount, postId, "⭐")
		}
	}

	fun removeReaction(activeAccount: String, postId: String) {
		viewModelScope.launch {
			postRepository.deleteReaction(activeAccount, postId)
		}
	}

	fun addReaction(activeAccount: String, postId: String, reaction: String) {
		viewModelScope.launch {
			postRepository.createReaction(activeAccount, postId, reaction)
		}
	}
}
