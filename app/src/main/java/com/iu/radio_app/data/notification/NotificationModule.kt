package com.iu.radio_app.data.notification

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/** Legt fest, welche Implementierung hinter [NotificationRepository] steckt (ADR 003). */
@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Binds
    abstract fun bindNotificationRepository(impl: StubNotificationRepository): NotificationRepository
}
