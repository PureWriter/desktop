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
import io.netty.channel.Channel
import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers

/**
 * @author Drakeet Xu
 */
fun Channel.sendArticle(title: String, content: String, start: Int, end: Int) {
  Completable.fromAction {
    writeAndFlush(
      newJsonMessage(id = 1, type = "ArticleMessage", any = ArticleMessage(title, content, start, end))
    )
  }
    .subscribeOn(Schedulers.single())
    .subscribe()
}

fun newJsonMessage(id: Int = 0, type: String, any: Any = ""): PureWriterProtocol.Message {
  return PureWriterProtocol.Message.newBuilder()
    .setId(id)
    .setVersion(2)
    .setContentType("JSON")
    .setMessageType(type)
    .setContent(any.toJson())
    .setTime(System.currentTimeMillis())
    .build()
}
