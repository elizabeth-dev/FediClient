package sh.elizabeth.fedihome.ui.composable

import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import coil.compose.AsyncImage
import coil.compose.rememberAsyncImagePainter
import com.vanniktech.blurhash.BlurHash
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import sh.elizabeth.fedihome.mock.defaultProfile
import sh.elizabeth.fedihome.model.Profile
import sh.elizabeth.fedihome.ui.theme.FediHomeTheme
import kotlin.math.roundToInt

//private const val HEADER_RATIO = 2.25f
private val AVATAR_SIZE = 48.dp

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProfilePreview(modifier: Modifier = Modifier, profile: Profile) {
	val resources = LocalContext.current.resources

	Surface(
		modifier = modifier.border(
			1.dp,
			color = MaterialTheme.colorScheme.primary.copy(alpha = 0.25f),
			shape = MaterialTheme.shapes.medium,
		),
		// FIXME
		color = MaterialTheme.colorScheme.primary.copy(
			alpha = 0.05f
		),
		contentColor = MaterialTheme.colorScheme.onSurface,
		shape = MaterialTheme.shapes.medium,
	) {
		BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
			val _maxWidth = maxWidth
			val headerWidthInPx =
				resources.displayMetrics.densityDpi.div(160f).times(maxWidth.value).roundToInt()
			var headerBlurHash by remember { mutableStateOf<Bitmap?>(null) }

			LaunchedEffect(profile.headerBlur) { // TODO: Maybe move this to viewModel everywhere a blurhash is calculated
				CoroutineScope(Dispatchers.IO).launch {
					headerBlurHash = BlurHash.decode(
						blurHash = profile.headerBlur ?: "",
						height = headerWidthInPx.div(HEADER_RATIO).roundToInt(),
						width = headerWidthInPx,
					)
				}
			}

			// Avatar
			BlurHashAvatar(
				imageUrl = profile.avatarUrl,
				imageBlur = profile.avatarBlur,
				imageSize = AVATAR_SIZE,
				modifier = Modifier
					.zIndex(1f)
					.offset(
						y = maxWidth
							.div(HEADER_RATIO)
							.minus(AVATAR_SIZE.div(2)), x = 16.dp
					)
			)

			Column(Modifier.fillMaxWidth()) {
				// Header
				if (profile.headerUrl != null) AsyncImage(model = profile.headerUrl,
					contentDescription = null,
					modifier = Modifier
						.width(_maxWidth)
						.aspectRatio(HEADER_RATIO),
					contentScale = ContentScale.Crop,
					placeholder = headerBlurHash?.let {
						rememberAsyncImagePainter(
							it, contentScale = ContentScale.Crop
						)
					}) else BoxWithConstraints( // FIXME: temporal placeholder
					modifier = Modifier
						.width(_maxWidth)
						.aspectRatio(HEADER_RATIO)
						.background(
							MaterialTheme.colorScheme.inverseSurface.copy(
								alpha = 0.25f
							)
						)
				) {}

				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(
							top = AVATAR_SIZE
								.div(2)
								.plus(8.dp), start = 16.dp, end = 16.dp, bottom = 12.dp
						),
					verticalArrangement = Arrangement.spacedBy(12.dp),
				) {
					Column {
						EnrichedText(
							text = profile.name ?: "",
							emojis = profile.emojis,
							emojiSize = 30.sp,
							style = MaterialTheme.typography.headlineSmall,
							allowClickable = false
						)
						Text(
							"@${profile.username}", style = MaterialTheme.typography.bodyLarge.copy(
								fontWeight = FontWeight.Medium
							), maxLines = 1, color = MaterialTheme.colorScheme.onSurface.copy(
								alpha = 0.6f
							)
						)
					}
					if (!profile.description.isNullOrBlank()) EnrichedText(
						text = profile.description,
						emojis = profile.emojis,
						style = MaterialTheme.typography.bodyLarge,
					)
				}
			}
		}
	}
}

@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES, showBackground = true)
@Preview(showBackground = true)
@Composable
fun ProfilePreviewPreview() {
	FediHomeTheme {
		ProfilePreview(
			modifier = Modifier
				.padding(8.dp)
				.fillMaxWidth(), profile = defaultProfile
		)
	}
}
