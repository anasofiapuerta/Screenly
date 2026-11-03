package com.example

import com.example.ui.components.NavDestination
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ExampleUnitTest {
  @Test
  fun addition_isCorrect() {
    assertEquals(4, 2 + 2)
  }

  @Test
  fun testNavDestinationItemsAreNotNull() {
    val items = NavDestination.items
    assertEquals(4, items.size)
    items.forEach { destination ->
      assertNotNull("Destination must not be null", destination)
      assertNotNull("Route must not be null", destination.route)
      assertNotNull("Title must not be null", destination.title)
    }
    assertEquals("dashboard", NavDestination.Dashboard.route)
    assertEquals("usage", NavDestination.Usage.route)
    assertEquals("limits", NavDestination.Limits.route)
    assertEquals("stats", NavDestination.Stats.route)
  }
}
