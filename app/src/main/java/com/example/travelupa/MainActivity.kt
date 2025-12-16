package com.example.travelupa

// Import yang diperlukan (Bab 1 - Bab 9)
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.room.Room
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import com.example.travelupa.ui.theme.TravelupaTheme // Asumsi Theme Anda
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

// Anda perlu membuat file R.drawable.travelupa
// Note: Kode ini mengasumsikan Anda memiliki drawable dengan nama R.drawable.travelupa

// ----------------------------------------------------
// MODEL DATA (Bab 4, Bab 5, Bab 8)
// ----------------------------------------------------
data class TempatWisata(
    val id: String = "", // Firestore Document ID
    val nama: String = "",
    val deskripsi: String = "",
    val gambarUriString: String? = null, // URL gambar Firebase Storage
    val gambarResId: Int? = null,
)

// Sealed Class untuk Navigasi (Bab 7)
sealed class Screen(val route: String) {
    object Greeting : Screen("greeting")
    object Login : Screen("login")
    object RekomendasiTempat : Screen("rekomendasi_tempat")
    object Gallery : Screen("gallery")
}


/**
 * Activity Utama (Bab 1, Bab 6, Bab 7, Bab 8)
 */
class MainActivity : ComponentActivity() {
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var imageDao: ImageDao
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        // Inisialisasi Room Database (Bab 8)
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "travelupa-database"
        ).build()
        imageDao = db.imageDao()

        val currentUser: FirebaseUser? = auth.currentUser

        setContent {
            TravelupaTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White
                ) {
                    AppNavigation(currentUser, firestore, storage, imageDao, auth)
                }
            }
        }
    }
}


/**
 * Fungsi Navigasi Utama (Bab 7)
 */
