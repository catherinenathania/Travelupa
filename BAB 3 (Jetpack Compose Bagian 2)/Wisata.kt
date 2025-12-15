package com.example.travelupa.model

data class Wisata(
    val id: Int,
    val nama: String,
    val lokasi: String, // Contoh: Malang, Banyuwangi
    val deskripsiSingkat: String,
    val gambarResId: Int // Resource ID gambar (dari R.drawable)
)
