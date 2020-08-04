package com.khs.thread

import android.os.Handler
import android.os.Message
import java.lang.Exception

class NewThread(
    val myHandler: MainActivity.Companion.MyHandler,
    var secondNum: Int
) : Thread() {

    companion object {
        const val NEWTHREAD_WHAT = 0
    }

    override fun run() {
        while (true) {
            secondNum++;
            val msg = Message.obtain()
            msg.what = 0
            msg.arg1 = secondNum
            myHandler.sendMessage(msg)
            sleep(500)
        }
    }
}