@Composable
fun AppNavigation(
    currentUser: FirebaseUser?,
    firestore: FirebaseFirestore,
    storage: FirebaseStorage,
    imageDao: ImageDao,
    auth: FirebaseAuth
) {
    val navController = rememberNavController()
    val startDestination = if (currentUser != null) {
        Screen.RekomendasiTempat.route
    } else {
        Screen.Greeting.route
    }

    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Greeting.route) {
            GreetingScreen(
                onStart = {
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.Greeting.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Login.route) {
            LoginScreen(
                auth = auth,
                onLoginSuccess = {
                    navController.navigate(Screen.RekomendasiTempat.route) {
                        popUpTo(Screen.Login.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.RekomendasiTempat.route) {
            RekomendasiTempatScreen(
                firestore = firestore,
                onBackToLogin = {
                    auth.signOut()
                    navController.navigate(Screen.Login.route) {
                        popUpTo(Screen.RekomendasiTempat.route) { inclusive = true }
                    }
                },
                onGallerySelected = {
                    navController.navigate(Screen.Gallery.route)
                }
            )
        }

        composable(Screen.Gallery.route) {
            GalleryScreen(
                imageDao = imageDao,
                onImageSelected = { uri ->
                    Log.d("Gallery", "Image selected: $uri")
                    navController.popBackStack()
                },
                onBack = {
                    navController.popBackStack()
                }
            )
        }
    }
}


// ----------------------------------------------------
// UI SCREENS DENGAN PENINGKATAN VISUAL
// ----------------------------------------------------

/**
 * Halaman Greeting (Bab 2, Bab 7)
 */
@Composable
fun GreetingScreen(
    onStart: () -> Unit // Navigasi ke halaman selanjutnya
) {
    // Tentukan warna tema untuk tampilan ini
    // Menggunakan warna background muda (mirip Light background)
    val purpleDark = Color(0xFF4A148C) // Warna ungu tua (mirip warna logo di gambar)
    val lightBackground = Color(0xFFF3E5F5) // Background sangat muda/putih

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(lightBackground),
        contentAlignment = Alignment.Center,
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 1. LOGO/BRANDING (Di paling atas, besar)
            Image(
                painter = painterResource(id = R.drawable.travelupa),
                contentDescription = "Travelupa Logo",
                modifier = Modifier
                    .size(700.dp)
                    .padding(horizontal = 16.dp),
                contentScale = ContentScale.Fit
            )

            // 2. KALIMAT SAMBUTAN (Di bawah branding)
            Text(
                text = "Selamat Datang di Travelupa!",
                style = MaterialTheme.typography.h6,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.SemiBold,
                color = purpleDark // Gunakan warna yang serasi dengan branding
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // 3. TOMBOL MULAI (Di bagian bawah, lebar penuh)
        Button(
            onClick = onStart,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 8.dp),
            // Mengubah warna tombol agar serasi dengan tampilan di gambar
            colors = ButtonDefaults.buttonColors(backgroundColor = Color(0xFF673AB7)),
            elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
        ) {
            Text(
                text = "Mulai Jelajahi",
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

/**
 * Halaman Login (Bab 5, Bab 6) - MODIFIKASI UI
 */
@Composable
fun LoginScreen(
    auth: FirebaseAuth,
    onLoginSuccess: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isPasswordVisible by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Surface(
        color = MaterialTheme.colors.primary,
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                "Travelupa Admin",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                "Akses ke Konten Wisata",
                fontSize = 16.sp,
                color = Color.White.copy(alpha = 0.8f),
                modifier = Modifier.padding(bottom = 40.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = 8.dp,
                shape = MaterialTheme.shapes.medium
            ) {
                Column(modifier = Modifier.padding(24.dp)) {
                    // INPUT EMAIL
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it; errorMessage = null },
                        label = { Text("Email Admin") },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email") },
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
                    )
                    // INPUT PASSWORD
                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it; errorMessage = null },
                        label = { Text("Password") },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = "Password") },
                        visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                        trailingIcon = {
                            IconButton(onClick = { isPasswordVisible = !isPasswordVisible }) {
                                Icon(
                                    imageVector = if (isPasswordVisible) Icons.Outlined.VisibilityOff else Icons.Outlined.Visibility,
                                    contentDescription = if (isPasswordVisible) "Hide password" else "Show password"
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                    )

                    // TOMBOL LOGIN
                    Button(
                        onClick = {
                            if (email.isBlank() || password.isBlank()) {
                                errorMessage = "Email dan password tidak boleh kosong."
                                return@Button
                            }
                            errorMessage = null
                            isLoading = true
                            coroutineScope.launch {
                                try {
                                    withContext(Dispatchers.IO) {
                                        auth.signInWithEmailAndPassword(email, password).await()
                                    }
                                    isLoading = false
                                    onLoginSuccess()
                                } catch (e: Exception) {
                                    isLoading = false
                                    errorMessage = "Login gagal: ${e.localizedMessage}"
                                    Log.e("FIREBASE_AUTH", "Login failed", e)
                                }
                            }
                        },
                        enabled = !isLoading,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        elevation = ButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                        } else {
                            Text("MASUK", fontWeight = FontWeight.Bold)
                        }
                    }

                    // PESAN ERROR
                    errorMessage?.let {
                        Text(it, color = Color.Red, modifier = Modifier.padding(top = 16.dp), style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}


/**
 * Halaman Rekomendasi Tempat (Bab 3, Bab 4, Bab 5) - MODIFIKASI UI
 */
@Composable
fun RekomendasiTempatScreen(
    firestore: FirebaseFirestore,
    onBackToLogin: (() -> Unit)? = null,
    onGallerySelected: () -> Unit
) {
    var daftarTempatWisata by remember { mutableStateOf(listOf<TempatWisata>()) }
    var showTambahDialog by remember { mutableStateOf(false) }
    val userEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Admin"
    var drawerState = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    var listenerRegistration: ListenerRegistration? by remember { mutableStateOf(null) }

    // Mengambil data dari Firestore (Bab 5)
    DisposableEffect(firestore) {
        listenerRegistration = firestore.collection("tempat_wisata")
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    Log.w("Firestore", "Listen failed.", e)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val tempatWisataList = mutableListOf<TempatWisata>()
                    for (document in snapshot.documents) {
                        val tempatWisata = document.toObject(TempatWisata::class.java)?.copy(id = document.id)
                        tempatWisata?.let { tempatWisataList.add(it) }
                    }
                    daftarTempatWisata = tempatWisataList
                }
            }
        onDispose {
            listenerRegistration?.remove()
        }
    }

    ModalDrawer(
        drawerState = drawerState,
        drawerContent = {
            Column {
                // Header Drawer
                Box(modifier = Modifier.fillMaxWidth().height(150.dp).background(MaterialTheme.colors.primary).padding(16.dp), contentAlignment = Alignment.BottomStart) {
                    Text("Halo, ${userEmail.substringBefore('@')}!", style = MaterialTheme.typography.h6, color = Color.White)
                }
                Divider()
                // Menu Gallery
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch { drawerState.close() }
                            onGallerySelected()
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.PhotoLibrary, contentDescription = "Gallery", modifier = Modifier.padding(end = 8.dp))
                    Text(text = "Gallery", style = MaterialTheme.typography.subtitle1)
                }
                // Menu Logout
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                drawerState.close()
                                onBackToLogin?.invoke()
                            }
                        }
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Logout", modifier = Modifier.padding(end = 8.dp))
                    Text(text = "Logout", style = MaterialTheme.typography.subtitle1)
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text("Rekomendasi Tempat Wisata", fontSize = 16.sp)
                            Text("Kelola Data", fontSize = 12.sp, color = Color.White.copy(alpha = 0.7f))
                        }
                    },
                    backgroundColor = MaterialTheme.colors.primary,
                    contentColor = Color.White,
                    elevation = 4.dp,
                    navigationIcon = {
                        IconButton(onClick = {
                            coroutineScope.launch { drawerState.open() }
                        }) {
                            Icon(Icons.Filled.Menu, contentDescription = "Menu")
                        }
                    }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { showTambahDialog = true },
                    backgroundColor = MaterialTheme.colors.secondary
                ) {
                    Icon(Icons.Filled.Add, contentDescription = "Tambah Tempat Wisata")
                }
            }
        ) { paddingValues ->
            LazyColumn(contentPadding = paddingValues) {
                if (daftarTempatWisata.isEmpty()) {
                    item {
                        Text(
                            "Belum ada data tempat wisata. Tambahkan yang baru!",
                            modifier = Modifier.fillMaxWidth().padding(32.dp),
                            textAlign = TextAlign.Center,
                            color = Color.Gray
                        )
                    }
                }
                items(daftarTempatWisata, key = { it.id }) { tempat ->
                    TempatItemEditable(
                        tempat = tempat,
                        onDelete = { /* Logic di dalam item */ }
                    )
                }
            }
        }

        if (showTambahDialog) {
            TambahTempatWisataDialog(
                firestore = firestore,
                context = context,
                onDismiss = { showTambahDialog = false },
                onTambah = { _, _, _ ->
                    showTambahDialog = false
                }
            )
        }
    }
}


