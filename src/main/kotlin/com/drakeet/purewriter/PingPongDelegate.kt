package com.drakeet.purewriter

import io.netty.channel.Channel
import io.reactivex.Observable
import io.reactivex.disposables.Disposable
import java.util.concurrent.TimeUnit


/**
 * @author Drakeet Xu
 */
object PingPongDelegate {

  var isJustDisconnected: Boolean = false
  private var timer: Disposable? = null

  @Volatile
  private var pinging = false

  init {
    // RxBus.event(Pong::class).subscribe { pong() }
  }

  fun ping(channel: Channel) {
    if (pinging) return
    isJustDisconnected = false
    pinging = true
    Log.d("Pinging...")
    channel.writeAndFlush(newJsonMessage(type = "Ping"))
    timer?.dispose()
    timer = Observable.timer(3, TimeUnit.SECONDS)
      .subscribe {
        Log.d("pinging: $pinging")
        if (pinging) disconnect(channel)
      }
  }

  fun pong() {
    timer?.dispose()
    timer = null
    pinging = false
  }

  private fun disconnect(channel: Channel) {
    isJustDisconnected = true
    channel.disconnect()
    RxBus.post(ChannelActive(false))
    pinging = false
  }
}
