package com.khs.thread

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.khs.thread.databinding.ActivityMainBinding
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding

    companion object {
        private var mainNum: Int = 0
        private var secondNum: Int = 0
        private lateinit var mHandler: MyHandler
        class MyHandler(val activity: MainActivity) : Handler(Looper.getMainLooper()) {
            private var mWeakActivity: WeakReference<MainActivity> = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                var _activity = mWeakActivity.get()
                if (_activity != null && msg.what==0) {
                    _activity.mBinding.tvSecond.text = secondNum.toString()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mHandler = MyHandler(this)
        mBinding.apply {
            btnStart.setOnClickListener {
                startNum();
            }
        }
    }

    private fun startNum() {
        mainNum++;
        val newThread = NewThread();
        newThread.isDaemon = true
        newThread.start()
        mBinding.tvMain.text = mainNum.toString()

    }

    class NewThread() : Thread() {
        override fun run() {
            while (true) {
                secondNum++;
                try {
                    sleep(600)
                } catch (e: Exception) { }
                val msg = Message.obtain()
                msg.what = 0
                msg.arg1 = 0
                msg.arg2 = 0
                msg.obj = null
                mHandler.sendMessage(msg)
            }
        }
    }
}

