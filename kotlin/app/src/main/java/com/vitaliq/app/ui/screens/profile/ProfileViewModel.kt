package com.vitaliq.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vitaliq.app.data.api.RetrofitClient
import com.vitaliq.app.data.model.ProfileDto
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class ProfileUiState {
    object Loading : ProfileUiState()
    data class Success(
        val profile: ProfileDto,
        val saving: Boolean = false,
        val importing: Boolean = false,
        val snackbarMessage: String? = null
    ) : ProfileUiState()
    data class Error(val message: String) : ProfileUiState()
}

class ProfileViewModel : ViewModel() {

    private val api = RetrofitClient.apiService

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = api.getProfile()
                _uiState.value = ProfileUiState.Success(profile)
            } catch (e: Exception) {
                _uiState.value = ProfileUiState.Error(e.message ?: "Failed to load profile")
            }
        }
    }

    fun saveProfile(profile: ProfileDto) {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(saving = true)
            try {
                val updated = api.updateProfile(profile)
                _uiState.value = current.copy(profile = updated, saving = false, snackbarMessage = "Profile saved!")
            } catch (e: Exception) {
                _uiState.value = current.copy(saving = false, snackbarMessage = "Error: ${e.message}")
            }
        }
    }

    fun importFile(context: Context, uri: Uri) {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        viewModelScope.launch {
            _uiState.value = current.copy(importing = true)
            try {
                val inputStream = context.contentResolver.openInputStream(uri)
                    ?: throw Exception("Cannot open file")
                val bytes = inputStream.readBytes()
                inputStream.close()
                val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
                val fileName = uri.lastPathSegment ?: "import.json"
                val requestBody = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
                val part = MultipartBody.Part.createFormData("file", fileName, requestBody)
                api.importFile(part)
                _uiState.value = current.copy(importing = false, snackbarMessage = "Data imported successfully!")
            } catch (e: Exception) {
                _uiState.value = current.copy(importing = false, snackbarMessage = "Import failed: ${e.message}")
            }
        }
    }

    fun dismissSnackbar() {
        val current = _uiState.value as? ProfileUiState.Success ?: return
        _uiState.value = current.copy(snackbarMessage = null)
    }
}
