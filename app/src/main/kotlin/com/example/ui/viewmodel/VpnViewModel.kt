package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.models.VpnServer
import com.example.data.models.VpnStatus
import com.example.data.repository.VpnRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VpnViewModel(private val repository: VpnRepository) : ViewModel() {
    private val _vpnStatus = MutableStateFlow<VpnStatus>(VpnStatus.DISCONNECTED)
    val vpnStatus: StateFlow<VpnStatus> = _vpnStatus.asStateFlow()

    private val _connectedServer = MutableStateFlow<VpnServer?>(null)
    val connectedServer: StateFlow<VpnServer?> = _connectedServer.asStateFlow()

    private val _servers = MutableStateFlow<List<VpnServer>>(emptyList())
    val servers: StateFlow<List<VpnServer>> = _servers.asStateFlow()

    private val _selectedCountry = MutableStateFlow<String>("All")
    val selectedCountry: StateFlow<String> = _selectedCountry.asStateFlow()

    private val _connectionTime = MutableStateFlow<Long>(0L)
    val connectionTime: StateFlow<Long> = _connectionTime.asStateFlow()

    init {
        loadServers()
    }

    private fun loadServers() {
        viewModelScope.launch {
            repository.getAllServers().collect { serverList ->
                _servers.value = serverList
            }
        }
    }

    fun connectToServer(server: VpnServer) {
        viewModelScope.launch {
            _vpnStatus.value = VpnStatus.CONNECTING
            _connectedServer.value = server
            _connectionTime.value = System.currentTimeMillis()
            try {
                // Simulate connection delay
                kotlinx.coroutines.delay(2000)
                _vpnStatus.value = VpnStatus.CONNECTED
            } catch (e: Exception) {
                _vpnStatus.value = VpnStatus.ERROR
            }
        }
    }

    fun disconnect() {
        viewModelScope.launch {
            _vpnStatus.value = VpnStatus.DISCONNECTING
            try {
                kotlinx.coroutines.delay(1000)
                _vpnStatus.value = VpnStatus.DISCONNECTED
                _connectedServer.value = null
                _connectionTime.value = 0L
            } catch (e: Exception) {
                _vpnStatus.value = VpnStatus.ERROR
            }
        }
    }

    fun filterServersByCountry(country: String) {
        _selectedCountry.value = country
    }

    fun refreshServers() {
        viewModelScope.launch {
            repository.refreshServers()
        }
    }
}