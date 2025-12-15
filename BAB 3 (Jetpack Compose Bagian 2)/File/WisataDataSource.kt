package com.example.travelupa.data

import com.example.travelupa.R
import com.example.travelupa.model.Wisata

// Sumber data dummy untuk daftar wisata
object WisataDataSource {
    val daftarWisata = listOf(
        Wisata(1, "Gunung Bromo", "Probolinggo", "Gunung berapi ikonik dengan lautan pasir.", R.drawable.gunungbromo),
        Wisata(2, "Kawah Ijen", "Banyuwangi", "Fenomena api biru yang langka di dunia.", R.drawable.kawahijen),
        Wisata(3, "Jatim Park 2", "Malang", "Taman rekreasi edukatif dengan museum satwa.", R.drawable.jatimpark2),
        Wisata(4, "Pantai Merah", "Banyuwangi", "Pantai dengan pasir merah unik di kawasan TN Alas Purwo.", R.drawable.pantaimerah),
        Wisata(6, "Air Terjun Tumpak Sewu", "Lumajang", "Air terjun tirai yang spektakuler, sering disebut Niagara Indonesia.", R.drawable.airterjun),
    )
}
