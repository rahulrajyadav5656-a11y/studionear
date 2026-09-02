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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Verified
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
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

class HomeViewModel(
    private val studioRepository: StudioRepository = ServiceLocator.studioRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedFilter = MutableStateFlow(StudioFilter.ALL)
    val selectedFilter: StateFlow<StudioFilter> = _selectedFilter.asStateFlow()

    private val _selectedCategory = MutableStateFlow<String?>(null)
    val selectedCategory: StateFlow<String?> = _selectedCategory.asStateFlow()

    private val _studios = MutableStateFlow<List<Studio>>(emptyList())
    val studios: StateFlow<List<Studio>> = _studios.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    init {
        observeStudios()
    }

    private fun observeStudios() {
        viewModelScope.launch {
            _isLoading.value = true
            combine(
                studioRepository.getStudios(searchQuery = "", filter = StudioFilter.ALL, serviceCategory = null),
                _searchQuery,
                _selectedFilter,
                _selectedCategory
            ) { rawList, query, filter, category ->
                filterAndSortStudios(rawList, query, filter, category)
            }.collect { filteredList ->
                _studios.value = filteredList
                _isLoading.value = false
            }
        }
    }

    private fun filterAndSortStudios(
        rawStudios: List<Studio>,
        searchQuery: String,
        filter: StudioFilter,
        serviceCategory: String?
    ): List<Studio> {
        var result = rawStudios
        // 1. Filter by Service Category if specified
        if (!serviceCategory.isNullOrBlank()) {
            val catLower = serviceCategory.trim().lowercase()
            result = result.filter { studio ->
                studio.services.any { it.lowercase().contains(catLower) } ||
                studio.servicesOffered.any { it.lowercase().contains(catLower) } ||
                studio.name.lowercase().contains(catLower) ||
                studio.description.lowercase().contains(catLower)
            }
        }
        
        // 2. Filter by Search Query
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.trim().lowercase()
            result = result.filter { studio ->
                studio.name.lowercase().contains(query) ||
                studio.area.lowercase().contains(query) ||
                studio.city.lowercase().contains(query) ||
                (studio.location?.address?.lowercase()?.contains(query) == true) ||
                (studio.location?.area?.lowercase()?.contains(query) == true) ||
                studio.services.any { it.lowercase().contains(query) } ||
                studio.servicesOffered.any { it.lowercase().contains(query) } ||
                studio.description.lowercase().contains(query)
            }
        }
        
        // 3. Apply Filter Option & Prioritization
        return when (filter) {
            StudioFilter.ALL -> {
                result.sortedWith(
                    compareByDescending<Studio> { it.isSponsoredActive }
                        .thenByDescending { it.isVerifiedActive }
                        .thenByDescending { it.rating }
                        .thenByDescending { it.reviewCount }
                )
            }
            StudioFilter.TOP_RATED -> {
                result.filter { it.rating >= 4.0 }
                    .sortedByDescending { it.rating }
            }
            StudioFilter.VERIFIED -> {
                result.filter { it.isVerifiedActive }
                    .sortedByDescending { it.rating }
            }
            StudioFilter.NEAR_ME -> {
                result.sortedBy { it.distanceKm }
            }
            StudioFilter.BUDGET_FRIENDLY -> {
                result.sortedBy { it.startingPrice }
            }
        }
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun setFilter(filter: StudioFilter) {
        _selectedFilter.value = filter
    }

    fun toggleCategory(category: String) {
        if (_selectedCategory.value == category) {
            _selectedCategory.value = null
        } else {
            _selectedCategory.value = category
        }
    }

    fun clearAllFilters() {
        _searchQuery.value = ""
        _selectedFilter.value = StudioFilter.ALL
        _selectedCategory.value = null
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onStudioClick: (String) -> Unit,
    onNotificationClick: () -> Unit,
    onCategoryClick: (String) -> Unit,
    viewModel: HomeViewModel = viewModel()
) {
    val studios by viewModel.studios.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedFilter by viewModel.selectedFilter.collectAsState()
    val selectedCategory by viewModel.selectedCategory.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    val focusManager = LocalFocusManager.current

    Scaffold(
        containerColor = ThemeBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(ThemeBackground)
                .padding(padding),
            contentPadding = PaddingValues(top = 24.dp, bottom = 32.dp)
        ) {
            // Header Location, Switch Role & Notification
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f, fill = false)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .background(ThemePrimary, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(modifier = Modifier.size(6.dp).background(ThemeOnPrimary, CircleShape))
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                "CURRENT LOCATION",
                                color = ThemePrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                letterSpacing = 1.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Civil Lines, Prayagraj", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = ThemeOnBackground)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = ThemeOnSurfaceVariant, modifier = Modifier.size(16.dp))
                        }
                    }
                    
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .border(1.dp, ThemeOutline, CircleShape)
                                .background(ThemeSurfaceVariant, CircleShape)
                                .clip(CircleShape)
                                .clickable { onNotificationClick() },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = ThemeOnSurfaceVariant, modifier = Modifier.size(20.dp))
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset(x = (-2).dp, y = 2.dp)
                                    .size(9.dp)
                                    .background(NotificationBadge, CircleShape)
                                    .border(2.dp, ThemeBackground, CircleShape)
                            )
                        }
                    }
                }
            }
            
            // Hero Banner
            item { 
                com.example.ui.components.HeroBanner(onExploreClick = {
                    viewModel.setFilter(StudioFilter.TOP_RATED)
                }) 
            }
            item { Spacer(modifier = Modifier.height(20.dp)) }

            // Search Bar
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp)
                        .height(56.dp)
                        .background(ThemeSurfaceVariant, RoundedCornerShape(50))
                        .border(1.dp, ThemeOutline, RoundedCornerShape(50))
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
                            fontSize = 15.sp
                        ),
                        decorationBox = { innerTextField ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Search, contentDescription = "Search", tint = ThemeOnSurfaceVariant, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Box(modifier = Modifier.weight(1f)) {
                                    if (searchQuery.isEmpty()) {
                                        Text(
                                            "Search studios, area (Katra, Naini) or service...",
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

            // Quick Filter Chips (Top Rated, Verified, Near Me, Budget Friendly)
            item {
                LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp),
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        FilterPillChip(
                            label = "All Studios",
                            icon = null,
                            isSelected = selectedFilter == StudioFilter.ALL && selectedCategory == null,
                            onClick = {
                                viewModel.setFilter(StudioFilter.ALL)
                                if (selectedCategory != null) viewModel.toggleCategory(selectedCategory!!)
                            }
                        )
                    }
                    item {
                        FilterPillChip(
                            label = "Top Rated",
                            icon = Icons.Default.Star,
                            isSelected = selectedFilter == StudioFilter.TOP_RATED,
                            onClick = { viewModel.setFilter(StudioFilter.TOP_RATED) }
                        )
                    }
                    item {
                        FilterPillChip(
                            label = "Verified",
                            icon = Icons.Default.Verified,
                            isSelected = selectedFilter == StudioFilter.VERIFIED,
                            onClick = { viewModel.setFilter(StudioFilter.VERIFIED) }
                        )
                    }
                    item {
                        FilterPillChip(
                            label = "Near Me",
                            icon = Icons.Default.NearMe,
                            isSelected = selectedFilter == StudioFilter.NEAR_ME,
                            onClick = { viewModel.setFilter(StudioFilter.NEAR_ME) }
                        )
                    }
                    item {
                        FilterPillChip(
                            label = "Budget Friendly",
                            icon = null,
                            isSelected = selectedFilter == StudioFilter.BUDGET_FRIENDLY,
                            onClick = { viewModel.setFilter(StudioFilter.BUDGET_FRIENDLY) }
                        )
                    }
                }
            }
            
            // Service Category Grid Items
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 28.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    CategoryItem(
                        title = "Candid",
                        icon = Icons.Default.CameraAlt,
                        bg = CategoryCandidBg,
                        iconTint = CategoryCandidIcon,
                        isSelected = selectedCategory?.equals("Candid", ignoreCase = true) == true,
                        onClick = { viewModel.toggleCategory("Candid") }
                    )
                    CategoryItem(
                        title = "Cinematic",
                        icon = Icons.Default.Movie,
                        bg = CategoryCinematicBg,
                        iconTint = CategoryCinematicIcon,
                        isSelected = selectedCategory?.equals("Cinematic", ignoreCase = true) == true,
                        onClick = { viewModel.toggleCategory("Cinematic") }
                    )
                    CategoryItem(
                        title = "Drone",
                        icon = Icons.Default.Flight,
                        bg = CategoryDroneBg,
                        iconTint = CategoryDroneIcon,
                        isSelected = selectedCategory?.equals("Drone", ignoreCase = true) == true,
                        onClick = { viewModel.toggleCategory("Drone") }
                    )
                    CategoryItem(
                        title = "Traditional",
                        icon = Icons.Default.PhotoCamera,
                        bg = CategoryTraditionalBg,
                        iconTint = CategoryTraditionalIcon,
                        isSelected = selectedCategory?.equals("Traditional", ignoreCase = true) == true,
                        onClick = { viewModel.toggleCategory("Traditional") }
                    )
                }
            }
            
            // Studios Header
            item {
                val headerTitle = when {
                    searchQuery.isNotBlank() -> "Search Results for \"$searchQuery\""
                    selectedCategory != null -> "$selectedCategory Photography Studios"
                    selectedFilter == StudioFilter.TOP_RATED -> "Top Rated Studios in Prayagraj"
                    selectedFilter == StudioFilter.VERIFIED -> "Verified Studios in Prayagraj"
                    selectedFilter == StudioFilter.NEAR_ME -> "Studios Nearest to You"
                    selectedFilter == StudioFilter.BUDGET_FRIENDLY -> "Budget Friendly Studios"
                    else -> "Top Studios in Prayagraj"
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = headerTitle,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnBackground,
                            letterSpacing = (-0.5).sp
                        )
                        if (studios.isNotEmpty()) {
                            Text(
                                text = "${studios.size} studios available",
                                fontSize = 12.sp,
                                color = ThemeOnSurfaceVariant
                            )
                        }
                    }
                    if (searchQuery.isNotEmpty() || selectedFilter != StudioFilter.ALL || selectedCategory != null) {
                        Text(
                            text = "Reset",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemePrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.clearAllFilters() }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    } else {
                        Text(
                            text = "View All",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemePrimary,
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .clickable { viewModel.setFilter(StudioFilter.ALL) }
                                .padding(horizontal = 6.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            // Studio Cards
            if (studios.isEmpty()) {
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp, horizontal = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "No Results",
                            modifier = Modifier.size(48.dp),
                            tint = ThemeOnSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No studios found matching your search.",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = ThemeOnSurface,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Try a different keyword or category.",
                            fontSize = 14.sp,
                            color = ThemeOnSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = { viewModel.clearAllFilters() },
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("Clear Filters", color = ThemeOnPrimary, fontWeight = FontWeight.Bold)
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
fun FilterPillChip(
    label: String,
    icon: ImageVector?,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val containerColor = if (isSelected) ThemePrimary else ThemeSurfaceVariant
    val contentColor = if (isSelected) ThemeOnPrimary else ThemeOnSurface
    val borderColor = if (isSelected) ThemePrimary else ThemeOutline

    Row(
        modifier = Modifier
            .border(1.dp, borderColor, RoundedCornerShape(50))
            .background(containerColor, RoundedCornerShape(50))
            .clip(RoundedCornerShape(50))
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = label,
            fontSize = 13.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = contentColor
        )
    }
}

@Composable
fun CategoryItem(
    title: String,
    icon: ImageVector,
    bg: Color,
    iconTint: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(bg, RoundedCornerShape(16.dp))
                .border(
                    width = if (isSelected) 2.dp else 0.dp,
                    color = if (isSelected) ThemePrimary else Color.Transparent,
                    shape = RoundedCornerShape(16.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = title, tint = iconTint, modifier = Modifier.size(32.dp))
        }
        Text(
            text = title.uppercase(),
            fontSize = 11.sp,
            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Bold,
            color = if (isSelected) ThemePrimary else ThemeOnBackground,
            letterSpacing = (-0.5).sp
        )
    }
}