/**
 * Item Tempat Wisata dengan Opsi Delete (Bab 3, Bab 4, Bab 5) - MODIFIKASI UI
 */
@Composable
fun TempatItemEditable(
    tempat: TempatWisata,
    onDelete: () -> Unit
) {
    val firestore = FirebaseFirestore.getInstance()
    var expanded by remember { mutableStateOf(false) }
    val context = LocalContext.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = 6.dp,
        shape = MaterialTheme.shapes.medium
    ) {
        Column {
            // GAMBAR UTAMA
            val imagePainter = tempat.gambarUriString?.let { uriString ->
                rememberAsyncImagePainter(
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(uriString)
                        .crossfade(true)
                        .error(R.drawable.travelupa) // Fix Coil Syntax
                        .build()
                )
            } ?: tempat.gambarResId?.let {
                painterResource(id = it)
            } ?: painterResource(id = R.drawable.travelupa)

            Image(
                painter = imagePainter,
                contentDescription = tempat.nama,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentScale = ContentScale.Crop
            )

            // KONTEN TEKS DAN TOMBOL HAPUS
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        tempat.nama,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 20.sp,
                        color = MaterialTheme.colors.primary
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        tempat.deskripsi,
                        maxLines = 3,
                        style = MaterialTheme.typography.body2,
                        color = Color.Gray
                    )
                }

                // Dropdown Menu untuk Opsi Delete
                Box {
                    IconButton(
                        onClick = { expanded = true },
                        modifier = Modifier.size(40.dp)
                    ) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More options")
                    }
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        offset = DpOffset(0.dp, 0.dp),
                    ) {
                        DropdownMenuItem(onClick = {
                            expanded = false
                            if (tempat.id.isNotBlank()) {
                                firestore.collection("tempat_wisata").document(tempat.id)
                                    .delete()
                                    .addOnSuccessListener {
                                        Toast.makeText(context, "Dokumen dihapus", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Log.w("TempatItemEditable", "Error deleting document", e)
                                        Toast.makeText(context, "Gagal menghapus", Toast.LENGTH_SHORT).show()
                                    }
                            }
                        }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete", tint = Color.Red.copy(alpha = 0.8f))
                                Spacer(Modifier.width(8.dp))
                                Text("Delete", color = Color.Red.copy(alpha = 0.8f))
                            }
                        }
                    }
                }
            }
        }
    }
}


