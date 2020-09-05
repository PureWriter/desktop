package com.drakeet.purewriter

import java.util.prefs.Preferences

/**
 * @author Drakeet Xu
 */
object Settings {

  private val settings = Preferences.userNodeForPackage(Settings::class.java)

  var darkMode: Boolean
    set(value) = settings.putBoolean("darkMode", value)
    get() = settings.getBoolean("darkMode", false)

  var ip: String
    set(value) = settings.put("ip", value)
    get() = settings.get("ip", "")
}
