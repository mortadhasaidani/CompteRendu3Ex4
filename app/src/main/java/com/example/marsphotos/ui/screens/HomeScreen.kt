package com.example.marsphotos.ui.screens

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.marsphotos.MarsPhotosApplication
import com.example.marsphotos.data.MarsPhotosRepository
import com.example.marsphotos.model.MarsPhoto
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

/**
 * Represents the UI state for the Home screen.
 */
sealed interface MarsUiState {
    data class Success(val photos: List<MarsPhoto>) : MarsUiState
    object Error : MarsUiState
    object Loading : MarsUiState
}

class MarsViewModel(
    private val marsPhotosRepository: MarsPhotosRepository
) : ViewModel() {

    /** Holds the current UI state. */
    var uiState: MarsUiState by mutableStateOf(MarsUiState.Loading)
        private set

    init {
        fetchMarsPhotos()
    }

    /**
     * Public method to initiate photo fetch and update state.
     */
    fun fetchMarsPhotos() {
        viewModelScope.launch {
            uiState = MarsUiState.Loading
            val result = loadPhotosFromApi()
            uiState = result
        }
    }

    /**
     * Private function to perform the API call and handle errors.
     */
    private suspend fun loadPhotosFromApi(): MarsUiState {
        return try {
            val photos = marsPhotosRepository.getMarsPhotos()
            MarsUiState.Success(photos)
        } catch (e: IOException) {
            MarsUiState.Error
        } catch (e: HttpException) {
            MarsUiState.Error
        }
    }

    /**
     * ViewModel factory with dependency injection.
     */
    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as MarsPhotosApplication
                val repository = application.container.marsPhotosRepository
                MarsViewModel(repository)
            }
        }
    }
}
