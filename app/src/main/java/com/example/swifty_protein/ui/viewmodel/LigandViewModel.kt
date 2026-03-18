package com.example.swifty_protein.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swifty_protein.data.ApiClient
import com.example.swifty_protein.data.Resource
import kotlinx.coroutines.launch

class LigandViewModel : ViewModel() {
    var ligandData by mutableStateOf<Resource<String>>(Resource.Loading)
        private set

    fun fetchLigand(id: String) {
        viewModelScope.launch {
            ligandData = Resource.Loading
            ligandData = ApiClient.safeApiCall {
                ApiClient.rcsbService.getLigandCif(id)
            }
        }
    }
}
