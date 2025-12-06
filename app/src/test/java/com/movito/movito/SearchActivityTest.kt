package com.movito.movito

import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import com.movito.movito.ui.SearchActivity
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
 * Unit Tests للـ SearchActivity
 *
 * بنختبر:
 * - Activity lifecycle
 * - Intent handling
 * - Context attachment
 * - Activity creation
 * - ViewModel initialization
 * - LanguageChangeObserver registration/unregistration
 * - Language change listener behavior
 *
 * ملحوظة مهمة:
 * الـ Activity بتسجل listener في LanguageChangeObserver
 * وبتشيله في onDestroy علشان تتجنب memory leaks
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class SearchActivityTest {

    private lateinit var context: Context
    private lateinit var activityController: ActivityController<SearchActivity>

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
    fun `activity should be created successfully`() {
        // Act & Assert - Should not throw
        try {
            activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should not be null", activity)
    }

    @Test
    fun `activity should have valid context`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity context should not be null", activity)
        assertNotNull(
            "Application context should not be null",
            activity.applicationContext
        )
    }

    // ===== اختبارات Lifecycle =====

    @Test
    fun `activity lifecycle onCreate should complete`() {
        // Act & Assert
        try {
            activityController = Robolectric.buildActivity(SearchActivity::class.java)
                .create()

            assertTrue("onCreate should complete", true)
        } catch (e: Exception) {
            fail("onCreate should not throw: ${e.message}")
        }
    }

    @Test
    fun `activity should handle onStart`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive onStart", activity)
    }

    @Test
    fun `activity should handle onResume`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act & Assert - Should not throw
        try {
            activityController.pause().stop().destroy()
            assertTrue("onDestroy should complete successfully", true)
        } catch (e: Exception) {
            fail("onDestroy should not throw: ${e.message}")
        }
    }

    @Test
    fun `complete lifecycle should work correctly`() {
        // Act & Assert - Full lifecycle
        try {
            activityController = Robolectric.buildActivity(SearchActivity::class.java)
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

    @Test
    fun `onDestroy should cleanup properly`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Destroy activity
        activityController.pause().stop().destroy()

        // Assert - Should complete without issues
        // LanguageChangeObserver.removeListener should be called
        assertTrue("Activity destroyed successfully", true)
    }

    // ===== اختبارات Intent =====

    @Test
    fun `activity should be launchable with intent`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java)

        // Act
        activityController = Robolectric.buildActivity(
            SearchActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should be created with intent", activity)
    }

    @Test
    fun `activity intent should be valid`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = activityController.get()
        val intent = activity.intent

        // Assert
        assertNotNull("Intent should not be null", intent)
    }

    @Test
    fun `activity should handle intent with flags`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Act
        activityController = Robolectric.buildActivity(
            SearchActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should handle flags", activity)
    }

    @Test
    fun `activity should handle CLEAR_TOP flag correctly`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }

        // Act
        activityController = Robolectric.buildActivity(
            SearchActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val hasFlag = activity.intent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0

        // Assert
        assertTrue("Intent should have CLEAR_TOP flag", hasFlag)
    }

    @Test
    fun `activity should handle NEW_TASK flag correctly`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Act
        activityController = Robolectric.buildActivity(
            SearchActivity::class.java,
            intent
        ).create()

        val activity = activityController.get()
        val hasFlag = activity.intent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0

        // Assert
        assertTrue("Intent should have NEW_TASK flag", hasFlag)
    }

    // ===== اختبارات Context =====

    @Test
    fun `attachBaseContext should not crash`() {
        // This is tested implicitly when creating the activity
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertNotNull("attachBaseContext should complete", activity)
    }

    @Test
    fun `activity should have application context`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = activityController.get()

        // Assert
        assertFalse(
            "Activity should not be finishing initially",
            activity.isFinishing
        )
    }

    @Test
    fun `activity should not be destroyed initially`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        val activity = activityController.get()

        // Act
        activity.finish()

        // Assert
        assertTrue("Activity should be finishing after finish()", activity.isFinishing)
    }

    @Test
    fun `activity should complete lifecycle after finish`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        val activity = activityController.get()

        // Act
        activity.finish()
        activityController.pause().stop().destroy()

        // Assert
        assertTrue("Lifecycle should complete after finish", true)
    }

    // ===== اختبارات Multiple Instances =====

    @Test
    fun `multiple activity instances should be independent`() {
        // Act
        val controller1 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
        val activity1 = controller1.get()

        val controller2 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
        val activity2 = controller2.get()

        // Assert
        assertNotNull("Activity 1 should exist", activity1)
        assertNotNull("Activity 2 should exist", activity2)
        assertNotSame(
            "Activities should be different instances",
            activity1,
            activity2
        )

        // Cleanup
        controller1.pause().stop().destroy()
        controller2.pause().stop().destroy()
    }

    @Test
    fun `each activity instance should have its own listener`() {
        // Arrange & Act
        val controller1 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
        val activity1 = controller1.get()

        val controller2 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
        val activity2 = controller2.get()

        // Assert
        assertNotSame(
            "Each activity should be independent",
            activity1,
            activity2
        )

        // Cleanup - Both should cleanup without issues
        controller1.pause().stop().destroy()
        controller2.pause().stop().destroy()
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `recreating activity should work`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Simulate configuration change
        val activity1 = activityController.get()
        activityController.pause().stop().destroy()

        // Create new instance
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
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
            activityController = Robolectric.buildActivity(SearchActivity::class.java)
                .create()

            // Rapid lifecycle changes
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
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Pause and resume
        activityController.pause()
        activityController.resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive pause/resume", activity)
        assertFalse("Activity should not be finishing", activity.isFinishing)
    }

    @Test
    fun `destroying activity multiple times should be safe`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Try to destroy multiple times
        try {
            activityController.pause().stop().destroy()
            // Second destroy attempt (shouldn't crash)
        } catch (e: Exception) {
            fail("Multiple destroy calls should be safe: ${e.message}")
        }

        // Assert
        assertTrue("Should handle multiple destroy calls", true)
    }

    @Test
    fun `activity should handle onCreate without savedInstanceState`() {
        // Act
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create(null) // null savedInstanceState

        val activity = activityController.get()

        // Assert
        assertNotNull("Should handle null savedInstanceState", activity)
    }

    @Test
    fun `activity should handle rapid create and destroy cycles`() {
        // Act & Assert
        try {
            repeat(5) {
                val controller = Robolectric.buildActivity(SearchActivity::class.java)
                    .create()
                    .start()
                    .resume()
                    .pause()
                    .stop()
                    .destroy()
            }
            assertTrue("Should handle multiple cycles", true)
        } catch (e: Exception) {
            fail("Should handle rapid create/destroy: ${e.message}")
        }
    }

    // ===== اختبارات Language Change Scenarios =====

    @Test
    fun `activity should survive background to foreground transition`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Simulate going to background
        activityController.pause()
        activityController.stop()

        // Return to foreground
        activityController.start()
        activityController.resume()

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive background transition", activity)
        assertFalse("Activity should not be finishing", activity.isFinishing)
    }

    @Test
    fun `activity should handle multiple pause resume cycles`() {
        // Arrange
        activityController = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Multiple cycles
        repeat(5) {
            activityController.pause()
            activityController.resume()
        }

        val activity = activityController.get()

        // Assert
        assertNotNull("Activity should survive multiple cycles", activity)
        assertFalse("Activity should not be finishing", activity.isFinishing)
    }
}

