package com.viz.prodzen.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.viz.prodzen.data.model.AppInfo
import com.viz.prodzen.data.repository.AppRepository
import com.viz.prodzen.data.repository.TimeRange
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val topApps: List<AppInfo> = emptyList(),
    val totalUsage: Long = 0L,
    val selectedTabIndex: Int = 0,
    val timeRanges: List<TimeRange> = listOf(TimeRange.TODAY, TimeRange.WEEK, TimeRange.MONTH)
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState = _uiState.asStateFlow()

    init {
        // Initial load is now triggered from the view to ensure permission is granted first.
    }

    fun onTimeRangeSelected(index: Int) {
        _uiState.update { it.copy(selectedTabIndex = index) }
        loadUsageStatsForCurrentRange()
    }

    fun loadUsageStatsForCurrentRange() {
        viewModelScope.launch {
            val selectedRange = _uiState.value.timeRanges[_uiState.value.selectedTabIndex]
            val allApps = repository.getAllAppsWithUsage(selectedRange)
            _uiState.update {
                it.copy(
                    topApps = allApps,
                    totalUsage = allApps.sumOf { app -> app.usageTodayMillis }
                )
            }
        }
    }
}

