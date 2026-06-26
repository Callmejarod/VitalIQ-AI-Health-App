package com.vitaliq.app.ui.screens.profile

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.vitaliq.app.data.model.ProfileDto
import com.vitaliq.app.data.repository.ProfileRepository
import com.vitaliq.app.di.ServiceLocator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

class ProfileViewModel(
    private val repo: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProfileUiState>(ProfileUiState.Loading)
    val uiState: StateFlow<ProfileUiState> = _uiState

    init {
        Log.d("VitalIQ", "ProfileViewModel created")
    }

    fun load() {
        viewModelScope.launch {
            _uiState.value = ProfileUiState.Loading
            try {
                val profile = repo.getProfile()
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
                val updated = repo.updateProfile(profile)
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
                // Reading the file (an Android content concern) happens off the main
                // thread here; the repository handles how the bytes are transmitted.
                val (fileName, mimeType, bytes) = withContext(Dispatchers.IO) {
                    val inputStream = context.contentResolver.openInputStream(uri)
                        ?: throw Exception("Cannot open file")
                    val data = inputStream.use { it.readBytes() }
                    val mime = context.contentResolver.getType(uri) ?: "application/octet-stream"
                    val name = uri.lastPathSegment ?: "import.json"
                    Triple(name, mime, data)
                }
                repo.importFile(fileName, mimeType, bytes)
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

    override fun onCleared() {
        super.onCleared()
        Log.d("VitalIQ", "ProfileViewModel cleared")
    }

    companion object {
        // Context here is used only for content-resolver file reads in importFile,
        // never for data access — that goes through ProfileRepository.
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                ProfileViewModel(ServiceLocator.profileRepository)
            }
        }
    }
}
