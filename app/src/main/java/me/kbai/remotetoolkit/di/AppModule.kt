package me.kbai.remotetoolkit.di

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import me.kbai.remotetoolkit.tool.ErrorHandleAdapterFactory
import javax.inject.Singleton

/**
 * @author sean 2022/11/24
 */
@Module
@InstallIn(SingletonComponent::class)
class AppModule {

    @Singleton
    @Provides
    fun provideGson(): Gson = GsonBuilder()
        .registerTypeAdapterFactory(ErrorHandleAdapterFactory())
        .create()
}