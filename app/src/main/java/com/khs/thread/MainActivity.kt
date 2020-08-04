package com.khs.thread

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.khs.thread.databinding.ActivityMainBinding
import java.lang.ref.WeakReference

class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding
    lateinit var instrumentSound:InstrumentSound
    lateinit var newAsyncTask: ExampleAsyncTask

    var listener: View.OnClickListener = object : View.OnClickListener {
        var msg: Message? = null
        override fun onClick(v: View) {
            when (v.id) {
                R.id.btn_piano -> {
                    msg = Message.obtain()
                    msg?.what = InstrumentSound.SOUND_PIANO
                }
                R.id.btn_guitar -> {
                    msg = Message.obtain()
                    msg?.what = InstrumentSound.SOUND_GUITAR
                }
                R.id.btn_drum -> {
                    msg = Message.obtain()
                    msg?.what = InstrumentSound.SOUND_DRUM
                }
                R.id.btn_asynctask ->{
                    newAsyncTask = ExampleAsyncTask(this@MainActivity,mBinding.tvSound)
                    newAsyncTask.execute(100,50) // doinBackground param
                }
            }
            msg?.let {
                instrumentSound.handler?.sendMessage(it)
            }
        }
    }
    companion object {
        private var mainNum: Int = 0
        private var secondNum: Int = 0
        private lateinit var mHandler: MyHandler

        class MyHandler(val activity: MainActivity) : Handler(Looper.getMainLooper()) {
            private var mWeakActivity: WeakReference<MainActivity> = WeakReference(activity)
            override fun handleMessage(msg: Message) {
                super.handleMessage(msg)
                var _activity = mWeakActivity.get()
                if (_activity != null) {
                    var str = ""
                    when (msg.what) {
                        InstrumentSound.SOUND_PIANO -> {
                            str = msg.obj as String
                        }
                        InstrumentSound.SOUND_GUITAR -> {
                            str = msg.obj as String
                        }
                        InstrumentSound.SOUND_DRUM -> {
                            str = msg.obj as String
                        }
                    }
                    _activity.mBinding.tvSound.text = str
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
            btnStart.setOnClickListener(listener)
            btnPiano.setOnClickListener(listener)
            btnGuitar.setOnClickListener(listener)
            btnDrum.setOnClickListener(listener)
            btnAsynctask.setOnClickListener(listener)
        }
        instrumentSound = InstrumentSound(mHandler)
        instrumentSound.isDaemon = true
        instrumentSound.start()
    }
}

