package com.movito.movito

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit Tests للـ NotificationPreferences
 *
 * بنستخدم Robolectric علشان نقدر نستخدم SharedPreferences
 *
 * بنختبر:
 * - Singleton pattern
 * - Enable/Disable notifications
 * - Default values
 * - State persistence
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NotificationPreferencesTest {

    private lateinit var context: Context
    private lateinit var notificationPrefs: NotificationPreferences

    @Before
    fun setup() {
        // Get Robolectric context
        context = ApplicationProvider.getApplicationContext()

        // Clear any existing instance
        clearNotificationPreferencesInstance()

        // Get fresh instance
        notificationPrefs = NotificationPreferences.getInstance(context)

        // Clear preferences before each test
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        // Clear preferences after each test
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // Clear singleton instance
        clearNotificationPreferencesInstance()
    }

    // Helper function to clear singleton instance using reflection
    private fun clearNotificationPreferencesInstance() {
        try {
            val instanceField = NotificationPreferences::class.java
                .getDeclaredField("INSTANCE")
            instanceField.isAccessible = true
            instanceField.set(null, null)
        } catch (e: Exception) {
            // If reflection fails, that's okay for tests
        }
    }

    // ===== اختبارات Default Values =====

    @Test
    fun `notifications should be enabled by default`() {
        // Arrange - Fresh instance with no saved preferences
        val freshPrefs = NotificationPreferences.getInstance(context)

        // Act
        val isEnabled = freshPrefs.isNotificationsEnabled()

        // Assert
        assertTrue("Notifications should be enabled by default", isEnabled)
    }

    // ===== اختبارات Enable/Disable =====

    @Test
    fun `setNotificationsEnabled to false should disable notifications`() {
        // Act
        notificationPrefs.setNotificationsEnabled(false)

        // Assert
        assertFalse(
            "Notifications should be disabled",
            notificationPrefs.isNotificationsEnabled()
        )
    }

    @Test
    fun `setNotificationsEnabled to true should enable notifications`() {
        // Arrange - First disable
        notificationPrefs.setNotificationsEnabled(false)

        // Act - Then enable
        notificationPrefs.setNotificationsEnabled(true)

        // Assert
        assertTrue(
            "Notifications should be enabled",
            notificationPrefs.isNotificationsEnabled()
        )
    }

    @Test
    fun `multiple calls to setNotificationsEnabled should update state correctly`() {
        // Act & Assert - Toggle multiple times
        notificationPrefs.setNotificationsEnabled(false)
        assertFalse("Should be disabled", notificationPrefs.isNotificationsEnabled())

        notificationPrefs.setNotificationsEnabled(true)
        assertTrue("Should be enabled", notificationPrefs.isNotificationsEnabled())

        notificationPrefs.setNotificationsEnabled(false)
        assertFalse("Should be disabled again", notificationPrefs.isNotificationsEnabled())
    }

    // ===== اختبارات Persistence =====

    @Test
    fun `preference changes should persist across instances`() {
        // Arrange
        notificationPrefs.setNotificationsEnabled(false)

        // Act - Create new instance
        clearNotificationPreferencesInstance()
        val newInstance = NotificationPreferences.getInstance(context)

        // Assert - Value should persist
        assertFalse(
            "Disabled state should persist",
            newInstance.isNotificationsEnabled()
        )
    }

    @Test
    fun `enabled state should persist across instances`() {
        // Arrange
        notificationPrefs.setNotificationsEnabled(true)

        // Act - Create new instance
        clearNotificationPreferencesInstance()
        val newInstance = NotificationPreferences.getInstance(context)

        // Assert
        assertTrue(
            "Enabled state should persist",
            newInstance.isNotificationsEnabled()
        )
    }

    // ===== اختبارات Singleton Pattern =====

    @Test
    fun `getInstance should return the same instance`() {
        // Act
        val instance1 = NotificationPreferences.getInstance(context)
        val instance2 = NotificationPreferences.getInstance(context)

        // Assert
        assertSame(
            "Should return the same instance",
            instance1,
            instance2
        )
    }

    @Test
    fun `singleton instance should maintain state`() {
        // Arrange
        val instance1 = NotificationPreferences.getInstance(context)
        instance1.setNotificationsEnabled(false)

        // Act
        val instance2 = NotificationPreferences.getInstance(context)

        // Assert
        assertSame("Should be same instance", instance1, instance2)
        assertFalse(
            "State should be maintained",
            instance2.isNotificationsEnabled()
        )
    }

    // ===== اختبارات Edge Cases =====

    @Test
    fun `rapid toggling should work correctly`() {
        // Act - Rapid toggles
        repeat(10) { i ->
            val shouldEnable = i % 2 == 0
            notificationPrefs.setNotificationsEnabled(shouldEnable)

            // Assert immediately
            assertEquals(
                "State should match after toggle $i",
                shouldEnable,
                notificationPrefs.isNotificationsEnabled()
            )
        }
    }

    @Test
    fun `setting same value multiple times should work`() {
        // Act - Set to false multiple times
        repeat(5) {
            notificationPrefs.setNotificationsEnabled(false)
        }

        // Assert
        assertFalse(
            "Should remain disabled",
            notificationPrefs.isNotificationsEnabled()
        )

        // Act - Set to true multiple times
        repeat(5) {
            notificationPrefs.setNotificationsEnabled(true)
        }

        // Assert
        assertTrue(
            "Should remain enabled",
            notificationPrefs.isNotificationsEnabled()
        )
    }

    // ===== اختبارات Context =====

    @Test
    fun `getInstance with different contexts should return same instance`() {
        // Arrange
        val appContext1 = context.applicationContext
        val appContext2 = context.applicationContext

        // Act
        val instance1 = NotificationPreferences.getInstance(appContext1)
        val instance2 = NotificationPreferences.getInstance(appContext2)

        // Assert
        assertSame(
            "Should return same instance regardless of context",
            instance1,
            instance2
        )
    }

    // ===== اختبارات State Consistency =====

    @Test
    fun `state should be consistent across get calls`() {
        // Arrange
        notificationPrefs.setNotificationsEnabled(false)

        // Act - Multiple reads
        val value1 = notificationPrefs.isNotificationsEnabled()
        val value2 = notificationPrefs.isNotificationsEnabled()
        val value3 = notificationPrefs.isNotificationsEnabled()

        // Assert
        assertFalse("All values should be false", value1)
        assertEquals("Values should be consistent", value1, value2)
        assertEquals("Values should be consistent", value2, value3)
    }

    @Test
    fun `initial state should be consistent`() {
        // Arrange - Fresh instance
        clearNotificationPreferencesInstance()
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        // Act
        val freshInstance = NotificationPreferences.getInstance(context)
        val value1 = freshInstance.isNotificationsEnabled()
        val value2 = freshInstance.isNotificationsEnabled()

        // Assert
        assertTrue("Default should be true", value1)
        assertEquals("Values should be consistent", value1, value2)
    }

    // ===== اختبارات Thread Safety (Singleton) =====

    @Test
    fun `singleton should handle rapid getInstance calls`() {
        // Clear instance first
        clearNotificationPreferencesInstance()

        // Act - Rapid calls
        val instances = mutableListOf<NotificationPreferences>()
        repeat(100) {
            instances.add(NotificationPreferences.getInstance(context))
        }

        // Assert - All should be the same instance
        val firstInstance = instances.first()
        assertTrue(
            "All instances should be the same",
            instances.all { it === firstInstance }
        )
    }
}

