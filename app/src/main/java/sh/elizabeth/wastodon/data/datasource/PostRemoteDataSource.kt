package sh.elizabeth.wastodon.data.datasource

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import sh.elizabeth.wastodon.api.firefish.PostFirefishApi
import sh.elizabeth.wastodon.api.firefish.model.toDomain
import sh.elizabeth.wastodon.api.mastodon.PostMastodonApi
import sh.elizabeth.wastodon.api.mastodon.model.toDomain
import sh.elizabeth.wastodon.model.Post
import sh.elizabeth.wastodon.model.PostDraft
import sh.elizabeth.wastodon.util.SupportedInstances
import javax.inject.Inject

class PostRemoteDataSource @Inject constructor(
	private val postFirefishApi: PostFirefishApi,
	private val postMastodonApi: PostMastodonApi,
) {
	suspend fun createPost(
		instance: String,
		instanceType: SupportedInstances,
		token: String,
		newPost: PostDraft,
	): Post = when (instanceType) {
		SupportedInstances.FIREFISH -> postFirefishApi.createPost(
			instance, token, newPost
		).createdNote.toDomain(instance)

		SupportedInstances.GLITCH,
		SupportedInstances.MASTODON,
		-> postMastodonApi.createPost(instance, token, newPost).toDomain(instance)

	}

	suspend fun fetchPost(
		instance: String,
		instanceType: SupportedInstances,
		token: String,
		postId: String,
	): Post = when (instanceType) {
		SupportedInstances.FIREFISH -> postFirefishApi.fetchPost(instance, token, postId)
			.toDomain(instance)

		SupportedInstances.GLITCH,
		SupportedInstances.MASTODON,
		-> postMastodonApi.fetchPost(instance, token, postId).toDomain(instance)
	}

	suspend fun fetchPostsByProfile(
		instance: String,
		instanceType: SupportedInstances,
		token: String,
		profileId: String,
	): List<Post> = when (instanceType) {
		SupportedInstances.FIREFISH -> postFirefishApi.fetchPostsByProfile(
			instance,
			token,
			profileId
		).map { it.toDomain(instance) }

		SupportedInstances.GLITCH,
		SupportedInstances.MASTODON,
		-> postMastodonApi.fetchPostsByProfile(instance, token, profileId)
			.map { it.toDomain(instance) }
	}

	suspend fun votePoll(
		instance: String,
		instanceType: SupportedInstances,
		token: String,
		pollId: String,
		choices: List<Int>,
	) = when (instanceType) {
		SupportedInstances.FIREFISH -> coroutineScope {
			val voteCoroutines = choices.map {
				async { postFirefishApi.votePoll(instance, token, pollId, it) }
			}

			voteCoroutines.awaitAll()
			return@coroutineScope
		}

		SupportedInstances.GLITCH,
		SupportedInstances.MASTODON,
		-> postMastodonApi.votePoll(instance, token, pollId, choices)
	}
}
