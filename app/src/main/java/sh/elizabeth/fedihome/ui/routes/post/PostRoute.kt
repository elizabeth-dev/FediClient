package sh.elizabeth.fedihome.ui.routes.post

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.elizabeth.fedihome.mock.defaultPost
import sh.elizabeth.fedihome.ui.theme.FediHomeTheme

@Composable
fun PostRoute(
	postViewModel: PostViewModel = hiltViewModel(),
	navBack: () -> Unit,
	navToCompose: (replyId: String) -> Unit,
	navToProfile: (profileId: String) -> Unit,
) {
	val uiState by postViewModel.uiState.collectAsStateWithLifecycle()

	PostRoute(
		uiState = uiState,
		navBack = navBack,
		onPostRefresh = postViewModel::refreshPost,
		onReply = navToCompose,
		onVotePoll = postViewModel::votePoll,
		navToProfile = navToProfile,
		onAddFavorite = postViewModel::addFavorite,
		onRemoveReaction = postViewModel::removeReaction,
		onAddReaction = postViewModel::addReaction,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostRoute(
	uiState: PostUiState,
	navBack: () -> Unit,
	onPostRefresh: (activeAccount: String, postId: String) -> Unit,
	onReply: (String) -> Unit,
	onVotePoll: (activeAccount: String, postId: String, pollId: String?, List<Int>) -> Unit,
	navToProfile: (profileId: String) -> Unit,
	onAddFavorite: (String, String) -> Unit,
	onRemoveReaction: (String, String) -> Unit,
	onAddReaction: (String, String, String) -> Unit,
) {
	LaunchedEffect(key1 = uiState.activeAccount, key2 = uiState.postId) {
		if (uiState.activeAccount.isNotBlank()) onPostRefresh(
			uiState.activeAccount, uiState.postId
		)
	}

	Scaffold(topBar = {
		TopAppBar(title = {
			Text(
				text = "Post", maxLines = 1, overflow = TextOverflow.Ellipsis
			)
		}, navigationIcon = {
			IconButton(
				onClick = navBack
			) {
				Icon(
					Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back"
				)
			}
		})
	}) { contentPadding ->
		PostScreen(
			uiState = uiState,
			onPostRefresh = onPostRefresh,
			contentPadding = contentPadding,
			onReply = onReply,
			onVotePoll = onVotePoll,
			navToProfile = navToProfile,
			onAddFavorite = onAddFavorite,
			onRemoveReaction = onRemoveReaction,
			onAddReaction = onAddReaction,
		)

	}
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun PostRoutePreview() {
	FediHomeTheme {
		PostRoute(
			uiState = PostUiState.HasPost(
				postId = "foo", post = defaultPost, activeAccount = "foo", isLoading = false
		),
			navBack = {},
			onPostRefresh = { _, _ -> },
			onReply = {},
			onVotePoll = { _, _, _, _ -> },
			navToProfile = {},
			onAddFavorite = { _, _ -> },
			onRemoveReaction = { _, _ -> },
			onAddReaction = { _, _, _ -> })
	}
}
