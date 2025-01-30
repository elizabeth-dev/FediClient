package sh.elizabeth.fedihome.model

import java.time.Instant

data class Post(
	val id: String,
	val createdAt: Instant?, // Sometimes null on Calckey
	val updatedAt: Instant?,
	val cw: String?,
	val text: String?,
	val author: Profile,
	val repostedBy: Profile?,
	val quote: Post?,
	val poll: Poll?,
	val reactions: Map<String, Int>,
	val myReaction: String?,
	val emojis: Map<String, Emoji>,
)

fun Post.unwrapQuotes(): List<Post> {
	val quotes = mutableListOf(this)
	var currentQuote = quote
	while (currentQuote != null) {
		quotes.add(0, currentQuote)
		currentQuote = currentQuote.quote
	}
	return quotes
}

fun Post.unwrapProfiles(): List<Profile> =
	if (repostedBy == null) listOf(author) else listOf(author, repostedBy)

data class Poll(
	val id: String?,
	val voted: Boolean,
	val choices: List<PollChoice>,
	val expiresAt: Instant?,
	val multiple: Boolean,
)

data class PollChoice(val text: String, val votes: Int?, val isVoted: Boolean)
