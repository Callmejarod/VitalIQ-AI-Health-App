package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.ProfileDto

interface ProfileRepository {
    suspend fun getProfile(): ProfileDto
    suspend fun updateProfile(dto: ProfileDto): ProfileDto

    /**
     * Import a previously-exported data file. The repository owns the transport
     * detail (HTTP multipart upload); callers pass only neutral file content, so
     * the ViewModel never references Retrofit/OkHttp types.
     */
    suspend fun importFile(fileName: String, mimeType: String, bytes: ByteArray)
}
