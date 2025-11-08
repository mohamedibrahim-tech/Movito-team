package com.movito.movito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch


data class Movie(
    val id: Int,
    val title: String,
    val year: String,
    val time: String,
    val posterUrl: Int // بنستخدم ID من drawable مؤقتاً
)

/**
 * يمثل كل الحالات الممكنة للشاشة (تحميل، نجاح، خطأ، بيانات).
 */
data class HomeUiState(
    val isLoading: Boolean = false,
    val isRefreshing: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val error: String? = null
)

/**
 * الـ ViewModel هو "العقل" المسئول عن إدارة بيانات شاشة الهوم.
 * وظيفته يجيب البيانات (من API) ويحدّث الـ UI state.
 */
class HomeViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    // بيانات مزيفة مؤقتة (بدل الـ API حالياً)
    private val mockMovies = listOf(
        Movie(1, "Cosmic Echoes", "2025", "2h 15m", R.drawable.poster_test),
        Movie(2, "Cyber City", "2024", "1h 55m", R.drawable.poster_test),
        Movie(3, "Ocean's Deep", "2023", "2h 05m", R.drawable.poster_test),
        Movie(4, "Mountain Peak", "2025", "2h 20m", R.drawable.poster_test),
        Movie(5, "Desert Runner", "2024", "1h 45m", R.drawable.poster_test),
        Movie(6, "Space Pirates", "2023", "2h 00m", R.drawable.poster_test),
        Movie(7, "Time Bender", "2025", "2h 30m", R.drawable.poster_test),
        Movie(8, "Alpha Centauri", "2024", "2h 10m", R.drawable.poster_test)
    )

    init {
        // تحميل البيانات أول ما الشاشة تفتح
        loadMovies(isLoading = true)
    }

    /**
     * دالة مسئولة عن جلب بيانات الأفلام (بشكل مزيف حالياً).
     * بتحدّث الـ state عشان تعرض "دايرة تحميل" وهي بتجيب البيانات.
     */
    fun loadMovies(isLoading: Boolean = false, isRefreshing: Boolean = false) {
        viewModelScope.launch {
            // 1. اعرض حالة التحميل
            _uiState.update {
                it.copy(
                    isLoading = isLoading,
                    isRefreshing = isRefreshing,
                    error = null
                )
            }

            // 2. تظاهر إنك بتجيب بيانات (تأخير ثانيتين)
            delay(2000)

            // 3. اعرض البيانات (أو خطأ، لو حابب)
            _uiState.update {
                it.copy(
                    isLoading = false,
                    isRefreshing = false,
                    movies = mockMovies
                    // error = "Failed to load" // (لو عايز تجرب شكل الخطأ)
                )
            }
        }
    }
}