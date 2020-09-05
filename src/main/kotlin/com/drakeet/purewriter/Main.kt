/*
 * Copyright (C) 2019 Drakeet Xu <drakeet@drakeet.com>
 *
 * This file is part of Pure Writer Desktop
 *
 * Pure Writer Desktop is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * rebase-server is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 *
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with rebase-server. If not, see <http://www.gnu.org/licenses/>.
 */

package com.drakeet.purewriter

import javafx.application.Application
import javafx.application.Platform
import javafx.fxml.FXMLLoader
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.stage.Stage
import kotlin.system.exitProcess


class Main : Application() {

  override fun start(stage: Stage) {
    mainStage = stage
    val root = FXMLLoader.load<Parent>(javaClass.getResource("editor.fxml"))
    stage.title = stageTitle
    stage.scene = Scene(root, 968.0, 615.0).apply {
      sheets = Main::class.java.getResource("editor.css").toExternalForm()
      sheetsNight = Main::class.java.getResource("editor-night.css").toExternalForm()
      setDarkMode(Settings.darkMode)
    }
    stage.show()
    stage.setOnCloseRequest {
      Platform.exit()
      exitProcess(0)
    }
  }

  companion object {
    @JvmStatic
    fun main(args: Array<String>) {
      launch(Main::class.java, *args)
    }
  }
}
