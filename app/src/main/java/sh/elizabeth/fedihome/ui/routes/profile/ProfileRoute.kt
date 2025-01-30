package sh.elizabeth.fedihome.ui.routes.profile

import android.content.res.Configuration
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import sh.elizabeth.fedihome.mock.defaultPost
import sh.elizabeth.fedihome.mock.defaultProfile
import sh.elizabeth.fedihome.ui.theme.FediHomeTheme

@Composable
fun ProfileRoute(
	profileViewModel: ProfileViewModel = hiltViewModel(),

	navBack: () -> Unit,
	navToCompose: (postId: String) -> Unit,
	navToPost: (postId: String) -> Unit,
	navToProfile: (profileTag: String) -> Unit,
) {
	val uiState by profileViewModel.uiState.collectAsStateWithLifecycle()

	ProfileRoute(
		uiState = uiState,
		navBack = navBack,
		onRefresh = profileViewModel::refreshProfile,
		onReply = navToCompose,
		onVotePoll = profileViewModel::votePoll,
		navToPost = navToPost,
		navToProfile = navToProfile,
		onAddFavorite = profileViewModel::addFavorite,
		onRemoveReaction = profileViewModel::removeReaction,
		onAddReaction = profileViewModel::addReaction,
	)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileRoute(
	uiState: ProfileUiState,
	navBack: () -> Unit,
	onRefresh: (activeAccount: String, profileTag: String, profileId: String?) -> Unit,
	onReply: (String) -> Unit,
	onVotePoll: (activeAccount: String, postId: String, pollId: String?, List<Int>) -> Unit,
	navToPost: (String) -> Unit,
	navToProfile: (String) -> Unit,
	onAddFavorite: (String, String) -> Unit,
	onRemoveReaction: (String, String) -> Unit,
	onAddReaction: (String, String, String) -> Unit,
) {
	LaunchedEffect(
		key1 = uiState.profileTag,
		key2 = uiState.activeAccount,
		key3 = if (uiState is ProfileUiState.HasProfile) uiState.profile.id else Unit
	) {
		if (uiState.activeAccount.isNotBlank()) onRefresh(
			uiState.activeAccount,
			uiState.profileTag,
			if (uiState is ProfileUiState.HasProfile) uiState.profile.id else null
		)

	}

	Scaffold(topBar = {
		TopAppBar(
			title = {
				Text(
					text = "", maxLines = 1, overflow = TextOverflow.Ellipsis
				)
			}, navigationIcon = {
				IconButton(
					onClick = navBack
				) {
					Icon(
						Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back"
					)
				}
			}, colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
		)
	}) { contentPadding ->
		ProfileScreen(
			uiState = uiState,
			onRefresh = onRefresh,
			contentPadding = contentPadding,
			onReply = onReply,
			onVotePoll = onVotePoll,
			navToPost = navToPost,
			navToProfile = navToProfile,
			onAddReaction = onAddReaction,
			onAddFavorite = onAddFavorite,
			onRemoveReaction = onRemoveReaction,
		)
	}
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ProfileRoutePreview() {
	FediHomeTheme {
		ProfileRoute(
			uiState = ProfileUiState.HasProfile(
				profileTag = "foo", profile = defaultProfile, posts = listOf(
				defaultPost
			), activeAccount = "foo", isLoading = false
		),
			navBack = {},
			onRefresh = { _, _, _ -> },
			onReply = {},
			onVotePoll = { _, _, _, _ -> },
			navToPost = {},
			navToProfile = {},
			onAddFavorite = { _, _ -> },
			onRemoveReaction = { _, _ -> },
			onAddReaction = { _, _, _ -> })
	}
}
