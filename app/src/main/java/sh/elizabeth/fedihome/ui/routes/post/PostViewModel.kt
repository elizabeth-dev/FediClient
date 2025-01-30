package sh.elizabeth.fedihome.ui.routes.post

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import sh.elizabeth.fedihome.MainDestinations
import sh.elizabeth.fedihome.data.repository.AuthRepository
import sh.elizabeth.fedihome.data.repository.PostRepository
import sh.elizabeth.fedihome.domain.VotePollUseCase
import sh.elizabeth.fedihome.model.Post
import javax.inject.Inject

sealed interface PostUiState {
	val isLoading: Boolean
	val activeAccount: String
	val postId: String

	data class NoPost(
		override val isLoading: Boolean,
		override val activeAccount: String,
		override val postId: String,
	) : PostUiState

	data class HasPost(
		val post: Post,
		override val isLoading: Boolean,
		override val activeAccount: String,
		override val postId: String,
	) : PostUiState
}

private data class PostViewModelState(
	val isLoading: Boolean = false,
	val postId: String,
) {
	fun toUiState(post: Post? = null, activeAccount: String = ""): PostUiState = if (post == null) {
		PostUiState.NoPost(isLoading = isLoading, activeAccount = activeAccount, postId = postId)
	} else {
		PostUiState.HasPost(
			post = post, isLoading = isLoading, activeAccount = activeAccount, postId = postId
		)
	}
}

@HiltViewModel
class PostViewModel @Inject constructor(
	private val votePollUseCase: VotePollUseCase,
	private val postRepository: PostRepository,
	authRepository: AuthRepository,
	savedStateHandle: SavedStateHandle,
) : ViewModel() {
	private val postId =
		savedStateHandle.toRoute<MainDestinations.POST>().postId

	private val viewModelState =
		MutableStateFlow(PostViewModelState(isLoading = true, postId = postId))

	val uiState = combine(
		postRepository.getPostFlow(postId), authRepository.activeAccount, viewModelState
	) { post, activeAccount, state ->
		state.toUiState(post, activeAccount)
	}.stateIn(viewModelScope, SharingStarted.Eagerly, viewModelState.value.toUiState())

	fun refreshPost(activeAccount: String, postId: String) {
		viewModelState.update { it.copy(isLoading = true) }
		viewModelScope.launch {
			postRepository.fetchPost(activeAccount, postId)
			viewModelState.update { it.copy(isLoading = false) }
		}
	}

	fun votePoll(activeAccount: String, postId: String, pollId: String?, choices: List<Int>) {
		viewModelScope.launch {
			votePollUseCase(activeAccount, postId, pollId, choices)
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
