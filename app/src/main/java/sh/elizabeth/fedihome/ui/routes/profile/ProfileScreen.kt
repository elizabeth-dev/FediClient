package sh.elizabeth.fedihome.ui.routes.profile

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.displayCutoutPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.elizabeth.fedihome.ui.composable.ProfileHeader
import sh.elizabeth.fedihome.ui.composable.SlimPostCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProfileScreen(
	uiState: ProfileUiState,
	contentPadding: PaddingValues,
	onRefresh: (activeAccount: String, profileTag: String, profileId: String?) -> Unit,
	onReply: (String) -> Unit,
	onVotePoll: (activeAccount: String, postId: String, pollId: String?, List<Int>) -> Unit,
	navToPost: (String) -> Unit,
	navToProfile: (String) -> Unit,
	onAddFavorite: (String, String) -> Unit,
	onRemoveReaction: (String, String) -> Unit,
	onAddReaction: (String, String, String) -> Unit,
) {
	val pullRefreshState = rememberPullRefreshState(
		uiState.isLoading, {
			onRefresh(
				uiState.activeAccount,
				uiState.profileTag,
				if (uiState is ProfileUiState.HasProfile) uiState.profile.id else null
			)
		})

	Box(
		modifier = Modifier
			.fillMaxSize()
			.navigationBarsPadding()
			.statusBarsPadding()
			.displayCutoutPadding()
			.pullRefresh(pullRefreshState)
	) {
		when (uiState) {
			is ProfileUiState.NoProfile -> if (!uiState.isLoading) Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(text = "Post not found")
			}

			is ProfileUiState.HasProfile -> LazyColumn(Modifier.fillMaxSize()) {

				item {
					ProfileHeader(profile = uiState.profile, navToProfile = navToProfile)
					HorizontalDivider(thickness = 1.dp)
				}

				if (uiState.posts != null) items(uiState.posts) { post ->
					SlimPostCard(
						post = post,
						onReply = onReply,
						onVotePoll = {
							onVotePoll(uiState.activeAccount, post.id, post.poll?.id, it)
						},
						navToPost = navToPost,
						navToProfile = { if (it != uiState.profileTag) navToProfile(it) },
						onAddFavorite = { onAddFavorite(uiState.activeAccount, it) },
						onRemoveReaction = { onRemoveReaction(uiState.activeAccount, it) },
						onAddReaction = { postId, reaction ->
							onAddReaction(
								uiState.activeAccount, postId, reaction
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
