package center.basis.dion.call.service

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
class CallDIModule {
    @Singleton
    @Provides
    fun provideCallInteractor(
        @ApplicationContext context: Context
    ): CallInteractor = CallInteractor(context)
}