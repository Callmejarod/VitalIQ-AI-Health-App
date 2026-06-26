package com.vitaliq.app.data.repository

import com.vitaliq.app.data.model.ProfileDto
import okhttp3.MultipartBody
import okhttp3.ResponseBody

interface ProfileRepository {
    suspend fun getProfile(): ProfileDto
    suspend fun updateProfile(dto: ProfileDto): ProfileDto
    suspend fun importFile(part: MultipartBody.Part): ResponseBody
}
