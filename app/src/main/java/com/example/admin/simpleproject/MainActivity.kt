package com.example.admin.simpleproject

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import com.example.admin.daggervskoin.R.layout.activity_main
import com.example.admin.simpleproject.adapter.UserAdapter
import com.example.admin.simpleproject.interfaces.RandomUsersApi
import com.example.admin.simpleproject.model.User
import com.google.gson.GsonBuilder
import com.jakewharton.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var retrofit: Retrofit
    private lateinit var recyclerView: RecyclerView
    private lateinit var mAdapter: UserAdapter
    private lateinit var picasso: Picasso
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(activity_main)
        initViews()

        val gsonBuilder = GsonBuilder()
        val gson = gsonBuilder.create()

        Timber.plant(Timber.DebugTree())

        val cacheFile = File(this.cacheDir, "HttpCache")
        cacheFile.mkdirs()

        val cache = Cache(cacheFile, 10 * 1000 * 1000) //10 MB

        val httpLoggingInterceptor =
            HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
                Timber.i(message)
            })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val okHttpClient = OkHttpClient()
            .newBuilder()
            .addInterceptor(httpLoggingInterceptor)
            .build()

        val okHttpDownloader = OkHttp3Downloader(okHttpClient)

        picasso = Picasso.Builder(this).downloader(okHttpDownloader).build()

        retrofit = Retrofit.Builder()
            .client(okHttpClient)
            .baseUrl("https://randomuser.me/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        populateUsers()
    }

    private fun populateUsers() {
        val callUsers = getUserService().getRandomUsers(10)
        callUsers.enqueue(object : Callback<User>{
            override fun onFailure(call: Call<User>, t: Throwable) {
                Timber.i(t.message)
            }

            override fun onResponse(call: Call<User>, response: Response<User>) {
                if (response.isSuccessful) {
                    mAdapter = UserAdapter()
                    response.body()?.let { user ->
                        mAdapter.setItems(user.results)
                    }
                    recyclerView.adapter = mAdapter
                }
            }

        })
    }

    private fun initViews() {
        recyclerView = findViewById(com.example.admin.daggervskoin.R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
    }

    private fun getUserService() : RandomUsersApi{
        return retrofit.create(RandomUsersApi::class.java)
    }
}
