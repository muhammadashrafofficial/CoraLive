package com.example.coralive.core.model


enum class League(
    val code: String,
    val titleAr: String,
    val titleEn: String
) {
    PREMIER_LEAGUE("eng.1", "الدوري الإنجليزي", "Premier League"),
    LA_LIGA("esp.1", "الدوري الإسباني", "LaLiga"),
    SERIE_A("ita.1", "الدوري الإيطالي", "Serie A"),
    BUNDESLIGA("ger.1", "الدوري الألماني", "Bundesliga"),
    LIGUE_1("fra.1", "الدوري الفرنسي", "Ligue 1"),
    CHAMPIONS_LEAGUE("uefa.champions", "دوري أبطال أوروبا", "Champions League")
}
