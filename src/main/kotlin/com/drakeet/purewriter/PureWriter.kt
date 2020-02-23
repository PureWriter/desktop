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

import io.netty.channel.Channel
import io.reactivex.Completable
import io.reactivex.disposables.Disposable
import io.reactivex.rxjavafx.observables.JavaFxObservable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler.platform
import io.reactivex.schedulers.Schedulers
import io.reactivex.schedulers.Schedulers.computation
import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.fxml.FXML
import javafx.fxml.Initializable
import javafx.scene.control.IndexRange
import javafx.scene.control.Label
import javafx.scene.control.TextArea
import javafx.scene.control.TextField
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.util.Duration
import java.net.URL
import java.util.*


class PureWriter : Initializable {

  private val version = "0.1.8"

  @FXML
  private lateinit var emptyView: Label
  @FXML
  private lateinit var ipLayout: VBox
  @FXML
  private lateinit var ipLabelCN: Label
  @FXML
  private lateinit var ipLabelEN: Label
  @FXML
  private lateinit var ipView: TextField
  @FXML
  private lateinit var contentView: TextArea

  private val client = Client()

  private val channel: Channel? get() = client.channel

  override fun initialize(location: URL?, resources: ResourceBundle?) {
    contentView.font = Font.loadFont(javaClass.getResourceAsStream("SourceHanSansCN-Light.ttf"), 16.toDouble())
    ipLabelCN.font = Font.loadFont(javaClass.getResourceAsStream("SourceHanSansCN-Light.ttf"), 18.toDouble())
    ipLabelEN.font = ipLabelCN.font
    emptyView.font = ipLabelCN.font
    ipView.font = ipLabelCN.font
    showIpViews()

    RxBus.event(ChannelActive::class)
      .observeOn(platform())
      .subscribe {
        if (it.active) {
          // hideIP()
          // Wait for the article
        } else {
          showIpViews()
        }
      }

    RxBus.event(ArticleMessage::class)
      .subscribe {
        beginSyncing()
        mainStage.title = if (it.title.isNotEmpty()) it.title else "Untitled"
        contentView.replaceText(it.content)
        contentView.selectRange(it.selectionStart, it.selectionEnd)
        isSyncingSelection = false
        isSyncingTitle = false
        hideIP()
        hideEmpty()
      }

    RxBus.event(EmptyArticle::class)
      .subscribe {
        showEmpty()
        hideIP()
      }

    contentView.textProperty().addListener { _, _, newValue ->
      Log.d("newText: $newValue")
      if (channel?.isActive != true) {
        showIpViews()
        return@addListener
      }
      if (!isSyncing) {
        invalidate(content = newValue)
      }
      isSyncingContent = false
    }
    contentView.selectionProperty().addListener { _, _, newValue ->
      if (channel?.isActive != true) {
        showIpViews()
        return@addListener
      }
      if (!isSyncing) {
        invalidate(selection = newValue)
      }
      isSyncingSelection = false
    }

    JavaFxObservable.interval(Duration.seconds(1.0))
      .subscribeOn(computation())
      .observeOn(platform())
      .subscribe {
        if (channel?.isActive != true) {
          showIpViews()
        } else {
          PingPongDelegate.ping(channel!!)
        }
      }
  }

  private fun showEmpty() {
    emptyView.text = """
      中文：
      未打开任何文章
      请在纯纯写作手机版中打开一篇文章
      
      English:
      No article selected
      Please open an article on Pure Writer mobile
      
      
      Pure Writer Desktop v$version
    """.trimIndent()
    emptyView.isVisible = true
    isSyncingContent = true
    contentView.replaceText("")
    isSyncingTitle = true
    mainStage.title = "Pure Writer"
  }

  private fun hideEmpty() {
    if (emptyView.isVisible) {
      emptyView.isVisible = false
    }
  }

  private var isIpViewsInitialized = false

  private fun showIpViews() {
    if (ipLayout.isVisible && isIpViewsInitialized) {
      if (ipView.text.isNotBlank() && !PingPongDelegate.isJustDisconnected) {
        ipObserver.changed(null, ipView.text, ipView.text)
      } else {
        ipView.text = ""
        PingPongDelegate.isJustDisconnected = false
      }
      return
    }
    ipLabelCN.text = """
      中文：
      未连接或与手机断开
      请打开纯纯写作 Android 版并点击其顶部的云图标获得 IP 地址填于下方
      一旦输入正确 IP，它将自动连接
      注意：当前桌面版只支持与纯纯写作 【v14.7.2】 或以上版本搭配使用
    """.trimIndent()

    ipLabelEN.text = """
      English:
      Unconnected or disconnected
      Please open Pure Writer for Android and click on its top cloud icon 
      to get an IP address into the above input field
      Once the correct IP is entered, it will be auto connected      
      Note: The current Desktop version only works with Pure Writer 【v14.7.2】 or above
      
      Pure Writer Desktop v$version  ⇋  Pure Writer 14.7.2+
    """.trimIndent()

    ipLayout.isVisible = true
    isSyncingTitle = true
    mainStage.title = "Pure Writer"
    isSyncingTitle = false
    isSyncingContent = true
    contentView.replaceText("")
    Platform.runLater { ipView.requestFocus() }
    ipView.textProperty().removeListener(ipObserver)
    ipView.textProperty().addListener(ipObserver)
    isIpViewsInitialized = true
  }

  private val ipObserver = ChangeListener<String> { _, _, newValue ->
    val ip = newValue.trim()
    Log.d("Input IP: $ip")
    if (ip.matches("((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})(\\.((2(5[0-5]|[0-4]\\d))|[0-1]?\\d{1,2})){3}".toRegex())) {
      startNettyClient(ip)
    }
  }

  private fun hideIP() {
    ipView.textProperty().removeListener(ipObserver)
    ipLayout.isVisible = false
    isIpViewsInitialized = false
  }

  private fun invalidate(
    title: String = mainStage.title,
    content: String = contentView.text,
    selection: IndexRange = contentView.selection
  ) {
    Log.d("invalidate: $title, $content, $selection")
    channel?.sendArticle(
      title = title,
      content = content,
      start = selection.start,
      end = selection.end
    )
  }

  private var clientDisposable: Disposable? = null

  private fun startNettyClient(ip: String) {
    if (ip.isBlank()) return
    clientDisposable?.dispose()
    clientDisposable = Completable.fromAction { client.start(ip, 19621) }
      .subscribeOn(Schedulers.newThread())
      .subscribe({}, { it.printStackTrace() })
  }
}
