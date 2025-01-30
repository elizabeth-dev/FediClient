package sh.elizabeth.fedihome.util

import androidx.compose.ui.unit.sp
import kotlinx.serialization.SerialName

const val APP_NAME = "Test Fedi Client"
const val APP_DESCRIPTION = "Test Fedi Client by @elizabeth@tech.lgbt"

val ICESHRIMP_APP_PERMISSION = listOf(
	"write:user-groups",
	"read:user-groups",
	"read:page-likes",
	"write:page-likes",
	"write:pages",
	"read:pages",
	"write:votes",
	"write:reactions",
	"read:reactions",
	"write:notifications",
	"read:notifications",
	"write:notes",
	"write:mutes",
	"read:mutes",
	"read:account",
	"write:account",
	"read:blocks",
	"write:blocks",
	"read:drive",
	"write:drive",
	"read:favorites",
	"write:favorites",
	"read:following",
	"write:following",
	"read:messaging",
	"write:messaging",
)

val MASTODON_APP_PERMISSION = listOf(
	"read",
	"write",
	"push",
)

const val APP_DEEPLINK_URI = "app://sh.elizabeth.fedihome"
const val APP_LOGIN_OAUTH_PATH = "/login/oauth"

enum class SupportedInstances {
	@SerialName("sharkey")
	SHARKEY,

	@SerialName("iceshrimp")
	ICESHRIMP,

	@SerialName("mastodon")
	MASTODON,

	@SerialName("glitch-soc")
	GLITCH,
}

val DEFAULT_EMOJI_SIZE = 24.sp
const val DEFAULT_FAVORITE_EMOJI = "⭐"