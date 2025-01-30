package sh.elizabeth.fedihome.ui.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import sh.elizabeth.fedihome.model.Emoji
import sh.elizabeth.fedihome.util.DEFAULT_EMOJI_SIZE


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReactionButton(
	icon: String,
	count: Int,
	selected: Boolean,
	emojis: Map<String, Emoji>,
) {
	val emoji = emojis[icon]

	val emojiSize = with(LocalDensity.current) {
		DEFAULT_EMOJI_SIZE.toDp()
	}

	Row(
		modifier = Modifier
			.clip(RoundedCornerShape(4.dp))
			.background(
				if (selected) MaterialTheme.colorScheme.primaryContainer
				else MaterialTheme.colorScheme.surface
			)
			.padding(vertical = 4.dp, horizontal = 8.dp),
		verticalAlignment = Alignment.CenterVertically,
		horizontalArrangement = Arrangement.spacedBy(6.dp),
	) {
		if (emoji != null) AsyncImage(
			model = emoji.url,
			contentDescription = icon.split('@')[0],
			modifier = Modifier.size(emojiSize),
		)
		else Text(text = icon)

		Text(
			text = count.toString(),
			color = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
		)
	}
}