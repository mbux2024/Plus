package com.homeflix.tv.data.repository

import com.homeflix.tv.domain.repository.MediaRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * COMPATIBILITY STUB — Provides old MediaRepository interface for legacy components.
 * All default interface methods return empty/no-op results.
 * TODO: Remove once VideoPlayer.kt is migrated.
 */
@Singleton
class MediaRepositoryStub @Inject constructor() : MediaRepository
