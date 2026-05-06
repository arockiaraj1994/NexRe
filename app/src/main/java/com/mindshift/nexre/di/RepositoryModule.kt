package com.mindshift.nexre.di

import com.mindshift.nexre.data.repository.LinkRepositoryImpl
import com.mindshift.nexre.data.repository.TagRepositoryImpl
import com.mindshift.nexre.domain.repository.LinkRepository
import com.mindshift.nexre.domain.repository.TagRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindLinkRepository(impl: LinkRepositoryImpl): LinkRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(impl: TagRepositoryImpl): TagRepository
}
