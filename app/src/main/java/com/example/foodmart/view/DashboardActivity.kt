package com.example.foodmart.view

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import com.example.foodmart.model.ProductModel
import com.example.foodmart.repository.ProductRepositoryImpl
import com.example.foodmart.viewmodel.ProductViewModel

class DashboardActivity : ComponentActivity() {

    private lateinit var productViewModel: ProductViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize ViewModel with Repository
        val repository = ProductRepositoryImpl()
        productViewModel = ViewModelProvider(
            this,
            ProductViewModelFactory(repository)
        )[ProductViewModel::class.java]

        enableEdgeToEdge()
        setContent {
            DashboardScreen(productViewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        // Refresh products when returning to dashboard
        productViewModel.getAllProduct()
    }
}

// ViewModelFactory for ProductViewModel
class ProductViewModelFactory(private val repository: ProductRepositoryImpl) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(productViewModel: ProductViewModel) {
    val context = LocalContext.current
    val activity = context as Activity

    // FoodMart theme colors
    val primaryColor = Color(0xFFE91E63) // Food Pink
    val secondaryColor = Color(0xFFFF9800) // Orange
    val backgroundColor = Color(0xFFFFF3E0) // Light cream
    val cardColor = Color.White
    val textColor = Color(0xFF37474F) // Dark gray

    // Navigation state
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Products", "Search", "Profile")
    val tabIcons = listOf(Icons.Default.ShoppingCart, Icons.Default.Search, Icons.Default.Person)

    // Observe products from ViewModel
    val allProducts by productViewModel.allProducts.observeAsState(emptyList())
    val isLoading by productViewModel.loading.observeAsState(false)

    // LOCAL ONLY: Favorite products state management (UI only)
    var favoriteProducts by remember { mutableStateOf(setOf<String>()) }

    // Load products on first composition
    LaunchedEffect(Unit) {
        productViewModel.getAllProduct()
    }

    // Simple function to toggle favorite status (UI state only)
    fun toggleFavorite(productId: String) {
        favoriteProducts = if (favoriteProducts.contains(productId)) {
            favoriteProducts - productId
        } else {
            favoriteProducts + productId
        }
    }

    // User info from SharedPreferences
    val sharedPreferences = context.getSharedPreferences("FoodMartUser", Context.MODE_PRIVATE)
    val userEmail = sharedPreferences.getString("email", "user@foodmart.com") ?: "user@foodmart.com"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "FoodMart Dashboard",
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(
                        onClick = {
                            // Clear saved credentials and logout
                            val editor = sharedPreferences.edit()
                            editor.clear()
                            editor.apply()

                            val intent = Intent(context, LoginActivity::class.java)
                            context.startActivity(intent)
                            activity.finish()
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.ExitToApp,
                            contentDescription = "Logout",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor
                )
            )
        },
        bottomBar = {
            BottomAppBar(
                containerColor = Color.White,
                contentColor = primaryColor
            ) {
                tabs.forEachIndexed { index, tab ->
                    NavigationBarItem(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        icon = {
                            Icon(
                                imageVector = tabIcons[index],
                                contentDescription = tab
                            )
                        },
                        label = { Text(tab) }
                    )
                }
            }
        },
        floatingActionButton = {
            if (selectedTab == 0) {
                FloatingActionButton(
                    onClick = {
                        val intent = Intent(context, AddProductActivity::class.java)
                        context.startActivity(intent)
                    },
                    containerColor = primaryColor
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Product",
                        tint = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            backgroundColor,
                            Color(0xFFFFF8F0),
                            backgroundColor
                        )
                    )
                )
        ) {
            when (selectedTab) {
                0 -> ProductsScreen(
                    products = allProducts,
                    isLoading = isLoading,
                    favoriteProducts = favoriteProducts,
                    onToggleFavorite = ::toggleFavorite,
                    onViewProduct = { product ->
                        val intent = Intent(context, ViewProductActivity::class.java)
                        intent.putExtra("product_id", product.productID)
                        context.startActivity(intent)
                    },
                    onEditProduct = { product ->
                        val intent = Intent(context, EditProductActivity::class.java)
                        intent.putExtra("product_id", product.productID)
                        context.startActivity(intent)
                    },
                    innerPadding = innerPadding,
                    primaryColor = primaryColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )
                1 -> SearchScreen(
                    products = allProducts,
                    isLoading = isLoading,
                    favoriteProducts = favoriteProducts,
                    onToggleFavorite = ::toggleFavorite,
                    onViewProduct = { product ->
                        val intent = Intent(context, ViewProductActivity::class.java)
                        intent.putExtra("product_id", product.productID)
                        context.startActivity(intent)
                    },
                    onEditProduct = { product ->
                        val intent = Intent(context, EditProductActivity::class.java)
                        intent.putExtra("product_id", product.productID)
                        context.startActivity(intent)
                    },
                    innerPadding = innerPadding,
                    primaryColor = primaryColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryColor = secondaryColor
                )
                2 -> ProfileScreen(
                    userEmail = userEmail,
                    innerPadding = innerPadding,
                    primaryColor = primaryColor,
                    cardColor = cardColor,
                    textColor = textColor,
                    secondaryColor = secondaryColor,
                    context = context
                )
            }
        }
    }
}

