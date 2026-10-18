package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.PersonaType
import com.example.model.UserSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Maya AI", appName)
  }

  @Test
  fun `verify default user settings and persona`() {
    val settings = UserSettings()
    assertEquals("Maya", settings.assistantName)
    assertEquals(PersonaType.MAYA, settings.persona)
    assertNotNull(settings.userName)
  }
}