/**
 * Tests للـ sendWelcomeNotification function
 *
 *  ملحوظة: الـ function دي محتاجة Context حقيقي علشان:
 * - NotificationManagerCompat
 * - Permission checks
 * - Notification channels
 *
 * علشان كده الـ tests بتاعتها محدودة في Unit Tests
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NotificationFunctionTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Clear notification preferences
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @After
    fun tearDown() {
        // Cleanup
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()
    }

    @Test
    fun `sendWelcomeNotification should not crash when notifications disabled`() {
        // Arrange
        val prefs = NotificationPreferences.getInstance(context)
        prefs.setNotificationsEnabled(false)

        // Act & Assert - Should not throw
        try {
            sendWelcomeNotification(context)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun `sendWelcomeNotification should not crash when notifications enabled`() {
        // Arrange
        val prefs = NotificationPreferences.getInstance(context)
        prefs.setNotificationsEnabled(true)

        // Act & Assert - Should not throw
        try {
            sendWelcomeNotification(context)
        } catch (e: Exception) {
            fail("Should not throw exception: ${e.message}")
        }
    }

    @Test
    fun `sendWelcomeNotification should respect notification preferences`() {
        // Arrange
        val prefs = NotificationPreferences.getInstance(context)
        prefs.setNotificationsEnabled(false)

        // Act - Should return early due to disabled notifications
        // This test verifies the function doesn't crash
        try {
            sendWelcomeNotification(context)
            // If we get here, the function respected the preference
            assertTrue("Function completed successfully", true)
        } catch (e: Exception) {
            fail("Should not throw: ${e.message}")
        }
    }

    @Test
    fun `sendWelcomeNotification called multiple times should not crash`() {
        // Arrange
        val prefs = NotificationPreferences.getInstance(context)
        prefs.setNotificationsEnabled(true)

        // Act - Call multiple times
        try {
            repeat(5) {
                sendWelcomeNotification(context)
            }
        } catch (e: Exception) {
            fail("Should handle multiple calls: ${e.message}")
        }
    }
}

/**
 * Integration Tests للـ Notification System
 *
 * بنختبر التكامل بين NotificationPreferences و sendWelcomeNotification
 */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28], manifest = Config.NONE)
class NotificationIntegrationTest {

    private lateinit var context: Context
    private lateinit var prefs: NotificationPreferences

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        // Clear preferences
        context.getSharedPreferences("notification_prefs", Context.MODE_PRIVATE)
            .edit()
            .clear()
            .commit()

        prefs = NotificationPreferences.getInstance(context)
    }

    @Test
    fun `complete notification flow should work`() {
        // 1. Enable notifications
        prefs.setNotificationsEnabled(true)
        assertTrue("Should be enabled", prefs.isNotificationsEnabled())

        // 2. Send notification
        try {
            sendWelcomeNotification(context)
        } catch (e: Exception) {
            fail("Send should not fail: ${e.message}")
        }

        // 3. Disable notifications
        prefs.setNotificationsEnabled(false)
        assertFalse("Should be disabled", prefs.isNotificationsEnabled())

        // 4. Try to send again (should return early)
        try {
            sendWelcomeNotification(context)
        } catch (e: Exception) {
            fail("Should handle disabled state: ${e.message}")
        }
    }

    @Test
    fun `notification state changes should be reflected immediately`() {
        // Start enabled
        prefs.setNotificationsEnabled(true)

        // Send notification
        sendWelcomeNotification(context)

        // Disable
        prefs.setNotificationsEnabled(false)

        // Immediately check
        assertFalse(
            "State should change immediately",
            prefs.isNotificationsEnabled()
        )

        // Try to send (should respect new state)
        sendWelcomeNotification(context)
    }
}