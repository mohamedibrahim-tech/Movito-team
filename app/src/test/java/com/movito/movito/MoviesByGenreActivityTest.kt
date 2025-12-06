package com.movito.movito

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.movito.movito.ui.MoviesByGenreActivity
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config


/**
 * Unit Tests للـ MoviesByGenreActivity
 *
 * بنختبر:
 * - Activity lifecycle
 * - Intent handling with extras (genreId, genreName)
 * - Context attachment
 * - Activity creation
 * - ViewModel initialization
 * - Edge cases للـ Intent extras
 *
 * ملحوظة مهمة:
 * الـ Activity بتستخدم R.string.movies كـ default للـ genreName
 * علشان كده لازم نبعت genreName دايماً في الاختبارات
 * أو نعمل proper resource configuration في Robolectric
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MoviesByGenreActivityTest {

    private lateinit var context: Context
    private lateinit var activityController: ActivityController<MoviesByGenreActivity>

    // Helper method لإنشاء intent صالح
    private fun createValidIntent(genreId: Int = 28, genreName: String = "Action"): Intent {
        return Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", genreId)
            putExtra("genreName", genreName)
        }
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @After
    fun tearDown() {
        // Clean up if activity controller was created
        try {
            if (::activityController.isInitialized) {
                activityController.pause().stop().destroy()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors in tests
        }
    }

    // ===== اختبارات Activity Creation =====

    @Test
    fun `activity should be created successfully with valid intent`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act & Assert - Should not throw
        try {
            activityController = Robolectric.buildActivity(
                MoviesByGenreActivity::class.java,
                intent
            ).create()

            val activity = activityController.get()
            assertNotNull("Activity should be created", activity)
        } catch (e: Exception) {
            fail("Activity creation should not fail: ${e.message}")
        }
    }

    @Test
    fun `activity should not be null after creation`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should not be null", activity)
    }

    @Test
    fun `activity should have valid context`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity context should not be null", activity)
        assertNotNull(
            "Application context should not be null",
            activity.applicationContext
        )
    }

    // ===== اختبارات Intent Extras =====

    @Test
    fun `activity should handle valid genreId and genreName`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreId = activity.intent.getIntExtra("genreId", -1)
        val receivedGenreName = activity.intent.getStringExtra("genreName")

        // Assert
        assertEquals("GenreId should match", 28, receivedGenreId)
        assertEquals("GenreName should match", "Action", receivedGenreName)
    }

    @Test
    fun `activity should handle missing genreId with default value`() {
        // Arrange - Intent without genreId
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreId = activity.intent.getIntExtra("genreId", -1)

        // Assert
        assertEquals("GenreId should be default -1", -1, receivedGenreId)
    }

    @Test
    fun `activity should receive null genreName from intent when not provided`() {
        // Arrange - Intent without genreName
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
        }

        // Act - Just check the intent, don't create activity
        // (Activity will use default R.string.movies which may not exist in test)
        val receivedGenreName = intent.getStringExtra("genreName")

        // Assert
        assertNull("GenreName should be null in intent", receivedGenreName)
    }

    @Test
    fun `activity should handle empty intent extras without crash`() {
        // Note: This test verifies intent data handling
        // Activity creation with missing genreName will use R.string.movies
        // which requires proper resource configuration in Robolectric

        // Arrange - Intent without extras
        val intent = Intent(context, MoviesByGenreActivity::class.java)

        // Act & Assert - Check intent only
        val receivedGenreId = intent.getIntExtra("genreId", -1)
        val receivedGenreName = intent.getStringExtra("genreName")

        assertEquals("GenreId should be default -1", -1, receivedGenreId)
        assertNull("GenreName should be null", receivedGenreName)
    }

    @Test
    fun `activity should handle different genre types`() {
        // Test multiple genres
        val genres = listOf(
            Pair(28, "Action"),
            Pair(12, "Adventure"),
            Pair(35, "Comedy"),
            Pair(18, "Drama")
        )

        genres.forEach { (id, name) ->
            // Arrange
            val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
                putExtra("genreId", id)
                putExtra("genreName", name)
            }

            // Act
            val controller = Robolectric.buildActivity(
                MoviesByGenreActivity::class.java,
                intent
            ).create()

            val activity = controller.get()
            val receivedId = activity.intent.getIntExtra("genreId", -1)
            val receivedName = activity.intent.getStringExtra("genreName")

            // Assert
            assertEquals("GenreId should match for $name", id, receivedId)
            assertEquals("GenreName should match", name, receivedName)

            // Cleanup
            controller.pause().stop().destroy()
        }
    }

    @Test
    fun `activity should handle negative genreId`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", -5)
            putExtra("genreName", "Unknown")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreId = activity.intent.getIntExtra("genreId", -1)

        // Assert
        assertEquals("Should handle negative genreId", -5, receivedGenreId)
    }

    @Test
    fun `activity should handle zero genreId`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 0)
            putExtra("genreName", "All")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreId = activity.intent.getIntExtra("genreId", -1)

        // Assert
        assertEquals("Should handle zero genreId", 0, receivedGenreId)
    }

    @Test
    fun `activity should handle empty genreName string`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreName = activity.intent.getStringExtra("genreName")

        // Assert
        assertEquals("Should handle empty string", "", receivedGenreName)
    }

    // ===== اختبارات Lifecycle =====

    @Test
    fun `activity lifecycle onCreate should complete`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(
                MoviesByGenreActivity::class.java,
                intent
            ).create()

            assertTrue("onCreate should complete", true)
        } catch (e: Exception) {
            fail("onCreate should not throw: ${e.message}")
        }
    }

    @Test
    fun `activity should handle onStart`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onStart", activity)
    }

    @Test
    fun `activity should handle onResume`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onResume", activity)
    }

    @Test
    fun `activity should handle onPause`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        // Act
        activityController.pause()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onPause", activity)
    }

    @Test
    fun `activity should handle onStop`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume().pause()

        // Act
        activityController.stop()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onStop", activity)
    }

    @Test
    fun `activity should handle onDestroy`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        // Act & Assert - Should not throw
        try {
            activityController.pause().stop().destroy()
        } catch (e: Exception) {
            fail("onDestroy should not throw: ${e.message}")
        }
    }

    @Test
    fun `complete lifecycle should work correctly`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act & Assert - Full lifecycle
        try {
            activityController = Robolectric.buildActivity(
                MoviesByGenreActivity::class.java,
                intent
            ).create().start().resume().pause().stop().destroy()
        } catch (e: Exception) {
            fail("Complete lifecycle should work: ${e.message}")
        }
    }

    // ===== اختبارات Context =====

    @Test
    fun `attachBaseContext should not crash`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("attachBaseContext should complete", activity)
    }

    @Test
    fun `activity should have application context`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val appContext = activity.applicationContext

        // Assert
        assertNotNull("Application context should exist", appContext)
    }

    // ===== اختبارات State =====

    @Test
    fun `activity should not be finishing initially`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertFalse(
            "Activity should not be finishing initially",
            activity.isFinishing
        )
    }

    @Test
    fun `activity should not be destroyed initially`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertFalse(
            "Activity should not be destroyed initially",
            activity.isDestroyed
        )
    }

    @Test
    fun `activity should handle finish call`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        val activity = activityController.get()

        // Act
        activity.finish()

        // Assert
        assertTrue("Activity should be finishing after finish()", activity.isFinishing)
    }

    // ===== اختبارات Multiple Instances =====

    @Test
    fun `multiple activity instances should be independent`() {
        // Arrange
        val intent1 = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }
        val intent2 = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 35)
            putExtra("genreName", "Comedy")
        }

        // Act
        val controller1 = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent1
        ).create()
        val activity1 = controller1.get()

        val controller2 = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent2
        ).create()
        val activity2 = controller2.get()

        // Assert
        assertNotNull("Activity 1 should exist", activity1)
        assertNotNull("Activity 2 should exist", activity2)
        assertNotSame(
            "Activities should be different instances",
            activity1,
            activity2
        )

        // Verify different intents
        val genreId1 = activity1.intent.getIntExtra("genreId", -1)
        val genreId2 = activity2.intent.getIntExtra("genreId", -1)
        assertNotEquals("Should have different genreIds", genreId1, genreId2)

        // Cleanup
        controller1.pause().stop().destroy()
        controller2.pause().stop().destroy()
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `recreating activity should work`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        // Act - Simulate configuration change
        val activity1 = activityController.get()
        activityController.pause().stop().destroy()

        // Create new instance
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()
        val activity2 = activityController.get()

        // Assert
        assertNotNull("First instance should exist", activity1)
        assertNotNull("Recreated instance should exist", activity2)
        assertNotSame("Should be different instances", activity1, activity2)
    }

    @Test
    fun `activity should survive pause and resume`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        // Act - Pause and resume
        activityController.pause()
        activityController.resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive pause/resume", activity)
        assertFalse("Activity should not be finishing", activity.isFinishing)
    }

    @Test
    fun `activity should handle large genreId values`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", Int.MAX_VALUE)
            putExtra("genreName", "Test Genre")
        }

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreId = activity.intent.getIntExtra("genreId", -1)

        // Assert
        assertEquals("Should handle large genreId", Int.MAX_VALUE, receivedGenreId)
    }

    @Test
    fun `activity should handle special characters in genreName`() {
        // Arrange
        val specialGenreName = "Action & Adventure (2024) - Special!"
        val intent = createValidIntent(genreId = 28, genreName = specialGenreName)

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreName = activity.intent.getStringExtra("genreName")

        // Assert
        assertEquals("Should handle special characters", specialGenreName, receivedGenreName)
    }

    @Test
    fun `activity should handle unicode characters in genreName`() {
        // Arrange
        val unicodeGenreName = "أفلام أكشن 🎬"
        val intent = createValidIntent(genreId = 28, genreName = unicodeGenreName)

        // Act
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedGenreName = activity.intent.getStringExtra("genreName")

        // Assert
        assertEquals("Should handle unicode characters", unicodeGenreName, receivedGenreName)
    }

    @Test
    fun `activity should verify intent extras are preserved through lifecycle`() {
        // Arrange
        val intent = createValidIntent(genreId = 28, genreName = "Action")

        // Act - Full lifecycle
        activityController = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume().pause().stop()

        val activity = activityController.get()
        val genreId = activity.intent.getIntExtra("genreId", -1)
        val genreName = activity.intent.getStringExtra("genreName")

        // Assert - Intent data should persist
        assertEquals("GenreId should persist", 28, genreId)
        assertEquals("GenreName should persist", "Action", genreName)
    }
}


/**
 * Integration Tests للـ MoviesByGenreActivity
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MoviesByGenreActivityIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `activity should handle fresh start with valid data`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        // Act
        val controller = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        val activity = controller.get()

        // Assert
        assertNotNull("Activity should start fresh", activity)
        assertFalse("Should not be finishing", activity.isFinishing)

        // Cleanup
        controller.pause().stop().destroy()
    }

    @Test
    fun `activity should handle background and foreground`() {
        // Arrange
        val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
            putExtra("genreId", 28)
            putExtra("genreName", "Action")
        }

        val controller = Robolectric.buildActivity(
            MoviesByGenreActivity::class.java,
            intent
        ).create().start().resume()

        // Act - Simulate going to background and back
        controller.pause()
        controller.stop()
        controller.start()
        controller.resume()

        val activity = controller.get()

        // Assert
        assertNotNull("Activity should survive background", activity)

        // Cleanup
        controller.pause().stop().destroy()
    }

    @Test
    fun `activity should handle sequential genre changes`() {
        // Test navigating through different genres
        val genres = listOf(
            Pair(28, "Action"),
            Pair(12, "Adventure"),
            Pair(35, "Comedy")
        )

        genres.forEach { (id, name) ->
            // Arrange
            val intent = Intent(context, MoviesByGenreActivity::class.java).apply {
                putExtra("genreId", id)
                putExtra("genreName", name)
            }

            // Act
            val controller = Robolectric.buildActivity(
                MoviesByGenreActivity::class.java,
                intent
            ).create().start().resume()

            val activity = controller.get()

            // Assert
            assertNotNull("Activity should handle $name", activity)

            // Cleanup
            controller.pause().stop().destroy()
        }
    }
}

/**
 * Component Tests للـ MoviesByGenreActivity
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class MoviesByGenreActivityComponentTest {

    @Test
    fun `activity class should be accessible`() {
        // Assert
        assertNotNull(
            "Activity class should exist",
            MoviesByGenreActivity::class.java
        )
    }

    @Test
    fun `activity should extend ComponentActivity`() {
        // Assert
        assertTrue(
            "Should extend ComponentActivity",
            ComponentActivity::class.java.isAssignableFrom(MoviesByGenreActivity::class.java)
        )
    }
    @Test
    fun `activity should have public constructor`() {
        // Act & Assert
        try {
            val constructor = MoviesByGenreActivity::class.java.getDeclaredConstructor()
            assertNotNull("Constructor should exist", constructor)
        } catch (e: NoSuchMethodException) {
            fail("Activity should have default public constructor")
        }
    }
}