package sh.elizabeth.fedihome.ui.composable

import android.os.Build
import android.util.Patterns
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.InlineTextContent
import androidx.compose.foundation.text.appendInlineContent
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.Placeholder
import androidx.compose.ui.text.PlaceholderVerticalAlign
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.TextUnit
import coil.compose.AsyncImage
import sh.elizabeth.fedihome.model.Emoji
import sh.elizabeth.fedihome.util.DEFAULT_EMOJI_SIZE
import java.util.regex.Pattern

val contentPattern =
	":(?<emoji>[\\w-]+):|(?<url>(\\s|^)${Patterns.WEB_URL.pattern()})|(?<mention>(\\s|^)@\\w+(@${Patterns.DOMAIN_NAME})?)"
val contentRegex = Pattern.compile(contentPattern)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EnrichedText(
	text: String,
	modifier: Modifier = Modifier,
	emojiSize: TextUnit = DEFAULT_EMOJI_SIZE,
	emojis: Map<String, Emoji> = emptyMap(),
	overflow: TextOverflow = TextOverflow.Clip,
	softWrap: Boolean = true,
	maxLines: Int = Int.MAX_VALUE,
	minLines: Int = 1,
	style: TextStyle = LocalTextStyle.current,
	color: Color = Color.Unspecified,
	navToProfileTag: (profileTag: String) -> Unit = {},
	instance: String? = null,
	allowClickable: Boolean = true,
) {
	val _emojiSize = with(LocalDensity.current) {
		emojiSize.toDp()
	}
	val linkStyles = TextLinkStyles(style = SpanStyle(color = MaterialTheme.colorScheme.primary))

	Text(
		text = buildAnnotatedString {
			var carriageReturn = 0
			val matcher = contentRegex.matcher(text)

			while (matcher.find()) {
				append(text.substring(carriageReturn, matcher.start()))
				val currentCarriageReturn = carriageReturn
				carriageReturn = matcher.end()

				val emoji = matcher.group("emoji")
				if (emoji != null) {
					appendInlineContent(emoji, ":$emoji:")
					continue
				}

				if (allowClickable) {
					val url = matcher.group("url")
					if (url != null) {
						val cleanUrl = if (currentCarriageReturn != 0) url.substring(1)
							.also { append(url[0]) } else url

						withLink(
							LinkAnnotation.Url(
								url = cleanUrl, styles = linkStyles
							)
						) { append(cleanUrl) }
						continue
					}

					val mention = matcher.group("mention")
					if (mention != null) {
						val cleanTag = if (currentCarriageReturn != 0) mention.substring(1)
							.also { append(mention[0]) } else mention

						withLink(
							link = LinkAnnotation.Clickable(
								tag = cleanTag, styles = linkStyles
							) {
								navToProfileTag(
									cleanTag.substring(1)
										.let { if (it.contains('@')) it else "$it@$instance" })
							}) {
							append(cleanTag)
						}
						continue
					}
				} else {
					append(matcher.group())
				}
			}

			if (carriageReturn < text.length) append(text.substring(carriageReturn, text.length))
		},
		modifier = modifier,
		overflow = overflow,
		softWrap = softWrap,
		maxLines = maxLines,
		minLines = minLines,
		style = style,
		color = color,
		inlineContent = emojis.map { (key, emoji) ->
			key to InlineTextContent(
				placeholder = Placeholder(
					width = emojiSize,
					height = emojiSize,
					placeholderVerticalAlign = PlaceholderVerticalAlign.Center
				),
			) {
				AsyncImage(
					model = emoji.url,
					contentDescription = key,
					modifier = Modifier.size(_emojiSize)
				)
			}
		}.toMap(),
	)
}
