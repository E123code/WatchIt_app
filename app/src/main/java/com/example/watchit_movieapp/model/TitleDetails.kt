package com.example.watchit_movieapp.model

import com.google.gson.annotations.SerializedName

data class TitleDetails(
    @SerializedName("id") val id: Int,

    @SerializedName("poster_path")
    val poster: String? = null,

    @SerializedName("title")
    val title: String? = null, // הורדנו private

    @SerializedName("name")
    val tvName: String? = null, // הורדנו private

    @SerializedName("release_date")
    val relDate: String? = null,

    @SerializedName("first_air_date")
    val airDate: String? = null,

    @SerializedName("vote_average")
    val rating: Double = 0.0,

    @SerializedName("overview")
    val overview: String? = null,

    @SerializedName("genres")
    val genreObj: List<Genre>? = emptyList(),

    @SerializedName("media_type")
    var mediaType: String? = null,

    @SerializedName("runtime")
    val runtime: Int?,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int?, // רק בסדרות

    // הרחבות (append_to_response)
    @SerializedName("credits") val credits: CreditsResponse?,
    @SerializedName("watch/providers") val watchProviders: WatchProvidersResponse?,

    @SerializedName("release_dates") val releaseDates: ReleaseDatesResponse?, // לסרטים
    @SerializedName("content_ratings") val contentRatings: ContentRatingsResponse?, // לסדרות

    var isFavorite: Boolean = false
) {
    val name: String
        get() = when (mediaType) {
            "movie" -> title ?: "Unknown Movie"
            "tv" -> tvName ?: "Unknown TV Show"
            else -> title ?: tvName ?: "Unknown" // Fallback ליתר ביטחון
        }

    val date: String
        get() = when (mediaType) {
            "movie" -> (relDate ?: "N/A").take(4)
            "tv" -> (airDate ?: "N/A").take(4)
            else ->"N/A"
        }

    val genres : String
        get() = genreObj?.take(3)?.joinToString(" • ") { it.name } ?: "No genres"


    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$poster"

    val duration: String
        get() = when(mediaType) {
            "movie" -> if (runtime != null && runtime > 0) "$runtime min" else "N/A"
            "tv" -> if (numberOfSeasons != null && numberOfSeasons > 0){
                if (numberOfSeasons == 1) "1 Season" else "$numberOfSeasons Seasons"
            } else "N/A"
            else -> "N/A"
        }
    val ageRating: String
        get() {

            val countriesToSearch = listOf("IL", "DE") // סדר עדיפויות: קודם ישראל, אז גרמניה

            val rawRating = when (mediaType) {
                "movie" -> {
                    // מחפש את המדינה הראשונה מהרשימה שקיימת בתוצאות
                    val result = countriesToSearch.firstNotNullOfOrNull { code ->
                        releaseDates?.results?.find { it.countryCode == code }
                    }
                    result?.releaseDates?.firstOrNull()?.certification
                }
                "tv" -> {
                    // אותו דבר עבור סדרות
                    val result = countriesToSearch.firstNotNullOfOrNull { code ->
                        contentRatings?.results?.find { it.countryCode == code }
                    }
                    result?.rating
                }
                else -> null
            }

            // לוגיקת התצוגה
            return when {
                rawRating.isNullOrEmpty() -> "N/A"
                rawRating == "0" || rawRating == "6" -> "All" // בגרמניה 0 ו-6 הם לכל הגילאים
                rawRating.all { it.isDigit() } -> "$rawRating+" // אם זה מספר (כמו 12), נוסיף פלוס
                else -> rawRating // אם זה טקסט (כמו בשלוחות אחרות), נציג כפי שהוא
            }
        }


    fun toggleFavorite() = apply { isFavorite = !isFavorite}

}

data class Genre(
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class CreditsResponse(
    @SerializedName("cast") val cast: List<CastMember>
)

data class CastMember(
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
){
    val fullCastUrl: String
        get() = "https://image.tmdb.org/t/p/w185${profilePath}"
}

// 3. מודלים של ספקי צפייה (Watch Providers)
data class WatchProvidersResponse(
    @SerializedName("results") val results: Map<String, CountryProviders>?
)

data class CountryProviders(
    @SerializedName("flatrate") val membership: List<ProviderItem>?
)

data class ProviderItem(
    @SerializedName("provider_name") val name: String,
    @SerializedName("logo_path") val logoPath: String
){
    val fullLogoUrl: String
        get() = "https://image.tmdb.org/t/p/original${logoPath}"
}

    data class ReleaseDatesResponse(@SerializedName("results") val results: List<ReleaseDateResult>?)

    data class ReleaseDateResult(
        @SerializedName("iso_3166_1") val countryCode: String,
        @SerializedName("release_dates") val releaseDates: List<Certification>
    )

    data class Certification(@SerializedName("certification") val certification: String)

    // לסדרות
    data class ContentRatingsResponse(@SerializedName("results") val results: List<ContentRatingResult>?)

    data class ContentRatingResult(
        @SerializedName("iso_3166_1") val countryCode: String,
        @SerializedName("rating") val rating: String
    )