// SearchScreen Composable
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    products: List<ProductModel>,
    isLoading: Boolean,
    favoriteProducts: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onViewProduct: (ProductModel) -> Unit,
    onEditProduct: (ProductModel) -> Unit,
    innerPadding: PaddingValues,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCategoryFilter by remember { mutableStateOf(false) }

    val keyboardController = LocalSoftwareKeyboardController.current

    val categories = remember(products) {
        listOf("All") + products.map { it.category }.distinct().sorted()
    }

    val filteredProducts = remember(products, searchQuery, selectedCategory) {
        products.filter { product ->
            val matchesSearch = if (searchQuery.isBlank()) {
                true
            } else {
                product.productName.contains(searchQuery, ignoreCase = true) ||
                        product.description.contains(searchQuery, ignoreCase = true) ||
                        product.category.contains(searchQuery, ignoreCase = true)
            }

            val matchesCategory = selectedCategory == "All" || product.category == selectedCategory

            matchesSearch && matchesCategory
        }
    }

    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "🔍 Search Products",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(12.dp)),
            colors = CardDefaults.cardColors(containerColor = cardColor),
            shape = RoundedCornerShape(12.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search by name, category, or description...") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = primaryColor
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(
                            onClick = {
                                searchQuery = ""
                                keyboardController?.hide()
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Clear",
                                tint = Color.Gray
                            )
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primaryColor,
                    focusedLabelColor = primaryColor,
                    unfocusedBorderColor = Color.Transparent
                ),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(
                    onSearch = { keyboardController?.hide() }
                )
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Filter by Category:",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = textColor
            )

            Card(
                modifier = Modifier.clickable { showCategoryFilter = true },
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedCategory == "All") cardColor else primaryColor.copy(alpha = 0.1f)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = selectedCategory,
                        fontSize = 12.sp,
                        color = if (selectedCategory == "All") textColor else primaryColor,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Icon(
                        imageVector = Icons.Default.ArrowDropDown,
                        contentDescription = "Dropdown",
                        tint = if (selectedCategory == "All") textColor else primaryColor,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        DropdownMenu(
            expanded = showCategoryFilter,
            onDismissRequest = { showCategoryFilter = false }
        ) {
            categories.forEach { category ->
                DropdownMenuItem(
                    text = { Text(category) },
                    onClick = {
                        selectedCategory = category
                        showCategoryFilter = false
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (searchQuery.isNotEmpty() || selectedCategory != "All") {
            Text(
                text = "Found ${filteredProducts.size} product${if (filteredProducts.size != 1) "s" else ""}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            products.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🛍️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No products to search!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "Add some products first to search through them",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            filteredProducts.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🔍", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No results found",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "Try different keywords or check your filters",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )

                        if (searchQuery.isNotEmpty() || selectedCategory != "All") {
                            Spacer(modifier = Modifier.height(16.dp))
                            TextButton(
                                onClick = {
                                    searchQuery = ""
                                    selectedCategory = "All"
                                }
                            ) {
                                Text("Clear Filters", color = primaryColor)
                            }
                        }
                    }
                }
            }
            else -> {
                LazyColumn {
                    items(filteredProducts) { product ->
                        SearchProductCard(
                            product = product,
                            searchQuery = searchQuery,
                            isFavorite = favoriteProducts.contains(product.productID),
                            onToggleFavorite = { onToggleFavorite(product.productID) },
                            onView = { onViewProduct(product) },
                            onEdit = { onEditProduct(product) },
                            primaryColor = primaryColor,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// SearchProductCard with heart icon
@Composable
fun SearchProductCard(
    product: ProductModel,
    searchQuery: String,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onView() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )

                    Row(
                        modifier = Modifier.padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = primaryColor.copy(alpha = 0.1f)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = product.category,
                                fontSize = 10.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }

                        if (searchQuery.isNotEmpty()) {
                            Spacer(modifier = Modifier.width(8.dp))
                            val matchFound = when {
                                product.productName.contains(searchQuery, ignoreCase = true) -> "📝 Name"
                                product.category.contains(searchQuery, ignoreCase = true) -> "🏷️ Category"
                                product.description.contains(searchQuery, ignoreCase = true) -> "📄 Description"
                                else -> ""
                            }
                            if (matchFound.isNotEmpty()) {
                                Text(
                                    text = matchFound,
                                    fontSize = 10.sp,
                                    color = secondaryColor,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    if (product.description.isNotEmpty()) {
                        Text(
                            text = product.description,
                            fontSize = 14.sp,
                            color = Color.Gray,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }

                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row {
                    // Heart/Favorite Icon
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                    IconButton(onClick = onView) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = secondaryColor
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = primaryColor
                        )
                    }
                }
            }
        }
    }
}

// ProductsScreen with favorite functionality
@Composable
fun ProductsScreen(
    products: List<ProductModel>,
    isLoading: Boolean,
    favoriteProducts: Set<String>,
    onToggleFavorite: (String) -> Unit,
    onViewProduct: (ProductModel) -> Unit,
    onEditProduct: (ProductModel) -> Unit,
    innerPadding: PaddingValues,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
    ) {
        Text(
            text = "🍕 Your Products",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        when {
            isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryColor)
                }
            }
            products.isEmpty() -> {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    colors = CardDefaults.cardColors(containerColor = cardColor),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("🍽️", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            "No products yet!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = textColor
                        )
                        Text(
                            "Tap the + button to add your first product",
                            fontSize = 14.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
            else -> {
                LazyColumn {
                    items(products) { product ->
                        ProductCard(
                            product = product,
                            isFavorite = favoriteProducts.contains(product.productID),
                            onToggleFavorite = { onToggleFavorite(product.productID) },
                            onView = { onViewProduct(product) },
                            onEdit = { onEditProduct(product) },
                            primaryColor = primaryColor,
                            cardColor = cardColor,
                            textColor = textColor,
                            secondaryColor = secondaryColor
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

// ProductCard with heart icon
@Composable
fun ProductCard(
    product: ProductModel,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onView: () -> Unit,
    onEdit: () -> Unit,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(12.dp))
            .clickable { onView() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = product.productName,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = product.category,
                        fontSize = 12.sp,
                        color = primaryColor,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        text = product.description,
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                    Text(
                        text = "$${String.format("%.2f", product.price)}",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = secondaryColor,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row {
                    // Heart/Favorite Icon
                    IconButton(onClick = onToggleFavorite) {
                        Icon(
                            imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (isFavorite) Color.Red else Color.Gray
                        )
                    }
                    IconButton(onClick = onView) {
                        Icon(
                            imageVector = Icons.Default.Visibility,
                            contentDescription = "View",
                            tint = secondaryColor
                        )
                    }
                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit",
                            tint = primaryColor
                        )
                    }
                }
            }
        }
    }
}

// ProfileScreen
@Composable
fun ProfileScreen(
    userEmail: String,
    innerPadding: PaddingValues,
    primaryColor: Color,
    cardColor: Color,
    textColor: Color,
    secondaryColor: Color,
    context: Context
) {
    Column(
        modifier = Modifier
            .padding(innerPadding)
            .padding(16.dp)
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(20.dp))

        // Profile Picture
        Card(
            modifier = Modifier
                .size(120.dp)
                .shadow(8.dp, RoundedCornerShape(60.dp)),
            shape = RoundedCornerShape(60.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "👤",
                    fontSize = 48.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Welcome back! 👋",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Profile Information Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Profile Information",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                ProfileInfoRow("📧 Email", userEmail)
                ProfileInfoRow("🏪 Role", "Store Owner")
                ProfileInfoRow("📅 Member Since", "August 2024")
                ProfileInfoRow("🛍️ Store", "FoodMart")
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Settings Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(8.dp, RoundedCornerShape(16.dp)),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = cardColor)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Quick Actions",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = textColor,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                SettingsItem("🔧 Settings", "App preferences") {}
                SettingsItem("📊 Analytics", "View your stats") {}
                SettingsItem("💬 Support", "Get help") {}
                SettingsItem("ℹ️ About", "App information") {}
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun ProfileInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.Gray,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = Color(0xFF37474F),
            fontWeight = FontWeight.SemiBold
        )
    }
}

@Composable
fun SettingsItem(title: String, subtitle: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF37474F)
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = Color.Gray
            )
        }
        Text("→", fontSize = 18.sp, color = Color(0xFFE91E63))
    }
}
