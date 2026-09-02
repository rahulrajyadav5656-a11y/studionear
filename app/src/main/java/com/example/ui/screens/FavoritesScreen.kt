package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.FavoritesManager
import com.example.data.models.Studio
import com.example.data.repositories.StudioRepository
import com.example.di.ServiceLocator
import com.example.ui.components.StudioCard
import com.example.ui.theme.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class FavoritesViewModel(
    private val studioRepository: StudioRepository = ServiceLocator.studioRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val favoriteStudios: StateFlow<List<Studio>> = combine(
        FavoritesManager.favoriteStudioIds,
        studioRepository.getStudios(),
        _searchQuery
    ) { favIds, allStudios, query ->
        val matched = allStudios.filter { favIds.contains(it.id) }
        if (query.isBlank()) {
            matched
        } else {
            val q = query.trim().lowercase()
            matched.filter { studio ->
                studio.name.lowercase().contains(q) ||
                studio.city.lowercase().contains(q) ||
                studio.area.lowercase().contains(q) ||
                studio.services.any { it.lowercase().contains(q) }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalFavoritesCount: StateFlow<Int> = FavoritesManager.favoriteStudioIds
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun removeFavorite(studioId: String) {
        viewModelScope.launch {
            FavoritesManager.toggleFavorite(studioId)
        }
    }

    fun restoreFavorite(studioId: String) {
        viewModelScope.launch {
            FavoritesManager.toggleFavorite(studioId)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(
    onStudioClick: (String) -> Unit,
    onSearchClick: () -> Unit = {},
    viewModel: FavoritesViewModel = viewModel()
) {
    val studios by viewModel.favoriteStudios.collectAsState()
    val totalCount by viewModel.totalFavoritesCount.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "My Bookmarks",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = ThemeOnBackground
                        )
                        if (totalCount > 0) {
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = ThemePrimaryContainer,
                                modifier = Modifier.padding(top = 2.dp)
                            ) {
                                Text(
                                    text = "$totalCount",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = ThemePrimary,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = ThemeBackground)
            )
        },
        containerColor = ThemeBackground
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            if (totalCount > 0) {
                // Search bar inside Favorites
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 6.dp)
                        .background(ThemeSurface, RoundedCornerShape(12.dp))
                        .border(1.dp, ThemeOutline, RoundedCornerShape(12.dp))
                        .padding(horizontal = 12.dp, vertical = 10.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search Bookmarks",
                            tint = ThemeOnSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { viewModel.setSearchQuery(it) },
                            singleLine = true,
                            textStyle = TextStyle(
                                color = ThemeOnSurface,
                                fontSize = 14.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("favorites_search_input"),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Filter your saved studios...",
                                        color = ThemeOnSurfaceVariant,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { viewModel.setSearchQuery("") },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear search",
                                    tint = ThemeOnSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (totalCount == 0) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .background(ThemePrimaryContainer.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = ThemePrimary
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        Text(
                            text = "No Bookmarked Studios",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnBackground
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "Save your favorite photography studios to quickly compare packages, check availability, and book your dates.",
                            fontSize = 14.sp,
                            color = ThemeOnSurfaceVariant,
                            lineHeight = 20.sp,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 16.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(28.dp))
                        
                        Button(
                            onClick = onSearchClick,
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = ThemePrimary),
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .height(48.dp)
                                .testTag("explore_studios_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Explore Studios",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }
                }
            } else if (studios.isEmpty()) {
                // No search matches
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = "No matches found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ThemeOnBackground
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try searching with a different keyword.",
                            fontSize = 14.sp,
                            color = ThemeOnSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .testTag("favorites_list"),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(studios, key = { it.id }) { studio ->
                        StudioCard(
                            studio = studio,
                            onClick = { onStudioClick(studio.id) },
                            onFavoriteToggle = { isFav ->
                                if (!isFav) {
                                    scope.launch {
                                        val result = snackbarHostState.showSnackbar(
                                            message = "${studio.name} removed from bookmarks",
                                            actionLabel = "Undo",
                                            duration = SnackbarDuration.Short
                                        )
                                        if (result == SnackbarResult.ActionPerformed) {
                                            viewModel.restoreFavorite(studio.id)
                                        }
                                    }
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}
