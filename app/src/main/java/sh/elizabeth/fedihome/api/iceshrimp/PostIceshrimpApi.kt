package sh.elizabeth.fedihome.api.iceshrimp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sh.elizabeth.fedihome.api.iceshrimp.model.CreatePostRequest
import sh.elizabeth.fedihome.api.iceshrimp.model.CreatePostResponse
import sh.elizabeth.fedihome.api.iceshrimp.model.CreateReactionRequest
import sh.elizabeth.fedihome.api.iceshrimp.model.GetPostsByProfile
import sh.elizabeth.fedihome.api.iceshrimp.model.PollVoteRequest
import sh.elizabeth.fedihome.api.iceshrimp.model.Post
import sh.elizabeth.fedihome.api.iceshrimp.model.PostVisibility
import sh.elizabeth.fedihome.api.iceshrimp.model.SelectPostRequest
import sh.elizabeth.fedihome.model.PostDraft
import javax.inject.Inject
import sh.elizabeth.fedihome.model.PostVisibility as DomainPostVisibility

class PostIceshrimpApi @Inject constructor(private val httpClient: HttpClient) {
	suspend fun createPost(
		endpoint: String,
		token: String,
		newPost: PostDraft,
	): CreatePostResponse = httpClient.post("https://$endpoint/api/notes/create") {
		contentType(ContentType.Application.Json)
		bearerAuth(token)
		setBody(newPost.toCreatePost())
	}.body()

	suspend fun fetchPost(endpoint: String, token: String, postId: String): Post =
		httpClient.post("https://$endpoint/api/notes/show") {
			contentType(ContentType.Application.Json)
			bearerAuth(token)
			setBody(SelectPostRequest(noteId = postId.split('@').first()))
		}.body()

	suspend fun fetchPostsByProfile(
		endpoint: String,
		token: String,
		profileId: String,
	): List<Post> = httpClient.post("https://$endpoint/api/users/notes") {
		contentType(ContentType.Application.Json)
		bearerAuth(token)
		setBody(GetPostsByProfile(userId = profileId.split('@').first()))
	}.body()

	suspend fun votePoll(endpoint: String, token: String, postId: String, choice: Int) {
		httpClient.post(
			"https://$endpoint/api/notes/polls/vote"
		) {
			contentType(ContentType.Application.Json)
			bearerAuth(token)
			setBody(PollVoteRequest(noteId = postId.split('@').first(), choice = choice))
		}
	}

	suspend fun createReaction(
		endpoint: String,
		token: String,
		postId: String,
		emojiShortcode: String
	) {
		httpClient.post(
			"https://$endpoint/api/notes/reactions/create"
		) {
			contentType(ContentType.Application.Json)
			bearerAuth(token)
			setBody(
				CreateReactionRequest(
					noteId = postId.split('@').first(),
					reaction = emojiShortcode
				)
			)
		}
	}

	suspend fun deleteReaction(endpoint: String, token: String, postId: String) {
		httpClient.post(
			"https://$endpoint/api/notes/reactions/delete"
		) {
			contentType(ContentType.Application.Json)
			bearerAuth(token)
			setBody(SelectPostRequest(noteId = postId.split('@').first()))
		}
	}
}

fun PostDraft.toCreatePost() = CreatePostRequest(
	text = text,
	cw = cw,
	visibility = visibility.toIceshrimp(),
	visibleUserIds = visibleUserIds,
	localOnly = localOnly,
	replyId = replyId,
	renoteId = renoteId,
	channelId = channelId,
)

fun DomainPostVisibility.toIceshrimp(): PostVisibility = when (this) {
	DomainPostVisibility.PUBLIC -> PostVisibility.PUBLIC
	DomainPostVisibility.UNLISTED -> PostVisibility.HOME
	DomainPostVisibility.FOLLOWERS -> PostVisibility.FOLLOWERS
	DomainPostVisibility.MENTIONED -> PostVisibility.SPECIFIED
}
