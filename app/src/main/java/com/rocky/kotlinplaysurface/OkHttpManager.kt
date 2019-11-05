package com.rocky.newringtones.base.baseutil

import android.os.Handler
import android.os.Looper
import android.util.Log
import okhttp3.*
import java.io.File
import java.io.IOException
import java.io.UnsupportedEncodingException
import java.net.URLEncoder
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * Created by Administrator on 2017/3/31.
 */

class OkHttpManager {
    private val mHandler = Handler(Looper.getMainLooper())

    private val client: OkHttpClient

    interface OnCallback {
        fun onError(e: IOException)

        fun onSuccess(response: Response)
    }

    init {
        // 初始化Okhttp
        client = OkHttpClient.Builder().connectTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(20, TimeUnit.SECONDS).readTimeout(20, TimeUnit.SECONDS).build()
    }

    operator fun get(url: String, params: Map<String, String>?, onCallback: OnCallback) {
        val buffer = StringBuffer()
        buffer.append(url)
        // 构建参数
        if (params != null && params.size > 0) {
            buffer.append("?")
            for ((key, value) in params) {
                try {
                    buffer.append(key).append("=").append(URLEncoder.encode(value, "utf-8"))
                    buffer.append("&")
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

            }
            // 删除最后一个 &
            buffer.deleteCharAt(buffer.length - 1)
        }

        // 构建请求对象
        val request = Request.Builder()
            .get()
            .url(buffer.toString())
            .build()
        // 发起异步请求
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                // 将子线程任务运行到主线程
                mHandler.post { onCallback.onError(e) }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {
                //                mHandler.post(new Runnable() {
                //                    @Override
                //                    public void run() {
                //                        onCallback.onSuccess(response);
                //                    }
                //                });
                onCallback.onSuccess(response)
            }
        })
    }




    fun post(url: String, params: Map<String, String>?, onCallback: OnCallback) {
        // 构建 builder 对象  主要用于添加参数
        val stringBuilder = StringBuilder()
        val builder = FormBody.Builder()
        if (params != null && params.size > 0) {
            for ((key, value) in params) {
                try {
                    builder.add(key, URLEncoder.encode(value, "utf-8"))
                    stringBuilder.append("&" + key + "=" + URLEncoder.encode(value))
                } catch (e: UnsupportedEncodingException) {
                    e.printStackTrace()
                }

            }
        }

        // 构建 body 对象
        val body = builder.build()
        // 构建请求对象
        val request = Request.Builder().post(body).url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                mHandler.post { onCallback.onError(e) }
            }

            override fun onResponse(call: okhttp3.Call, response: Response) {

                mHandler.post { onCallback.onSuccess(response) }
            }
        })
    }

    private fun uploadMultiFile(uploadUrl: String, file: File, name: String, fileName: String) {
        val url = "upload url"
        //        File file = new File("fileDir", "test.jpg");
        val fileBody = RequestBody.create(MediaType.parse("application/octet-stream"), file)
        val requestBody = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart(name, fileName, fileBody)
            .build()
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                Log.e(TAG, "uploadMultiFile() e=$e")
            }


            @Throws(IOException::class)
            override fun onResponse(call: okhttp3.Call, response: Response) {
                Log.i(TAG, "uploadMultiFile() response=" + response.body()!!.string())
                response.body()!!.byteStream()
            }
        })
    }

    companion object {
        private val TAG = "OkHttpUtils"
        private val params: HashMap<String, String>? = null
        private var mOkHttpUtils: OkHttpManager? = null

        val instances: OkHttpManager
            get() {
                if (null == mOkHttpUtils) {
                    mOkHttpUtils = OkHttpManager()
                }
                return mOkHttpUtils as OkHttpManager
            }
    }
}