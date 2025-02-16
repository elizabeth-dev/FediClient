package sh.elizabeth.fedihome.api.iceshrimp

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import sh.elizabeth.fedihome.api.iceshrimp.model.Notification
import sh.elizabeth.fedihome.api.sharkey.model.GetNotificationsRequest
import javax.inject.Inject

class NotificationIceshrimpApi @Inject constructor(private val httpClient: HttpClient) {
	suspend fun getNotifications(endpoint: String, token: String): List<Notification> =
		httpClient.post("https://$endpoint/api/i/notifications") {
			contentType(ContentType.Application.Json)
			bearerAuth(token)
			setBody(GetNotificationsRequest())
		}.body()
}
