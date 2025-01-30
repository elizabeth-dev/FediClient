package sh.elizabeth.fedihome.ui.routes.dashboard.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.elizabeth.fedihome.ui.composable.SlimPostCard

@Composable
fun HomeScreen(
	homeViewModel: HomeViewModel = hiltViewModel(),
	navToCompose: (String) -> Unit,
	navToPost: (String) -> Unit,
	navToProfile: (String) -> Unit,
) {
	val uiState by homeViewModel.uiState.collectAsStateWithLifecycle()

	HomeScreen(
		uiState = uiState,
		onRefresh = homeViewModel::refreshTimeline,
		onReply = navToCompose,
		onVotePoll = homeViewModel::votePoll,
		navToPost = navToPost,
		navToProfile = navToProfile,
		onAddFavorite = homeViewModel::addFavorite,
		onRemoveReaction = homeViewModel::removeReaction,
		onAddReaction = homeViewModel::addReaction
	)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun HomeScreen(
	uiState: HomeUiState,
	onRefresh: (String) -> Unit,
	onReply: (String) -> Unit,
	onVotePoll: (activeAccount: String, postId: String, pollId: String?, List<Int>) -> Unit,
	navToPost: (String) -> Unit,
	navToProfile: (String) -> Unit,
	onAddFavorite: (activeAccount: String, postId: String) -> Unit,
	onRemoveReaction: (activeAccount: String, postId: String) -> Unit,
	onAddReaction: (activeAccount: String, postId: String, reaction: String) -> Unit,
) {
	val pullRefreshState =
		rememberPullRefreshState(uiState.isLoading, { onRefresh(uiState.activeAccount) })

	LaunchedEffect(key1 = uiState.activeAccount) {
		if (uiState.activeAccount.isNotBlank()) onRefresh(uiState.activeAccount)
	}

	Box(
		Modifier
			.fillMaxSize()
			.pullRefresh(pullRefreshState, true)

	) {
		when (uiState) {
			is HomeUiState.NoPosts -> if (!uiState.isLoading) Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(text = "No posts yet!")
			}

			is HomeUiState.HasPosts -> LazyColumn(Modifier.fillMaxSize()) {
				items(uiState.posts) { post ->
					SlimPostCard(
						post = post,
						onReply = onReply,
						onVotePoll = {
							onVotePoll(
								uiState.activeAccount, post.id, post.poll?.id, it
							)
						},
						navToPost = navToPost,
						navToProfile = navToProfile,
						onAddFavorite = { onAddFavorite(uiState.activeAccount, it) },
						onRemoveReaction = { onRemoveReaction(uiState.activeAccount, it) },
						onAddReaction = { postId, reaction ->
							onAddReaction(
								uiState.activeAccount,
								postId,
								reaction
							)
						})
				}
			}
		}

		PullRefreshIndicator(
			uiState.isLoading, pullRefreshState, Modifier.align(Alignment.TopCenter)
		)
	}
}
