package com.khs.thread

import android.app.ProgressDialog
import android.content.Context
import android.content.DialogInterface
import android.os.AsyncTask
import android.util.Log
import android.widget.TextView

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