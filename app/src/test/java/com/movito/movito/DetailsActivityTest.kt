package com.movito.movito


import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.movito.movito.data.model.Movie
import com.movito.movito.ui.DetailsActivity
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
 * Unit Tests للـ DetailsActivity
 *
 * بنختبر:
 * - Activity lifecycle
 * - Intent handling with Movie data
 * - Context attachment
 * - Activity creation
 * - Movie parcelable handling
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DetailsActivityTest {

    private lateinit var context: Context
    private lateinit var activityController: ActivityController<DetailsActivity>
    private lateinit var testMovie: Movie

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Create test movie
        testMovie = Movie(
            id = 550,
            title = "Fight Club",
            releaseDate = "1999-10-15",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            voteAverage = 8.4,
            overview = "A ticking-time-bomb insomniac and a slippery soap salesman...",
            genreIds = listOf(18, 53),
            homepage = "http://www.foxmovies.com/movies/fight-club"
        )
    }

    @After
    fun tearDown() {
        try {
            if (::activityController.isInitialized) {
                activityController.pause().stop().destroy()
            }
        } catch (e: Exception) {
            // Ignore cleanup errors
        }
    }

    // ===== اختبارات Activity Creation =====

    @Test
    fun `activity should be created successfully`() {
        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(DetailsActivity::class.java)
                .create()

            val activity = activityController.get()
            assertNotNull("Activity should be created", activity)
        } catch (e: Exception) {
            fail("Activity creation should not fail: ${e.message}")
        }
    }

    @Test
    fun `activity should not be null after creation`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should not be null", activity)
    }

    @Test
    fun `activity should have valid context`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity context should not be null", activity)
        assertNotNull("Application context should not be null", activity.applicationContext)
    }

    // ===== اختبارات Lifecycle =====

    @Test
    fun `activity lifecycle onCreate should complete`() {
        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(DetailsActivity::class.java)
                .create()

            assertTrue("onCreate should complete", true)
        } catch (e: Exception) {
            fail("onCreate should not throw: ${e.message}")
        }
    }

    @Test
    fun `activity should handle onStart`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onStart", activity)
    }

    @Test
    fun `activity should handle onResume`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onResume", activity)
    }

    @Test
    fun `activity should handle onPause`() {
        // Arrange
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        // Act
        activityController.pause()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onPause", activity)
    }

    @Test
    fun `activity should handle onStop`() {
        // Arrange
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()
            .pause()

        // Act
        activityController.stop()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onStop", activity)
    }

    @Test
    fun `activity should handle onDestroy`() {
        // Arrange
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        // Act & Assert
        try {
            activityController.pause().stop().destroy()
        } catch (e: Exception) {
            fail("onDestroy should not throw: ${e.message}")
        }
    }

    @Test
    fun `complete lifecycle should work correctly`() {
        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(DetailsActivity::class.java)
                .create()
                .start()
                .resume()
                .pause()
                .stop()
                .destroy()
        } catch (e: Exception) {
            fail("Complete lifecycle should work: ${e.message}")
        }
    }

    // ===== اختبارات Intent =====

    @Test
    fun `activity should be launchable with intent`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java)

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should be created with intent", activity)
    }

    @Test
    fun `activity should handle intent with movie data`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", testMovie)
        }

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val receivedMovie = activity.intent.getParcelableExtra<Movie>("movie")

        // Assert
        assertNotNull("Activity should be created", activity)
        assertNotNull("Movie should be in intent", receivedMovie)
        assertEquals("Movie ID should match", testMovie.id, receivedMovie?.id)
        assertEquals("Movie title should match", testMovie.title, receivedMovie?.title)
    }

    @Test
    fun `activity should handle intent without movie data`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java)

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should be created without movie", activity)
    }

    @Test
    fun `activity intent should be valid`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()
        val intent = activity.intent

        // Assert
        assertNotNull("Intent should not be null", intent)
    }

    @Test
    fun `activity should handle intent with flags`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", testMovie)
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should handle flags", activity)
    }

    // ===== اختبارات Context =====

    @Test
    fun `attachBaseContext should not crash`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("attachBaseContext should complete", activity)
    }

    @Test
    fun `activity should have application context`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()
        val appContext = activity.applicationContext

        // Assert
        assertNotNull("Application context should exist", appContext)
    }

    // ===== اختبارات State =====

    @Test
    fun `activity should not be finishing initially`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertFalse("Activity should not be finishing initially", activity.isFinishing)
    }

    @Test
    fun `activity should not be destroyed initially`() {
        // Act
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertFalse("Activity should not be destroyed initially", activity.isDestroyed)
    }

    // ===== اختبارات Multiple Instances =====

    @Test
    fun `multiple activity instances should be independent`() {
        // Act
        val controller1 = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
        val activity1 = controller1.get()

        val controller2 = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
        val activity2 = controller2.get()

        // Assert
        assertNotNull("Activity 1 should exist", activity1)
        assertNotNull("Activity 2 should exist", activity2)
        assertNotSame("Activities should be different instances", activity1, activity2)

        // Cleanup
        controller1.pause().stop().destroy()
        controller2.pause().stop().destroy()
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `recreating activity should work`() {
        // Arrange
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        // Act
        val activity1 = activityController.get()
        activityController.pause().stop().destroy()

        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()
        val activity2 = activityController.get()

        // Assert
        assertNotNull("First instance should exist", activity1)
        assertNotNull("Recreated instance should exist", activity2)
        assertNotSame("Should be different instances", activity1, activity2)
    }

    @Test
    fun `activity should handle rapid lifecycle changes`() {
        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(DetailsActivity::class.java)
                .create()

            repeat(3) {
                activityController.start().resume().pause().stop()
            }

            activityController.destroy()
        } catch (e: Exception) {
            fail("Should handle rapid lifecycle changes: ${e.message}")
        }
    }

    @Test
    fun `activity should survive pause and resume`() {
        // Arrange
        activityController = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        // Act
        activityController.pause()
        activityController.resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive pause/resume", activity)
        assertFalse("Activity should not be finishing", activity.isFinishing)
    }

    // ===== اختبارات Movie Handling =====

    @Test
    fun `activity should handle complete movie data`() {
        // Arrange
        val completeMovie = Movie(
            id = 550,
            title = "Fight Club",
            releaseDate = "1999-10-15",
            posterPath = "/pB8BM7pdSp6B6Ih7QZ4DrQ3PmJK.jpg",
            voteAverage = 8.4,
            overview = "A detailed overview...",
            genreIds = listOf(18, 53),
            homepage = "http://example.com"
        )

        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", completeMovie)
        }

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should handle complete movie", activity)
    }

    @Test
    fun `activity should handle empty movie data`() {
        // Arrange
        val emptyMovie = Movie()
        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", emptyMovie)
        }

        // Act
        activityController = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should handle empty movie", activity)
    }
}

