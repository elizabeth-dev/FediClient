package sh.elizabeth.fedihome.ui.routes.post

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import sh.elizabeth.fedihome.ui.composable.SlimPostCard

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun PostScreen(
	uiState: PostUiState,
	contentPadding: PaddingValues,
	onPostRefresh: (activeAccount: String, postId: String) -> Unit,
	onReply: (String) -> Unit,
	onVotePoll: (activeAccount: String, postId: String, pollId: String?, List<Int>) -> Unit,
	navToProfile: (String) -> Unit,
	onAddFavorite: (String, String) -> Unit,
	onRemoveReaction: (String, String) -> Unit,
	onAddReaction: (String, String, String) -> Unit,
) {
	val pullRefreshState = rememberPullRefreshState(
		uiState.isLoading, { onPostRefresh(uiState.activeAccount, uiState.postId) })

	Box(
		modifier = Modifier
			.fillMaxSize()
			.padding(contentPadding)
			.pullRefresh(pullRefreshState)
	) {
		HorizontalDivider(modifier = Modifier.zIndex(1f), thickness = 1.dp)

		when (uiState) {
			is PostUiState.NoPost -> if (!uiState.isLoading) Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState()),
				verticalArrangement = Arrangement.Center,
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Text(text = "Post not found")
			}

			is PostUiState.HasPost -> Column(
				Modifier
					.fillMaxSize()
					.verticalScroll(rememberScrollState())
			) {
				Surface {
					SlimPostCard(
						post = uiState.post,
						onReply = onReply,
						onVotePoll = {
							onVotePoll(
								uiState.activeAccount, uiState.post.id, uiState.post.poll?.id, it
							)
						},
						navToPost = {},
						navToProfile = navToProfile,
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
