package com.akhmilyas.test.notification

import center.basis.dion.call.service.CallServiceDependencies
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class MainModule {
    @Provides
    @Singleton
    fun provideCallServiceDependencies(): CallServiceDependencies = CallServiceDependenciesImpl()
}