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

import com.drakeet.purewriter.protocol.PureWriterProtocol
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import javafx.application.Platform

class MessageHandler : SimpleChannelInboundHandler<PureWriterProtocol.Message>() {

  override fun channelRead0(context: ChannelHandlerContext, message: PureWriterProtocol.Message) {
    Platform.runLater {
      Log.d("message.messageTyp: ${message.messageType}")
      when (message.messageType) {
        "ArticleMessage" -> RxBus.post(message.content.jsonTo(ArticleMessage::class))
        "EmptyArticleMessage" -> RxBus.post(EmptyArticleMessage())
        "DisconnectMessage" -> context.disconnect()
        // No need now, we will directly call the pong()
        // "Pong" -> RxBus.post(Pong())
      }
      if (message.messageType != "DisconnectMessage") {
        PingPongDelegate.pong()
      }
    }
  }

  @Throws(Exception::class)
  override fun channelActive(context: ChannelHandlerContext) {
    Log.d("channelActive: ${context.channel().localAddress()}")
    RxBus.post(ChannelActive(true))
  }

  @Throws(Exception::class)
  override fun channelInactive(context: ChannelHandlerContext) {
    Log.d("channelInactive: ${context.channel().localAddress()}")
    RxBus.post(ChannelActive(false))
  }

  @Throws(Exception::class)
  override fun exceptionCaught(context: ChannelHandlerContext, cause: Throwable) {
    context.close()
    cause.printStackTrace()
  }
}
