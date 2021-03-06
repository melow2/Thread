# Thread & Handler & AsyncTask
CPU는 연산을 하고 Memory는 연산을 할 수 있게 cpu로 옮겨준다. CPU가 동시간대 여러 작업을 하는 것을 MutiThread라고 한다. 
하지만 실제로 동시간대 작업을 하는 것이 아니며, switching 하는 시간이 빨라서 보통 동 시간대로 작업을 한다고 인식한다. 
주로 메인 스레드에서 시간이 길어지는 작업을 thread로 처리한다.

#
## 가장 간단한 구현 방법.
**1. Thread 객체의 run 메서드 구현.**
```
NewThread newThread = new NewThread();
newThread.setDaemon(true);              // 응용프로그램 종료 시, 함께 종료.
newThread.start();                      // run() 메소드 호출
class NewThread extends Thread {
    @Override
    public void run(){
            ....
    }
}
```
**2. Runnable 객체의 run 메서드 구현.**
```
SecondRunnable runnable = new SecondRunnable();
Thread newThread = new Thread(runnable);
newThread.setDaemon(true);
newThread.start();
class SecondRunnable implement Runnable {
    @Override
    public void run(){
            ....
    }
}
```
#
# Handler
스레드 간에 통신 매체이며,스레드에서 작업한 결과물로 메인스레드와의 통신이 필요한 경우 사용한다.
Handler의 경우에는 non-static 클래스이므로 외부 클래스의 레퍼런스를 가지고 있다.
따라서 앱이 종료되더라도 GC의 대상이 되지 않을 수 있으므로, 메모리 릭이 발생할 위험이 있다.
즉 Activity가 종료되더라도 GC가 되지 않고, Message가 Message queue에 남아 memory leak의 원인이 된다는 것이다.
**메모리 릭을 피하기 위한 Handler 코드는 아래와 같다.**

#
## WeakHandler
메인 스레드(UI 스레드)와 서브 스레드간에 메세지를 주고받는 예제.
서브 스레드에서는 view reference를 직접 참조할 수 없다.그래서 Handler를 통해
UI Thread로 신호(message)를 보내 통신할 수 있다. 
* MainActivity
```
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
                if (_activity != null && msg.what==NewThread.NEWTHREAD_WHAT) {
                    _activity.mBinding.tvSecond.text = msg.arg1.toString()
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
        val newThread = NewThread(mHandler, secondNum);
        newThread.isDaemon = true
        newThread.start()
        mBinding.tvMain.text = mainNum.toString()
    }
}
```
* NewThread
```
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
```
#
## runOnUiThread 
Handler를 통하지 않고, UI 스레드의 작업을 진행 할 수 있다.
```
runOnUiThread{
// UI 스레드 작업.
}
```

#
# Looper
메세지(Message)를 핸들러한테 전달해 주는 기계.
메인 스레드에서는 자동적으로 루퍼가 달려있다.
새로운 스레드에서는 Looper가 없기때문에 직접 만들어서 달아줘야한다.
백그라운드에서 메인스레드로 메세지를 보내는 경우는 대부분이지만,
메인스레드에서 백그라운드로 메세지를 보내는 경우는 드물다.
**아래 예제는 Looper를 활용해 스레드 간 양방향 통신을 구현한 예제이다.**
## Implementation
* MainActivity
```
class MainActivity : AppCompatActivity() {

    lateinit var mBinding: ActivityMainBinding
    lateinit var instrumentSound:InstrumentSound

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
        }
        instrumentSound = InstrumentSound(mHandler)
        instrumentSound.isDaemon = true
        instrumentSound.start()
    }
}
```
* InstrumentSound Thread
```
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
```
#
## ANR & 메시지 큐를 이용한 작업시간 조정.
핸들러는 큐에 쌓여있는 순서대로 작업을 진행한다.
이러한 일반적인 작업 스케쥴 이외 작업순서를 변경할 수도 있다.
* ANR (Application Not Responding)
    * '애플리케이션이 응답하지 않는다.'
    * 이 에러의 원인은 Main Thread(UI Thread)가 일정 시간(5초) 어떤 Task에 잡혀 있으면 발생하게 된다.
    * 애플리케이션이 UI 스레드에 어떠한 I/O 명령(빈번한 네트워크 액세스)으로 인해 막힐 때
    * 너무 많은 시간을 정교한 메모리 구조를 구축하는데 들일 때
```
handler.sendMessageDelayed(msg,10000)
```
#
# AsyncTask
지금까지 살펴본 Thread의 경우 UI를 관리하는 Main Thread외에 Background Thread에서 MainThread UI 접근하려면 
Handler를 사용하여 새로운 Thread를 만들어서 통신을 했는데. 이러한 여러 가지 작업을 AsyncTask 객체 하나로 사용하기 쉽게
만들어 놓은 것. 클래스 하나에 Thread와 Handler의 기능이 콜백 메서드로 모두 존재 한다.
* onPreExecute(): run()이 실행되기전에 준비 단계. ex) 다이얼 로그창을 띄움.
* doInBackground(): run()의 내용.
* onProgressUpdate(): 데이터가 다운로드가 시작 되고, 1%..2%.. 작업의 진행 정도.
* onPostExecute(): 스레드의 작업이 모두 종료된 다음 호출.
* onCancelled(): 작업이 중단되었을 경우.

## Implementation
* MainActivity
```
newAsyncTask = ExampleAsyncTask(this@MainActivity,mBinding.tvSound)
newAsyncTask.execute(100,50) // doinBackground param
```
* ExampleAsyncTask
```
class ExampleAsyncTask(c: Context?, t: TextView?) : AsyncTask<Int?, String?, Int>() {

    var mContext: Context? = null
    var mTv: TextView? = null
    var dialog: ProgressDialog? = null
    var cancelFlag = false

    init {
        mContext = c
        mTv = t
    }

    companion object {
        const val TAG = "NewAsyncTask"
    }

    override fun onPreExecute() {
        // TODO Auto-generated method stub
        Log.i(TAG, "onPreExecute()")
        dialog = ProgressDialog(mContext)
        dialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        dialog!!.setTitle("Dialog")
        dialog!!.setMessage("work start")
        dialog!!.setCancelable(false)
        dialog!!.setButton(
            DialogInterface.BUTTON_NEGATIVE,
            "Cancel"
        ) { dialog, which ->
            dialog.dismiss()
            cancelFlag = true
            cancel(true)
        }
        dialog!!.progress = 0
        dialog!!.show()
    }

    override fun doInBackground(vararg p0: Int?): Int? {
        Log.i(TAG, "doInBackground()")
        Log.i(TAG, "params[0] : " + p0[0]) // 100
        Log.i(TAG, "params[1] : " + p0[1]) // 50 
        for (i in 0 until p0[0]!!) {
            publishProgress(i.toString()) // onProgressUpdate 메소드 호출.
            try {
                Thread.sleep(100)
            } catch (e: Exception) {
            }
            if (cancelFlag) break
        }
        return p0[0]
    }

    override fun onProgressUpdate(vararg values: String?) {
        Log.i(TAG, "onProgressUpdate()")
        val i = values[0]?.toInt()
        dialog!!.progress = i!!
        mTv!!.text = i.toString()
    }

    override fun onPostExecute(result: Int) {
        Log.i(TAG, "onPostExecute()")
        Log.i(TAG, "result : $result")
        mTv!!.text = result.toString()
        dialog!!.dismiss()
    }

    override fun onCancelled() {
        Log.i(TAG, "onCancelled()")
        mTv!!.text = "Cancelled"
        dialog!!.dismiss()
    }
}
```