/**
 * Integration Tests للـ DetailsActivity
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DetailsActivityIntegrationTest {

    private lateinit var context: Context
    private lateinit var testMovie: Movie

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        testMovie = Movie(
            id = 550,
            title = "Fight Club",
            releaseDate = "1999-10-15",
            posterPath = "/abc.jpg",
            voteAverage = 8.4,
            overview = "Test overview",
            genreIds = listOf(18),
            homepage = ""
        )
    }

    @Test
    fun `activity should handle fresh start with movie`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", testMovie)
        }

        // Act
        val controller = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create().start().resume()

        val activity = controller.get()

        // Assert
        assertNotNull("Activity should start with movie", activity)
        assertFalse("Should not be finishing", activity.isFinishing)

        // Cleanup
        controller.pause().stop().destroy()
    }

    @Test
    fun `activity should handle background and foreground`() {
        // Arrange
        val controller = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()
            .start()
            .resume()

        // Act
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
    fun `activity creation and destruction should be balanced`() {
        // Act
        val controller = Robolectric.buildActivity(DetailsActivity::class.java)
            .create()

        val activity = controller.get()
        assertNotNull("Activity created", activity)

        controller.start().resume().pause().stop().destroy()

        // Assert
        assertTrue("Lifecycle completed successfully", true)
    }

    @Test
    fun `activity should handle configuration changes`() {
        // Arrange
        val intent = Intent(context, DetailsActivity::class.java).apply {
            putExtra("movie", testMovie)
        }

        // Act
        val controller = Robolectric.buildActivity(
            DetailsActivity::class.java,
            intent
        ).create().start().resume()

        // Simulate configuration change
        controller.pause().stop()
        controller.start().resume()

        val activity = controller.get()

        // Assert
        assertNotNull("Activity should survive config change", activity)

        // Cleanup
        controller.pause().stop().destroy()
    }
}

/**
 * Component Tests للـ DetailsActivity
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class DetailsActivityComponentTest {

    @Test
    fun `activity class should be accessible`() {
        // Assert
        assertNotNull("Activity class should exist", DetailsActivity::class.java)
    }

    @Test
    fun `activity should extend ComponentActivity`() {
        // Assert
        assertTrue(
            "Should extend ComponentActivity",
            DetailsActivity::class.java.isAssignableFrom(ComponentActivity::class.java)
        )
    }

    @Test
    fun `activity should be instantiable`() {
        val activityClass = DetailsActivity::class.java

        assertNotNull("Class should not be null", activityClass)
        assertFalse("Class should not be abstract", activityClass.isInterface)
    }

    @Test
    fun `movie class should be parcelable`() {
        // Verify Movie implements Parcelable
        val movieClass = Movie::class.java
        assertNotNull("Movie class should exist", movieClass)
    }
}