package com.example.ui.screens

import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.repository.SearchResultItem
import com.example.data.repository.SearchResultType
import com.example.ui.components.SSKNTopBar
import com.example.ui.theme.*

@Composable
fun SearchScreen(
    query: String,
    results: List<SearchResultItem>,
    onQueryChange: (String) -> Unit,
    onClearQuery: () -> Unit,
    onOpenResult: (chapterId: String, pageNumber: Int) -> Unit,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf<SearchResultType?>(null) }

    val filteredResults = remember(results, selectedFilter) {
        if (selectedFilter == null) results else results.filter { it.type == selectedFilter }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppBackground)
            .testTag("search_screen")
    ) {
        if (onBackClick != null) {
            SSKNTopBar(
                title = "Search NCERT Biology",
                onBackClick = onBackClick
            )
        }

        // Search Input Field
        Surface(
            color = SurfaceCard,
            tonalElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    placeholder = { Text("Search NCERT lines, terms, PYQs...", fontSize = 14.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = PrimaryBlue
                        )
                    },
                    trailingIcon = {
                        if (query.isNotEmpty()) {
                            IconButton(onClick = onClearQuery) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Clear",
                                    tint = SecondaryText
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryBlue,
                        unfocusedBorderColor = BorderSubtle,
                        focusedContainerColor = VeryLightBlue.copy(alpha = 0.3f),
                        unfocusedContainerColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("search_input_field")
                )

                // Filter Category Chips
                Spacer(modifier = Modifier.height(10.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    item {
                        FilterChip(
                            selected = selectedFilter == null,
                            onClick = { selectedFilter = null },
                            label = { Text("All (${results.size})", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchResultType.NCERT_PAGE,
                            onClick = { selectedFilter = SearchResultType.NCERT_PAGE },
                            label = { Text("Pages", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchResultType.CONCEPT,
                            onClick = { selectedFilter = SearchResultType.CONCEPT },
                            label = { Text("High-Yield Points", fontSize = 12.sp) }
                        )
                    }
                    item {
                        FilterChip(
                            selected = selectedFilter == SearchResultType.PYQ,
                            onClick = { selectedFilter = SearchResultType.PYQ },
                            label = { Text("PYQs", fontSize = 12.sp) }
                        )
                    }
                }
            }
        }

        // Search Results List
        if (query.isBlank()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = BorderSubtle,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Instant NCERT Search",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryText
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Try searching for 'Virchow', 'Endomembrane', 'Metaphase', 'Chargaff', or 'Snapdragon'",
                        fontSize = 12.5.sp,
                        color = SecondaryText,
                        textAlign = TextAlign.Center,
                        lineHeight = 17.sp
                    )
                }
            }
        } else if (filteredResults.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No NCERT matches found for \"$query\".\nTry searching for broader biological terms or chapter names.",
                    fontSize = 13.sp,
                    color = SecondaryText,
                    textAlign = TextAlign.Center,
                    lineHeight = 18.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 80.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(filteredResults) { item ->
                    SearchResultCard(
                        item = item,
                        onClick = { onOpenResult(item.chapterId, item.targetPage) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchResultCard(
    item: SearchResultItem,
    onClick: () -> Unit
) {
    val (typeTag, tagColor) = when (item.type) {
        SearchResultType.NCERT_PAGE -> "NCERT PAGE" to PrimaryBlue
        SearchResultType.CONCEPT -> "HIGH YIELD" to WarningOrange
        SearchResultType.PYQ -> "VERIFIED PYQ" to SuccessGreen
        SearchResultType.MCQ -> "PRACTICE MCQ" to AIPurple
    }

    Surface(
        color = SurfaceCard,
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, BorderSubtle),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("search_result_${item.id}")
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(tagColor.copy(alpha = 0.12f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = typeTag,
                        fontSize = 9.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = tagColor
                    )
                }
                Text(
                    text = "Page ${item.targetPage}",
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.Bold,
                    color = PrimaryBlue
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = item.title,
                fontSize = 14.5.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryText
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = item.snippet,
                fontSize = 12.5.sp,
                color = SecondaryText,
                lineHeight = 17.sp
            )
        }
    }
}