/**
 * Dialog Tambah Tempat Wisata (Bab 4, Bab 5, Bab 8) - MODIFIKASI UI
 */
@Composable
fun TambahTempatWisataDialog(
    firestore: FirebaseFirestore,
    context: Context,
    onDismiss: () -> Unit,
    onTambah: (String, String, String?) -> Unit
) {
    var nama by remember { mutableStateOf("") }
    var deskripsi by remember { mutableStateOf("") }
    var gambarUri by remember { mutableStateOf<Uri?>(null) }
    var isUploading by remember { mutableStateOf(false) }

    val gambarLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        gambarUri = uri
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Tempat Wisata Baru") },
        text = {
            Column {
                OutlinedTextField(
                    value = nama,
                    onValueChange = { nama = it },
                    label = { Text("Nama Tempat") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = deskripsi,
                    onValueChange = { deskripsi = it },
                    label = { Text("Deskripsi") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                )
                Spacer(modifier = Modifier.height(16.dp))

                gambarUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Gambar yang dipilih",
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }

                Button(
                    onClick = { gambarLauncher.launch("image/*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isUploading
                ) {
                    Text(if (gambarUri == null) "Pilih Gambar" else "Ganti Gambar")
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nama.isNotBlank() && deskripsi.isNotBlank() && gambarUri != null) {
                        isUploading = true
                        val tempId = UUID.randomUUID().toString()
                        val tempatWisata = TempatWisata(id = tempId, nama = nama, deskripsi = deskripsi)

                        uploadImageToFirestore(
                            firestore,
                            context,
                            gambarUri!!,
                            tempatWisata,
                            onSuccess = { uploadedTempat ->
                                isUploading = false
                                onTambah(nama, deskripsi, uploadedTempat.gambarUriString)
                                onDismiss()
                            },
                            onFailure = { e ->
                                isUploading = false
                                Log.e("Firestore", "Failed to upload image/data", e)
                                Toast.makeText(context, "Gagal Tambah: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        Toast.makeText(context, "Nama, Deskripsi, dan Gambar harus diisi", Toast.LENGTH_SHORT).show()
                    }
                },
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White)
                } else {
                    Text("Tambah")
                }
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(backgroundColor = MaterialTheme.colors.surface),
                enabled = !isUploading
            ) {
                Text("Batal", color = MaterialTheme.colors.onSurface)
            }
        }
    )
}

// ----------------------------------------------------
// HELPER FUNCTIONS (Bab 6, Bab 8, Bab 9)
// ----------------------------------------------------

/**
 * Fungsi untuk menyimpan gambar secara lokal (Bab 8)
 */
fun saveImageLocally(context: Context, uri: Uri): String {
    try {
        val inputStream = context.contentResolver.openInputStream(uri)
        val file = File(context.filesDir, "image_${System.currentTimeMillis()}.jpg")
        inputStream?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }
        return file.absolutePath
    } catch (e: Exception) {
        Log.e("ImageSave", "Error saving image", e)
        throw e
    }
}


/**
 * Fungsi untuk menyimpan gambar lokal dan upload data ke Firestore (Bab 5, Bab 8)
 */
fun uploadImageToFirestore(
    firestore: FirebaseFirestore,
    context: Context,
    imageUri: Uri,
    tempatWisata: TempatWisata,
    onSuccess: (TempatWisata) -> Unit,
    onFailure: (Exception) -> Unit
) {
    val storage = FirebaseStorage.getInstance()
    val storageRef = storage.reference
    // Gunakan UUID untuk nama file gambar yang unik
    val imageFileName = "images/${UUID.randomUUID()}.jpg"
    val imageRef = storageRef.child(imageFileName)

    // Dapatkan instance Room Database di luar Coroutine
    val db = Room.databaseBuilder(context, AppDatabase::class.java, "travelupa-database").build()
    val imageDao = db.imageDao()

    CoroutineScope(Dispatchers.IO).launch { // Melakukan operasi I/O dan Network di background thread
        try {
            // STEP 1: Upload gambar ke Storage dan menunggu hingga selesai
            imageRef.putFile(imageUri).await()

            // STEP 2: Mendapatkan Download URL
            val imageUrl = imageRef.downloadUrl.await().toString()

            // STEP 3: Simpan gambar ke penyimpanan lokal (untuk Gallery/Room)
            val localPath = withContext(Dispatchers.Main) {
                // Operasi file/context yang mungkin membutuhkan Main Thread,
                // meskipun saveImageLocally bisa aman di IO jika diimplementasikan dengan benar.
                saveImageLocally(context, imageUri)
            }

            // STEP 4: Persiapan Data untuk Firestore
            // Generate ID Firestore unik (sebelum set)
            val dbDocumentId = firestore.collection("tempat_wisata").document().id

            // Update model dengan URL gambar dan ID dokumen baru
            val updatedTempatWisata = tempatWisata.copy(
                gambarUriString = imageUrl,
                id = dbDocumentId // ID dokumen Firestore
            )

            // STEP 5: Simpan data Tempat Wisata ke Firestore
            firestore.collection("tempat_wisata")
                .document(updatedTempatWisata.id)
                .set(updatedTempatWisata)
                .addOnSuccessListener {
                    // STEP 6: Simpan entitas Room setelah sukses Firestore
                    CoroutineScope(Dispatchers.IO).launch {
                        imageDao.insert(ImageEntity(localPath = localPath, tempatWisataId = updatedTempatWisata.id))
                    }
                    onSuccess(updatedTempatWisata)
                }
                .addOnFailureListener { e ->
                    // Jika Firestore gagal
                    onFailure(e)
                }
                .await() // Tunggu hasil set Firestore (penting untuk menangkap exception segera)

        } catch (e: Exception) {
            // Menangkap kegagalan dari .await() (Storage gagal, Network error, dll.)
            Log.e("UPLOAD_ERROR", "Failed to upload or save data: ${e.message}", e)
            onFailure(e)
        }
    }
}


/**
 * Helper function untuk menyimpan Bitmap dari CameraX ke Uri (Bab 9)
 */
fun saveBitmapToUri(context: Context, bitmap: Bitmap): Uri {
    val file = File(context.cacheDir, "${UUID.randomUUID()}.jpg")
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
    outputStream.close()
    return Uri.fromFile(file)
}

// ----------------------------------------------------
// GALLERY AND CAMERAX SCREENS (Bab 8, Bab 9)
// ----------------------------------------------------

/**
 * Halaman Gallery (Bab 8, Bab 9)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GalleryScreen(
    imageDao: ImageDao,
    onImageSelected: (Uri) -> Unit,
    onBack: () -> Unit
) {
    val images by imageDao.getAllImages().collectAsState(initial = emptyList())
    var showAddImageDialog by remember { mutableStateOf(false) }
    var selectedImageEntity by remember { mutableStateOf<ImageEntity?>(null) }
    var showDeleteConfirmation by remember { mutableStateOf<ImageEntity?>(null) }
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Gallery") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddImageDialog = true },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add Image")
            }
        }
    ) { paddingValues ->
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            modifier = Modifier.padding(paddingValues)
        ) {
            items(images) { image ->
                Image(
                    painter = rememberAsyncImagePainter(model = image.localPath),
                    contentDescription = null,
                    modifier = Modifier
                        .size(100.dp)
                        .padding(4.dp)
                        .clickable {
                            selectedImageEntity = image
                            onImageSelected (Uri.parse(image.localPath))
                        },
                    contentScale = ContentScale.Crop
                )
            }
        }
    }

    if (showAddImageDialog) {
        AddImageDialog(
            onDismiss = { showAddImageDialog = false },
            onImageAdded = { uri ->
                try {
                    val localPath = saveImageLocally(context, uri)
                    val newImage = ImageEntity(localPath = localPath)
                    coroutineScope.launch(Dispatchers.IO) {
                        imageDao.insert(newImage)
                    }
                    showAddImageDialog = false
                } catch (e: Exception) {
                    Log.e("ImageSave", "Failed to save image", e)
                }
            }
        )
    }

    selectedImageEntity?.let { imageEntity ->
        ImageDetailDialog(
            imageEntity = imageEntity,
            onDismiss = { selectedImageEntity = null },
            onDelete = { imageToDelete ->
                showDeleteConfirmation = imageToDelete
            }
        )
    }

    showDeleteConfirmation?.let { imageToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = null },
            title = { Text("Delete Image") },
            text = { Text("Are you sure you want to delete this image?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        coroutineScope.launch(Dispatchers.IO) {
                            imageDao.delete(imageToDelete)
                            val file = File(imageToDelete.localPath)
                            if (file.exists()) {
                                file.delete()
                            }
                        }
                        showDeleteConfirmation = null
                        selectedImageEntity = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirmation = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}


/**
 * Dialog untuk Menambah Gambar (Galeri atau Kamera) (Bab 9)
 */
@Composable
fun AddImageDialog(
    onDismiss: () -> Unit,
    onImageAdded: (Uri) -> Unit
) {
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    val context = LocalContext.current
    val imagePickerLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri: Uri? -> imageUri = uri }
    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let {
            val uri = saveBitmapToUri(context, it)
            imageUri = uri
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Image") },
        text = {
            Column {
                imageUri?.let { uri ->
                    Image(
                        painter = rememberAsyncImagePainter(model = uri),
                        contentDescription = "Selected Image",
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentScale = ContentScale.Crop
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Button(onClick = { imagePickerLauncher.launch("image/*") }, modifier = Modifier.weight(1f)) { Text("File") }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = { cameraLauncher.launch(null) }, modifier = Modifier.weight(1f)) { Text("Camera") }
                }
            }
        },
        confirmButton = {
            Button(onClick = { imageUri?.let { uri -> onImageAdded(uri) } }, enabled = imageUri != null) { Text("Add") }
        },
        dismissButton = {
            Button(onClick = onDismiss) { Text("Cancel") }
        }
    )
}


/**
 * Dialog Detail Gambar (Bab 9)
 */
@Composable
fun ImageDetailDialog(
    imageEntity: ImageEntity,
    onDismiss: () -> Unit,
    onDelete: (ImageEntity) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        text = {
            Image(
                painter = rememberAsyncImagePainter(model = imageEntity.localPath),
                contentDescription = "Detailed Image",
                modifier = Modifier.fillMaxWidth().aspectRatio(1f),
                contentScale = ContentScale.Crop
            )
        },
        confirmButton = {
            Row {
                Button(onClick = { onDelete(imageEntity) }) { Text("Delete") }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) { Text("Close") }
            }
        }
    )
}