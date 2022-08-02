package org.hyperskill.app.notification.data.repository

import org.hyperskill.app.notification.data.source.NotificationCacheDataSource
import org.hyperskill.app.notification.domain.repository.NotificationRepository

class NotificationRepositoryImpl(
    private val notificationCacheDataSource: NotificationCacheDataSource
) : NotificationRepository {
    override suspend fun getNotificationsEnabled() = notificationCacheDataSource.getNotificationsEnabled()

    override suspend fun getNotificationsTimestamp() = notificationCacheDataSource.getNotificationsTimestamp()
}