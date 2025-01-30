package sh.elizabeth.fedihome.data.database.entity

import sh.elizabeth.fedihome.EmojiEntity
import sh.elizabeth.fedihome.GetTimelinePosts
import sh.elizabeth.fedihome.model.Post
import sh.elizabeth.fedihome.model.Profile
import sh.elizabeth.fedihome.model.ProfileField

fun GetTimelinePosts.toPostDomain(emojiList: List<EmojiEntity>): Post {
	val emojis = emojiList.associate { Pair(it.shortcode, it.toDomain()) }
	return Post(
		id = postId_,
		createdAt = createdAt,
		updatedAt = updatedAt,
		cw = cw,
		text = text,
		author = Profile(
			id = profileId,
			username = username,
			instance = instance,
			name = name,
			description = description,
			following = following,
			followers = followers,
			postCount = postCount,
			createdAt = createdAt_,
			fields = fields.map {
				ProfileField(name = it.name, value = it.value)
			},
			avatarUrl = avatarUrl,
			avatarBlur = avatarBlur,
			headerUrl = headerUrl,
			headerBlur = headerBlur,
			emojis = emojis,
		),
		repostedBy = if (profileId_ != null) Profile(
			id = profileId_,
			username = username_!!,
			instance = instance_!!,
			name = name_,
			description = description_,
			following = following_,
			followers = followers_,
			postCount = postCount_,
			createdAt = createdAt__,
			fields = fields_!!.map {
				ProfileField(name = it.name, value = it.value)
			},
			avatarUrl = avatarUrl_,
			avatarBlur = avatarBlur_,
			headerUrl = headerUrl_,
			headerBlur = headerBlur_,
			emojis = emojis,
		) else null,
		quote = if (postId__ != null && profileId__ != null) Post(
			id = postId__,
			createdAt = createdAt___,
			updatedAt = updatedAt_,
			cw = cw_,
			text = text_,
			author = Profile(
				id = profileId__,
				username = username__!!,
				instance = instance__!!,
				name = name__,
				description = description__,
				following = following__,
				followers = followers__,
				postCount = postCount__,
				createdAt = createdAt__,
				fields = fields__!!.map {
					ProfileField(name = it.name, value = it.value)
				},
				avatarUrl = avatarUrl__,
				avatarBlur = avatarBlur__,
				headerUrl = headerUrl__,
				headerBlur = headerBlur__,
				emojis = emojis,
			),
			repostedBy = null,
			quote = null,
			poll = poll_?.toDomain(),
			emojis = emojis,
			reactions = reactions ?: emptyMap(),
			myReaction = myReaction
		) else null,
		poll = poll?.toDomain(),
		emojis = emojis,
		reactions = reactions ?: emptyMap(),
		myReaction = myReaction
	)
}