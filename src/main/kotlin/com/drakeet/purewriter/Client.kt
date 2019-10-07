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
import io.netty.bootstrap.Bootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.protobuf.ProtobufDecoder
import io.netty.handler.codec.protobuf.ProtobufEncoder
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender
import io.netty.util.internal.logging.InternalLoggerFactory
import io.netty.util.internal.logging.JdkLoggerFactory
import java.net.InetSocketAddress
import java.util.logging.Level
import java.util.logging.Logger

class Client {

  init {
    Logger.getLogger("io.netty").level = Level.OFF
    InternalLoggerFactory.setDefaultFactory(JdkLoggerFactory.INSTANCE)
  }

  var channel: Channel? = null

  private var group: NioEventLoopGroup? = null

  fun start(ip: String, port: Int) {
    try {
      try {
        group = NioEventLoopGroup()
        val bootstrap = Bootstrap()
          .group(group!!)
          .channel(NioSocketChannel::class.java)
          .option(ChannelOption.SO_KEEPALIVE, true)
          .remoteAddress(InetSocketAddress(ip, port))
          .handler(object : ChannelInitializer<SocketChannel>() {
            @Throws(Exception::class)
            override fun initChannel(ch: SocketChannel) {
              Log.d("Connecting...")
              with(ch.pipeline()) {
                addLast(ProtobufVarint32FrameDecoder())
                addLast(ProtobufDecoder(PureWriterProtocol.Message.getDefaultInstance()))
                addLast(ProtobufVarint32LengthFieldPrepender())
                addLast(ProtobufEncoder())
                addLast(MessageHandler())
              }
            }
          })

        channel = bootstrap.connect().sync().channel()
        Log.d("The server is successfully connected.")

        channel?.closeFuture()?.sync()
        Log.d("Connection is closed")
      } finally {
        group?.shutdownGracefully()?.sync()
      }
    } catch (e: InterruptedException) {
      e.printStackTrace()
    }
  }
}

