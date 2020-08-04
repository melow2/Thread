package com.khs.thread

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import android.os.Message

class InstrumentSound(private val mHandler: Handler) : Thread() {
    var handler: Handler? = null

    companion object {
        const val SOUND_PIANO = 0
        const val SOUND_GUITAR = 1
        const val SOUND_DRUM = 2
    }

    @SuppressLint("HandlerLeak")
    override fun run() {
        Looper.prepare()  // 루퍼를 스레드에 달아놓음.
        handler = object : Handler() {
            override fun handleMessage(msg: Message) {
                val msgForMain = Message.obtain()
                when (msg.what) {
                    SOUND_PIANO -> {
                        try {
                            sleep(3000)
                        } catch (e: Exception) {
                        }
                        msgForMain.what = SOUND_PIANO
                        msgForMain.obj = "피아노 소리"
                    }
                    SOUND_GUITAR -> {
                        try {
                            sleep(4000)
                        } catch (e: Exception) {
                        }
                        msgForMain.what = SOUND_GUITAR
                        msgForMain.obj = "기타 소리"
                    }
                    SOUND_DRUM -> {
                        try {
                            sleep(5000)
                        } catch (e: Exception) {
                        }
                        msgForMain.what = SOUND_DRUM
                        msgForMain.obj = "드럼 소리"
                    }
                }
                mHandler.sendMessage(msgForMain)
            }
        }
        Looper.loop()
    }
}