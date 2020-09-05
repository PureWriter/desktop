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

import com.github.houbb.opencc4j.util.ZhConverterUtil
import javafx.stage.Stage
import java.util.*

/**
 * @author Drakeet Xu
 */
lateinit var mainStage: Stage

var isSyncingTitle: Boolean = false
var isSyncingContent: Boolean = false
var isSyncingSelection: Boolean = false

fun beginSyncing() {
  isSyncingTitle = true
  isSyncingContent = true
  isSyncingSelection = true
}

val isSyncing: Boolean
  get() {
    return isSyncingTitle || isSyncingContent || isSyncingSelection
  }

val isZh get() = Locale.getDefault().language == Locale.CHINA.language

val isTraditionalChinese
  get() = with(Locale.getDefault()) {
    this == Locale.TRADITIONAL_CHINESE || this == Locale("zh", "HK")
  }

fun String.toTraditionalIfNeeded(): String {
  if (!isZh) return this
  return if (isTraditionalChinese) ZhConverterUtil.toTraditional(this) else this
}

val stageTitle
  get() = when {
    isZh -> "纯纯写作".toTraditionalIfNeeded()
    else -> "Pure Writer"
  }
