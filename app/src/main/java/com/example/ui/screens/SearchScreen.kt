package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.models.Studio
import com.example.data.repositories.StudioFilter
import com.example.data.repositories.StudioRepository
import com.example.di.ServiceLocator
import com.example.ui.components.StudioCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class SearchViewModel(
    private val studioRepository: StudioRepository = ServiceLocator.studioRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedArea = MutableStateFlow<String?>(null)
    val selectedArea: StateFlow<String?> = _selectedArea.asStateFlow()

    private val _selectedService = MutableStateFlow<String?>(null)
    val selectedService: StateFlow<String?> = _selectedService.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StudioFilter.ALL)
    val selectedFilter: StateFlow<StudioFilter> = _selectedFilter.asStateFlow()

    private val _studios = MutableStateFlow<List<Studio>>(emptyList())
    val studios: StateFlow<List<Studio>> = _studios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeStudios()
    }

    private fun observeStudios() {
        viewModelScope.launch {
            combine(
                _searchQuery,
                _selectedArea,
                _selectedService,
                _selectedFilter
            ) { query, area, service, filter ->
                // Combine query and area for deep text matching
                val effectiveQuery = when {
                    query.isNotBlank() && area != null -> "$query $area"
                    area != null -> area
                    else -> query
                }
                Triple(effectiveQuery, filter, service)
            }.collectLatest { (query, filter, service) ->
                _isLoading.value = true
                studioRepository.getStudios(
                    searchQuery = query,
                    filter = filter,
                    serviceCategory = service
                ).collect { list ->
                    _studios.value = list
                    _isLoading.value = false
                }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectArea(area: String?) {
        _selectedArea.value = if (_selectedArea.value == area) null else area
    }

    fun selectService(service: String?) {
        _selectedService.value = if (_selectedService.value == service) null else service
    }

    fun setFilter(filter: StudioFilter) {
        _selectedFilter.value = filter
    }

    fun clearAll() {
        _searchQuery.value = ""
        _selectedArea.value = null
        _selectedService.value = null
        _selectedFilter.value = StudioFilter.ALL
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    onStudioClick: (String) -> Unit,
    viewModel: SearchViewModel = viewModel()
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedArea by viewModel.selectedArea.collectAsState()
    val selectedService by viewModel.selectedService.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val studios by viewModel.studios.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current

    val popularAreas = listOf(
        "Civil Lines",
        "Katra",
        "Naini",
        "Chowk",
        "George Town",
        "Ashok Nagar",
        "Tagore Town",
        "Govindpur",
        "Dhoomanganj"
    )

    val serviceCategories = listOf(
        "Candid",
        "Cinematic",
        "Drone",
        "Traditional",
        "Pre-Wedding",
        "Haldi",
        "Mehndi",
        "Wedding Photography"
    )

    Scaffold(
        containerColor = ThemeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(top = 16.dp, bottom = 32.dp)
        ) {
            // Screen Title
            item {
                Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)) {
                    Text(
                        text = "Find Studios in Prayagraj",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemeOnBackground,
                        letterSpacing = (-0.5).sp
                    )
                    Text(
                        text = "Search by studio name, locality, or photography services",
                        fontSize = 13.sp,
                        color = ThemeOnSurfaceVariant
                    )
                }
            }

            // Real-Time Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                        .height(56.dp)
                        .background(ThemeSurfaceVariant, RoundedCornerShape(50))
                        .border(1.dp, if (searchQuery.isNotEmpty()) ThemePrimary else ThemeOutline, RoundedCornerShape(50))
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    BasicTextField(
                        value = searchQuery,
                        onValueChange = { viewModel.setSearchQuery(it) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
                        textStyle = TextStyle(
                            color = ThemeOnBackground,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        ),
                        decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Default.Search,
                                    contentDescription = "Search",
                                    tint = if (searchQuery.isNotEmpty()) ThemePrimary else ThemeOnSurfaceVariant,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search studios, Katra, Civil Lines, Candid...",
                                            color = ThemeOnSurfaceVariant.copy(alpha = 0.7f),
                                            fontSize = 14.sp
                                        )
                                    }
                                    innerTextField()
                                }
                                if (searchQuery.isNotEmpty()) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = ThemeOnSurfaceVariant,
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .clickable {
                                                viewModel.setSearchQuery("")
                                                focusManager.clearFocus()
                                            }
                                    )
                                }
                            }
                        }
                    )
                }
            }

            // Locality / Area Filter Chips
            item {
                Column(modifier = Modifier.padding(top = 8.dp, bottom = 12.dp)) {
                    Text(
                        text = "POPULAR LOCALITIES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            AreaPill(
                                label = "All Areas",
                                isSelected = selectedArea == null,
                                onClick = { viewModel.selectArea(null) }
                            )
                        }
                        items(popularAreas) { area ->
                            AreaPill(
                                label = area,
                                isSelected = selectedArea == area,
                                onClick = { viewModel.selectArea(area) }
                            )
                        }
                    }
                }
            }

            // Service Category Filter Chips
            item {
                Column(modifier = Modifier.padding(bottom = 12.dp)) {
                    Text(
                        text = "SERVICES & STYLES",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = ThemePrimary,
                        letterSpacing = 1.sp,
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        item {
                            ServicePill(
                                label = "All Services",
                                isSelected = selectedService == null,
                                onClick = { viewModel.selectService(null) }
                            )
                        }
                        items(serviceCategories) { service ->
                            ServicePill(
                                label = service,
                                isSelected = selectedService == service,
                                onClick = { viewModel.selectService(service) }
                            )
                        }
                    }
                }
            }

            // Quick Sort / Status Filters
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        QuickFilterChip(
                            label = "All",
                            icon = null,
                            isSelected = selectedFilter == StudioFilter.ALL,
                            onClick = { viewModel.setFilter(StudioFilter.ALL) }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Top Rated",
                            icon = Icons.Default.Star,
                            isSelected = selectedFilter == StudioFilter.TOP_RATED,
                            onClick = { viewModel.setFilter(StudioFilter.TOP_RATED) }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Verified Only",
                            icon = Icons.Default.Verified,
                            isSelected = selectedFilter == StudioFilter.VERIFIED,
                            onClick = { viewModel.setFilter(StudioFilter.VERIFIED) }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Budget Friendly",
                            icon = null,
                            isSelected = selectedFilter == StudioFilter.BUDGET_FRIENDLY,
                            onClick = { viewModel.setFilter(StudioFilter.BUDGET_FRIENDLY) }
                        )
                    }
                    item {
                        QuickFilterChip(
                            label = "Near Me",
                            icon = Icons.Default.NearMe,
                            isSelected = selectedFilter == StudioFilter.NEAR_ME,
                            onClick = { viewModel.setFilter(StudioFilter.NEAR_ME) }
                        )
                    }
                }
            }

            // Results Header
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val activeFiltersCount = (if (searchQuery.isNotBlank()) 1 else 0) +
                            (if (selectedArea != null) 1 else 0) +
                            (if (selectedService != null) 1 else 0) +
                            (if (selectedFilter != StudioFilter.ALL) 1 else 0)

                    Column {
                        Text(
                            text = if (isLoading) "Searching studios..." else "${studios.size} Studios Found",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnBackground
                        )
                        if (selectedArea != null || selectedService != null) {
                            Text(
                                text = listOfNotNull(selectedArea, selectedService).joinToString(" • "),
                                fontSize = 12.sp,
                                color = ThemePrimary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (activeFiltersCount > 0) {
                        TextButton(
                            onClick = { viewModel.clearAll() },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Reset All", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // List of Studio Results
            if (isLoading) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = ThemePrimary)
                    }
                }
            } else if (studios.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(72.dp)
                                .background(ThemeSurfaceVariant, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.SearchOff,
                                contentDescription = null,
                                tint = ThemeOnSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No studios found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (searchQuery.isNotBlank())
                                "No studios match \"$searchQuery\". Try checking for typos or searching by area like Civil Lines or Katra."
                            else
                                "No studios available for the selected filters in Prayagraj.",
                            fontSize = 13.sp,
                            color = ThemeOnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearAll() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Clear All Filters", color = ThemeOnPrimary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else {
                items(studios, key = { it.id }) { studio ->
                    StudioCard(
                        studio = studio,
                        onClick = { onStudioClick(studio.id) },
                        modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AreaPill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) ThemePrimary else ThemeSurfaceVariant
    val contentColor = if (isSelected) ThemeOnPrimary else ThemeOnSurface
    val borderColor = if (isSelected) ThemePrimary else ThemeOutline

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.LocationOn,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}

@Composable
fun ServicePill(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) ThemePrimary else ThemeSurfaceVariant
    val contentColor = if (isSelected) ThemeOnPrimary else ThemeOnSurface
    val borderColor = if (isSelected) ThemePrimary else ThemeOutline

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp)
        )
    }
}

@Composable
fun QuickFilterChip(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) ThemePrimary.copy(alpha = 0.15f) else ThemeSurfaceVariant
    val contentColor = if (isSelected) ThemePrimary else ThemeOnSurface
    val borderColor = if (isSelected) ThemePrimary else ThemeOutline

    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(20.dp),
        color = containerColor,
        border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = contentColor
            )
        }
    }
}
