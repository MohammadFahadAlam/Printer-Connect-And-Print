package com.ft.printerconnectandprint

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ft.printerconnectandprint.printer.settings_data_store.SettingsDataStore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch


class AppViewModel(context: Context) : ViewModel() {
    private var dataStorePres: SettingsDataStore = SettingsDataStore(context)

    private var _printerSizeObserver = MutableStateFlow<String>("48mm")
    var printerSizeObserver: StateFlow<String> = _printerSizeObserver

    fun getPrinterSize() = viewModelScope.launch{
        dataStorePres.printerSizeFlow.collectLatest {
            _printerSizeObserver.value = it
        }
    }


}