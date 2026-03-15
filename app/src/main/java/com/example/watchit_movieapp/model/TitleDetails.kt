package com.example.watchit_movieapp.model

import com.google.gson.annotations.SerializedName

//data class of title details (the same as media Item but more detailed)
data class TitleDetails(
    @SerializedName("id") val id: Int,

    @SerializedName("poster_path")
    val poster: String? = null,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("name")
    val tvName: String? = null,

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

    @SerializedName("runtime")// the length  of movie
    val runtime: Int?,
    @SerializedName("number_of_seasons")
    val numberOfSeasons: Int?,

    @SerializedName("credits") val credits: CreditsResponse?, //the actors
    @SerializedName("watch/providers") val watchProviders: WatchProvidersResponse?,//where to watch (streaming services)

    @SerializedName("release_dates") val releaseDates: ReleaseDatesResponse?, // Movie-specific: Used to extract age certification (PG, 16+, etc.) per country.
    @SerializedName("content_ratings") val contentRatings: ContentRatingsResponse?,// TV-specific: Used to extract age rating/parental guidance for show episodes.

    var isFavorite: Boolean = false
) {
    val name: String
        get() = when (mediaType) {
            "movie" -> title ?: "Unknown Movie"
            "tv" -> tvName ?: "Unknown TV Show"
            else -> title ?: tvName ?: "Unknown"
        }

    val date: String
        get() = when (mediaType) {
            "movie" -> (relDate ?: "N/A").take(4)
            "tv" -> (airDate ?: "N/A").take(4)
            else -> "N/A"
        }

    val genres: String
        //to get list of genres takes top 3
        get() = genreObj?.take(3)?.joinToString(" • ") { it.name } ?: "No genres"


    val fullPosterUrl: String
        get() = "https://image.tmdb.org/t/p/w500$poster"

    val duration: String
        // to determine duration based on type
        get() = when (mediaType) {
            "movie" -> if (runtime != null && runtime > 0) "$runtime min" else "N/A"
            "tv" -> if (numberOfSeasons != null && numberOfSeasons > 0) {
                if (numberOfSeasons == 1) "1 Season" else "$numberOfSeasons Seasons"
            } else "N/A"

            else -> "N/A"
        }
    val ageRating: String
        //to get the exact age rating base on country and media type
        get() {

            val countriesToSearch = listOf("IL", "DE","US")

            val rawRating = when (mediaType) {
                "movie" -> {

                    val result = countriesToSearch.firstNotNullOfOrNull { code ->
                        releaseDates?.results?.find { it.countryCode == code }
                    }
                    result?.releaseDates?.firstOrNull()?.certification
                }

                "tv" -> {
                    val result = countriesToSearch.firstNotNullOfOrNull { code ->
                        contentRatings?.results?.find { it.countryCode == code }
                    }
                    result?.rating
                }

                else -> null
            }

            return when {
                rawRating.isNullOrEmpty() -> "N/A"
                rawRating == "0" || rawRating == "6" || rawRating.equals("G", ignoreCase = true) || rawRating.equals("TV-G", ignoreCase = true) -> "All"
                rawRating.equals("R", ignoreCase = true) -> "18+"
                rawRating.equals("NC-17", ignoreCase = true) -> "18+"
                rawRating.any { it.isDigit() } -> {
                    val digits = rawRating.filter { it.isDigit() }
                    "$digits+"
                }

                else -> rawRating
            }
        }


    fun toggleFavorite() = apply { isFavorite = !isFavorite }// to change status of favorite or not

}

data class Genre(// to get the genres
    @SerializedName("id") val id: Int,
    @SerializedName("name") val name: String
)

data class CreditsResponse( //to get the cast members
    @SerializedName("cast") val cast: List<CastMember>
)


//data class for cast member
data class CastMember(
    @SerializedName("name") val name: String,
    @SerializedName("character") val character: String,
    @SerializedName("profile_path") val profilePath: String?
) {
    val fullCastUrl: String
        // the cast member profile picture
        get() = "https://image.tmdb.org/t/p/w185${profilePath}"
}

/**
 * Root response for streaming services.
 * Uses a Map where the key is the Country Code (e.g., "IL", "US") to support global availability.
 */
data class WatchProvidersResponse(
    @SerializedName("results") val results: Map<String, CountryProviders>?
)

/**
 * Represents providers available in a specific country.
 * "flatrate" refers to subscription-based services like Netflix or Disney+.
 */
data class CountryProviders(
    @SerializedName("flatrate") val membership: List<ProviderItem>?
)

/**
 * Individual streaming service data (e.g., Netflix, Apple TV).
 */
data class ProviderItem(
    @SerializedName("provider_name") val name: String,
    @SerializedName("logo_path") val logoPath: String
) {
    val fullLogoUrl: String
        get() = "https://image.tmdb.org/t/p/original${logoPath}"
}

/**
 * Movie-specific container for age certifications across different regions.
 */
data class ReleaseDatesResponse(@SerializedName("results") val results: List<ReleaseDateResult>?)

data class ReleaseDateResult(
    @SerializedName("iso_3166_1") val countryCode: String,// e.g., "US", "IL"
    @SerializedName("release_dates") val releaseDates: List<Certification>
)

data class Certification(@SerializedName("certification") val certification: String)

/**
 * TV Show-specific container  for  age ratings.
 */
data class ContentRatingsResponse(@SerializedName("results") val results: List<ContentRatingResult>?)

data class ContentRatingResult(
    @SerializedName("iso_3166_1") val countryCode: String,
    @SerializedName("rating") val rating: String
)
