package com.dapascript.memogram.di

import android.content.Context
import androidx.room.Room
import com.dapascript.memogram.BuildConfig
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.data.source.StoryRepository
import com.dapascript.memogram.data.source.StoryRepositoryImpl
import com.dapascript.memogram.data.source.UserRepository
import com.dapascript.memogram.data.source.UserRepositoryImpl
import com.dapascript.memogram.data.source.local.db.FeedDatabase
import com.dapascript.memogram.data.source.remote.network.ApiPaging
import com.dapascript.memogram.data.source.remote.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideApiService(): ApiService {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        if (!BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideApiPaging(): ApiPaging {
        val loggingInterceptor =
            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
        if (!BuildConfig.DEBUG) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE)
        }
        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
        val retrofit = Retrofit.Builder()
            .baseUrl("https://story-api.dicoding.dev/v1/")
            .addConverterFactory(MoshiConverterFactory.create())
            .client(client)
            .build()
        return retrofit.create(ApiPaging::class.java)
    }

    @Provides
    @Singleton
    fun provideDataStore(@ApplicationContext context: Context): UserPreference =
        UserPreference(context)

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): FeedDatabase =
        Room.databaseBuilder(
            context,
            FeedDatabase::class.java,
            "memogram.db"
        ).build()

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
    ): UserRepository = UserRepositoryImpl(apiService)

    @Provides
    @Singleton
    fun provideStoryRepository(
        apiPaging: ApiPaging,
        database: FeedDatabase
    ): StoryRepository = StoryRepositoryImpl(apiPaging, database)
}