package com.example.swifty_protein.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swifty_protein.data.ApiClient
import com.example.swifty_protein.data.DbHelper
import com.example.swifty_protein.data.LigandRepository
import com.example.swifty_protein.data.Resource
import com.example.swifty_protein.model.Ligand
import kotlinx.coroutines.launch


class LigandViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = LigandRepository(DbHelper(application))

    var ligandData by mutableStateOf<Resource<Ligand>>(Resource.Loading)
        private set

    fun fetchLigand(id: String) {
        viewModelScope.launch {
            ligandData = Resource.Loading
            ligandData = repository.getLigandData(id)
        }
    }

    //for debug
    private val dbHelper = DbHelper(application)
    var ligandCount by mutableStateOf(0L)
        private set

    // Funzione per aggiornare il conteggio
    fun updateCount() {
        viewModelScope.launch {
            // Chiamiamo la funzione che abbiamo creato in DbHelper
            ligandCount = dbHelper.getLigandSize()
        }
    }

    // Chiamala nell'init per avere il valore all'avvio
    init {
        updateCount()
    }
}
