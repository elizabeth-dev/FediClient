package sh.elizabeth.fedihome.api.iceshrimp.model

import kotlinx.serialization.Serializable

@Serializable
data class CreateReactionRequest(val noteId: String, val reaction: String)