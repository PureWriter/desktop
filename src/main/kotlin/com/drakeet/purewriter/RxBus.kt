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

import com.jakewharton.rxrelay2.PublishRelay
import io.reactivex.BackpressureStrategy
import io.reactivex.Flowable
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler
import io.reactivex.schedulers.Schedulers
import kotlin.reflect.KClass

/**
 * @author drakeet
 */
object RxBus {

  private val relay = PublishRelay.create<Any>().toSerialized()

  fun post(event: Any) = relay.accept(event)

  @Suppress("MemberVisibilityCanBePrivate")
  fun <T> event(eventType: Class<T>): Flowable<T> {
    return relay.ofType(eventType).toFlowable(BackpressureStrategy.BUFFER)
  }

  fun <T : Any> event(eventType: KClass<T>): Flowable<T> {
    return event(eventType.java)
      .subscribeOn(Schedulers.io())
      .observeOn(JavaFxScheduler.platform())
  }

  fun hasObservers(): Boolean = relay.hasObservers()
}
