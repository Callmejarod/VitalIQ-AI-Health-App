package com.vitaliq.app.data.repository

import com.vitaliq.app.data.api.ApiService
import com.vitaliq.app.data.local.dao.ProfileDao
import com.vitaliq.app.data.local.entity.toDto
import com.vitaliq.app.data.local.entity.toEntity
import com.vitaliq.app.data.model.ProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.ResponseBody

class ProfileRepositoryImpl(
    private val api: ApiService,
    private val dao: ProfileDao
) : ProfileRepository {

    override suspend fun getProfile(): ProfileDto = withContext(Dispatchers.IO) {
        try {
            val dto = api.getProfile()
            dao.insert(dto.toEntity())
            dto
        } catch (e: Exception) {
            dao.get()?.toDto() ?: throw e
        }
    }

    override suspend fun updateProfile(dto: ProfileDto): ProfileDto = withContext(Dispatchers.IO) {
        val updated = api.updateProfile(dto)
        dao.insert(updated.toEntity())
        updated
    }

    override suspend fun importFile(part: MultipartBody.Part): ResponseBody = withContext(Dispatchers.IO) {
        api.importFile(part)
    }
}
