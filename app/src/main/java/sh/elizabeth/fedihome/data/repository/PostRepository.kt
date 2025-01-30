package sh.elizabeth.fedihome.data.repository

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import sh.elizabeth.fedihome.PostEmojiCrossRef
import sh.elizabeth.fedihome.ProfileEmojiCrossRef
import sh.elizabeth.fedihome.data.datasource.EmojiLocalDataSource
import sh.elizabeth.fedihome.data.datasource.InternalDataLocalDataSource
import sh.elizabeth.fedihome.data.datasource.PostLocalDataSource
import sh.elizabeth.fedihome.data.datasource.PostRemoteDataSource
import sh.elizabeth.fedihome.model.Post
import sh.elizabeth.fedihome.model.PostDraft
import sh.elizabeth.fedihome.model.unwrapProfiles
import sh.elizabeth.fedihome.model.unwrapQuotes
import sh.elizabeth.fedihome.util.InstanceEndpointTypeToken
import javax.inject.Inject

class PostRepository @Inject constructor(
	private val postLocalDataSource: PostLocalDataSource,
	private val profileRepository: ProfileRepository,
	private val emojiLocalDataSource: EmojiLocalDataSource,
	private val postRemoteDataSource: PostRemoteDataSource,
	private val internalDataLocalDataSource: InternalDataLocalDataSource,
) {
	private suspend fun getInstanceAndEndpointAndTypeAndToken(activeAccount: String): InstanceEndpointTypeToken =
		activeAccount.let {
			val internalData = internalDataLocalDataSource.internalData.first()
			val instance = it.split('@')[1]
			InstanceEndpointTypeToken(
				instance,
				internalData.instances[instance]?.delegatedEndpoint!!,
				internalData.instances[instance]?.instanceType!!,
				internalData.accounts[it]?.accessToken!!
			)
		}

	fun insertOrReplace(vararg posts: Post) {
		postLocalDataSource.insertOrReplace(*posts)
	}

	fun insertOrReplaceEmojiCrossRef(vararg refs: PostEmojiCrossRef) {
		postLocalDataSource.insertOrReplaceEmojiCrossRef(*refs)
	}

	suspend fun createPost(activeAccount: String, newPost: PostDraft) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		val posts = postRemoteDataSource.createPost(
			instance = instanceData.instance,
			endpoint = instanceData.endpoint,
			instanceType = instanceData.instanceType,
			token = instanceData.token,
			newPost = newPost
		).unwrapQuotes()

		handleInsertPosts(posts)
	}

	fun getPost(postId: String): Post? = postLocalDataSource.getPostSingle(postId)

	fun getPostFlow(postId: String) = postLocalDataSource.getPost(postId)

	fun getPostsByProfileFlow(profileId: String) = postLocalDataSource.getPostsByProfile(profileId)

	private suspend fun handleInsertPosts(posts: List<Post>) {
		val profiles = posts.flatMap { it.unwrapProfiles() }.toSet()
		val emojis =
			posts.flatMap { it.emojis.values }.plus(profiles.flatMap { it.emojis.values }).toSet()
		val postEmojiCrossRefs = posts.flatMap { post ->
			post.emojis.values.map { emoji ->
				PostEmojiCrossRef(
					postId = post.id, emojiId = emoji.fullEmojiId
				)
			}
		}
		val profileEmojiCrossRefs = profiles.flatMap { profile ->
			profile.emojis.values.map { emoji ->
				ProfileEmojiCrossRef(
					profileId = profile.id, emojiId = emoji.fullEmojiId
				)
			}
		}

		coroutineScope {
			val emojiRef = async { emojiLocalDataSource.insertOrReplace(*emojis.toTypedArray()) }

			profileRepository.insertOrReplace(*profiles.toTypedArray())
			postLocalDataSource.insertOrReplace(*posts.toTypedArray())

			emojiRef.await()

			val postEmojiRef =
				async { postLocalDataSource.insertOrReplaceEmojiCrossRef(*postEmojiCrossRefs.toTypedArray()) }
			val profileEmojiRef =
				async { profileRepository.insertOrReplaceEmojiCrossRef(*profileEmojiCrossRefs.toTypedArray()) }

			awaitAll(postEmojiRef, profileEmojiRef)
		}
	}

	suspend fun fetchPost(
		activeAccount: String,
		postId: String,
	) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		val posts = postRemoteDataSource.fetchPost(
			instance = instanceData.instance,
			endpoint = instanceData.endpoint,
			instanceType = instanceData.instanceType,
			token = instanceData.token,
			postId = postId.split('@').first()
		).unwrapQuotes()

		handleInsertPosts(posts)
	}

	suspend fun fetchPostsByProfile(
		activeAccount: String,
		profileId: String,
	) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		val posts = postRemoteDataSource.fetchPostsByProfile(
			instance = instanceData.instance,
			endpoint = instanceData.endpoint,
			instanceType = instanceData.instanceType,
			token = instanceData.token,
			profileId = profileId.split('@').first()
		).flatMap { it.unwrapQuotes() }

		handleInsertPosts(posts)
	}

	suspend fun votePoll(
		activeAccount: String,
		postId: String,
		choices: List<Int>,
	) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		postRemoteDataSource.votePoll(
			instanceData.endpoint,
			instanceData.instanceType,
			instanceData.token,
			postId.split('@').first(),
			choices
		)
	}

	suspend fun deleteReaction(
		activeAccount: String,
		postId: String,
	) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		val posts = postRemoteDataSource.deleteReaction(
			instanceData.endpoint,
			instanceData.instance,
			instanceData.instanceType,
			instanceData.token,
			postId.split('@').first()
		).unwrapQuotes()

		handleInsertPosts(posts)
	}

	suspend fun createReaction(
		activeAccount: String,
		postId: String,
		emojiShortcode: String,
	) {
		val instanceData = getInstanceAndEndpointAndTypeAndToken(activeAccount)

		val posts = postRemoteDataSource.createReaction(
			instanceData.endpoint,
			instanceData.instance,
			instanceData.instanceType,
			instanceData.token,
			postId.split('@').first(),
			emojiShortcode
		).unwrapQuotes()

		handleInsertPosts(posts)
	}
}
