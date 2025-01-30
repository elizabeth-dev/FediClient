package sh.elizabeth.fedihome.di

import android.content.Context
import androidx.sqlite.db.SupportSQLiteDatabase
import app.cash.sqldelight.ColumnAdapter
import app.cash.sqldelight.EnumColumnAdapter
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import sh.elizabeth.fedihome.NotificationEntity
import sh.elizabeth.fedihome.PostEntity
import sh.elizabeth.fedihome.ProfileEntity
import sh.elizabeth.fedihome.data.database.AppDatabase
import sh.elizabeth.fedihome.data.database.entity.PollEntity
import sh.elizabeth.fedihome.data.database.entity.ProfileFieldEntity
import java.time.Instant
import javax.inject.Singleton

val pollEntityAdapter = object : ColumnAdapter<PollEntity, String> {
	override fun decode(databaseValue: String): PollEntity {
		val type = object : TypeToken<PollEntity>() {}.type
		return Gson().fromJson(databaseValue, type)
	}

	override fun encode(value: PollEntity): String {
		val type = object : TypeToken<PollEntity>() {}.type
		return Gson().toJson(value, type)
	}
}

val profileFieldEntityListAdapter = object : ColumnAdapter<List<ProfileFieldEntity>, String> {
	override fun decode(databaseValue: String): List<ProfileFieldEntity> {
		val type = object : TypeToken<List<ProfileFieldEntity>>() {}.type
		return Gson().fromJson(databaseValue, type)
	}

	override fun encode(value: List<ProfileFieldEntity>): String {
		val type = object : TypeToken<List<ProfileFieldEntity>>() {}.type
		return Gson().toJson(value, type)
	}
}

val mapAdapter = object : ColumnAdapter<Map<String, Int>, String> {
	override fun decode(databaseValue: String): Map<String, Int> {
		val type = object : TypeToken<Map<String, Int>>() {}.type
		return Gson().fromJson(databaseValue, type)
	}

	override fun encode(value: Map<String, Int>): String {
		val type = object : TypeToken<Map<String, Int>>() {}.type
		return Gson().toJson(value, type)
	}
}

val instantAdapter = object : ColumnAdapter<Instant, Long> {
	override fun decode(databaseValue: Long): Instant = Instant.ofEpochMilli(databaseValue)

	override fun encode(value: Instant): Long = value.toEpochMilli()
}

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
	@Provides
	@Singleton
	fun provideAppDatabase(
		@ApplicationContext context: Context,
	): AppDatabase = AppDatabase(
		driver = AndroidSqliteDriver(
			schema = AppDatabase.Schema,
			context = context,
			name = "app-database",
			callback = object : AndroidSqliteDriver.Callback(
				AppDatabase.Schema
			) {
				override fun onOpen(db: SupportSQLiteDatabase) {
					super.onOpen(db)
					db.setForeignKeyConstraintsEnabled(
						true
					)
				}
			}),

		NotificationEntityAdapter = NotificationEntity.Adapter(
			typeAdapter = EnumColumnAdapter(), createdAtAdapter = instantAdapter
		), PostEntityAdapter = PostEntity.Adapter(
			pollAdapter = pollEntityAdapter,
			createdAtAdapter = instantAdapter,
			updatedAtAdapter = instantAdapter,
			reactionsAdapter = mapAdapter,
		), ProfileEntityAdapter = ProfileEntity.Adapter(
			fieldsAdapter = profileFieldEntityListAdapter, createdAtAdapter = instantAdapter
		),
	)
}
