package com.mindshift.nexre.di

import android.content.Context
import androidx.room.Room
import com.mindshift.nexre.data.local.NexReDatabase
import com.mindshift.nexre.data.local.dao.LinkDao
import com.mindshift.nexre.data.local.dao.TagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NexReDatabase =
        Room.databaseBuilder(context, NexReDatabase::class.java, "nexre.db")
            .addMigrations(NexReDatabase.MIGRATION_1_2)
            .build()

    @Provides
    fun provideLinkDao(db: NexReDatabase): LinkDao = db.linkDao()

    @Provides
    fun provideTagDao(db: NexReDatabase): TagDao = db.tagDao()
}
