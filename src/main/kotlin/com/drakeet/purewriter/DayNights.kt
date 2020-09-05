package com.drakeet.purewriter

import javafx.scene.Scene

/**
 * @author Drakeet Xu
 */
lateinit var sheets: String
lateinit var sheetsNight: String

fun Scene.setDarkMode(dark: Boolean) {
  if (dark) {
    stylesheets.remove(sheets)
    stylesheets.add(sheetsNight)
  } else {
    stylesheets.remove(sheetsNight)
    stylesheets.add(sheets)
  }
}
