package com.example

import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.dp
import com.example.data.DailyAggregate
import com.example.data.DailyCountEntry
import com.example.data.TrackerEntity
import com.example.data.TrackerSummary
import com.example.ui.components.TrackerCard
import com.example.ui.theme.MyApplicationTheme
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  @Test
  fun greeting_screenshot() {
    val sampleTracker = TrackerEntity(id = 1, name = "Name-1", colorHex = 0xFF4F46E5)
    val sampleSummary = TrackerSummary(
      tracker = sampleTracker,
      cumulativeTotal = 500,
      todayTotal = 100,
      yesterdayTotal = 300,
      dailyAggregates = listOf(
        DailyAggregate("2026-08-25", 100, emptyList()),
        DailyAggregate("2026-08-26", 300, emptyList()),
        DailyAggregate("2026-08-27", 100, emptyList())
      ),
      allEntries = emptyList()
    )

    composeTestRule.setContent {
      MyApplicationTheme {
        TrackerCard(
          summary = sampleSummary,
          onQuickAdd = {},
          onOpenAddCount = {},
          onOpenDetails = {},
          onEditTracker = {},
          onDeleteTracker = {},
          onClearHistory = {},
          modifier = Modifier.padding(16.dp)
        )
      }
    }

    composeTestRule.onRoot().captureRoboImage(filePath = "src/test/screenshots/greeting.png")
  }
}

