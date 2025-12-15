package com.example.travelupa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.travelupa.data.WisataDataSource
import com.example.travelupa.model.Wisata
import com.example.travelupa.ui.theme.TravelupaTheme

// Objek untuk mendefinisikan rute/destinasi navigasi
object Destinasi {
    const val SPLASH = "splash"
    const val HOME = "home"
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TravelupaTheme {
                TravelupaApp() // Panggil wadah navigasi utama
            }
        }
    }
}

// =========================================================================
// WADAH NAVIGASI UTAMA (Container Aplikasi)
// =========================================================================

@Composable
fun TravelupaApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Destinasi.SPLASH
    ) {
        // Rute 1: Splash Screen
        composable(Destinasi.SPLASH) {
            ComposeBasic(
                onContinueClicked = {
                    navController.navigate(Destinasi.HOME) {
                        popUpTo(Destinasi.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Rute 2: Home Screen
        composable(Destinasi.HOME) {
            HomeScreen()
        }
    }
}


// =========================================================================
// 1. LAYAR SPLASH (Selamat Datang)
// =========================================================================

@Composable
fun ComposeBasic(onContinueClicked: () -> Unit) {
    val buttonText = "Mulai Jelajahi"

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Image(
                painter = painterResource(id = R.drawable.travelupa_logo),
                contentDescription = "Travelupa Logo",
                modifier = Modifier
                    .height(700.dp)
                    .width(700.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Selamat Datang di Travelupa!",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = onContinueClicked,
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(buttonText)
            }
        }
    }
}


// =========================================================================
// 2. LAYAR HOME (Daftar Wisata)
// =========================================================================

@OptIn(ExperimentalMaterial3Api::class) // Diperlukan untuk CenterAlignedTopAppBar
@Composable
fun HomeScreen() {
    val wisataList = WisataDataSource.daftarWisata

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Daftar Wisata Jawa Timur") }
            )
        }
    ) { paddingValues ->
        // LazyColumn: Efisien untuk menampilkan daftar panjang
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 8.dp),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(wisataList) { wisata ->
                WisataCard(wisata = wisata)
            }
        }
    }
}


// =========================================================================
// 3. KOMPONEN CARD UNTUK SETIAP ITEM WISATA
// =========================================================================

@Composable
fun WisataCard(wisata: Wisata) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Gambar Thumbnail Wisata
            Image(
                painter = painterResource(id = wisata.gambarResId),
                contentDescription = wisata.nama,
                modifier = Modifier
                    .size(80.dp) // Ukuran thumbnail
                    .clip(RoundedCornerShape(8.dp)), // Memotong gambar agar sudutnya melengkung
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Detail Teks
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(wisata.nama, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1)
                Text(wisata.lokasi, color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(wisata.deskripsiSingkat, fontSize = 14.sp, maxLines = 2)
            }
        }
    }
}

// Preview dihapus agar tidak mengganggu, tetapi bisa ditambahkan kembali jika diperlukan.