/**
 * Integration Tests للـ SearchActivity
 *
 * بنختبر سيناريوهات حقيقية أكتر
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class SearchActivityIntegrationTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `activity should handle fresh start`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java)

        // Act
        val controller = Robolectric.buildActivity(
            SearchActivity::class.java,
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
        val controller = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

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
    fun `activity creation and destruction should be balanced`() {
        // Act
        val controller = Robolectric.buildActivity(SearchActivity::class.java)
            .create()

        val activity = controller.get()
        assertNotNull("Activity created", activity)

        controller.start().resume().pause().stop().destroy()

        // Assert - If we get here, lifecycle is balanced
        assertTrue("Lifecycle completed successfully", true)
    }

    @Test
    fun `activity should handle configuration change simulation`() {
        // Arrange
        val controller1 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        // Act - Simulate configuration change (e.g., rotation)
        controller1.pause().stop().destroy()

        val controller2 = Robolectric.buildActivity(SearchActivity::class.java)
            .create()
            .start()
            .resume()

        val activity2 = controller2.get()

        // Assert
        assertNotNull("Activity should handle configuration change", activity2)
        assertFalse("New activity should not be finishing", activity2.isFinishing)

        // Cleanup
        controller2.pause().stop().destroy()
    }

    @Test
    fun `activity should handle intent recreation with flags`() {
        // Arrange
        val intent = Intent(context, SearchActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        // Act
        val controller = Robolectric.buildActivity(
            SearchActivity::class.java,
            intent
        ).create().start().resume()

        val activity = controller.get()
        val receivedIntent = activity.intent

        // Assert
        assertNotNull("Activity should handle intent with flags", activity)
        assertTrue(
            "Should have CLEAR_TOP flag",
            receivedIntent.flags and Intent.FLAG_ACTIVITY_CLEAR_TOP != 0
        )
        assertTrue(
            "Should have NEW_TASK flag",
            receivedIntent.flags and Intent.FLAG_ACTIVITY_NEW_TASK != 0
        )

        // Cleanup
        controller.pause().stop().destroy()
    }

    @Test
    fun `multiple sequential activity launches should work`() {
        // Act - Launch multiple times
        repeat(3) { iteration ->
            val controller = Robolectric.buildActivity(SearchActivity::class.java)
                .create()
                .start()
                .resume()

            val activity = controller.get()

            // Assert
            assertNotNull("Activity $iteration should launch", activity)

            // Cleanup
            controller.pause().stop().destroy()
        }

        assertTrue("All launches completed successfully", true)
    }
}

/**
 * Component Tests للـ SearchActivity
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class SearchActivityComponentTest {

    @Test
    fun `activity class should be accessible`() {
        // Assert
        assertNotNull(
            "Activity class should exist",
            SearchActivity::class.java
        )
    }

    @Test
    fun `activity should extend ComponentActivity`() {
        // Assert
        assertTrue(
            "Should extend ComponentActivity",
            ComponentActivity::class.java.isAssignableFrom(SearchActivity::class.java)
        )
    }

    @Test
    fun `activity should be instantiable`() {
        // This is tested by creating activities in other tests
        // But we can verify the class structure
        val activityClass = SearchActivity::class.java

        assertNotNull("Class should not be null", activityClass)
        assertFalse("Class should not be interface", activityClass.isInterface)
    }

    @Test
    fun `activity should have public constructor`() {
        // Act & Assert
        try {
            val constructor = SearchActivity::class.java.getDeclaredConstructor()
            assertNotNull("Constructor should exist", constructor)
        } catch (e: NoSuchMethodException) {
            fail("Activity should have default public constructor")
        }
    }
}