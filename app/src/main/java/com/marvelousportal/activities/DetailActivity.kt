package com.marvelousportal.activities

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.text.Html
import android.util.Log
import android.view.View
import android.widget.LinearLayout
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.marvelousportal.R
import com.marvelousportal.activities.adapter.DetailComicAdapter
import com.marvelousportal.activities.adapter.DetailEventAdapter
import com.marvelousportal.activities.adapter.DetailSeriesAdapter
import com.marvelousportal.app.AppController
import com.marvelousportal.base.BaseActivity
import com.marvelousportal.models.Item
import com.marvelousportal.models.Result
import com.marvelousportal.utils.Constant.Companion.CHARACTERS
import com.marvelousportal.utils.Constant.Companion.COMICS
import com.marvelousportal.utils.Constant.Companion.EVENTS
import com.marvelousportal.utils.Constant.Companion.SERIES
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_detail.*
import java.io.ByteArrayOutputStream
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class DetailActivity : BaseActivity() {

    var bitmapImage: Bitmap? = null
    private var mDetailComicAdapter: DetailComicAdapter? = null
    private var comicsIssueList: MutableList<Item>? = null

    private var mDetailSeriesAdapter: DetailSeriesAdapter? = null
    private var seriesIssueList: MutableList<Item>? = null

    private var mDetailEventAdapter: DetailEventAdapter? = null
    private var eventIssueList: MutableList<Item>? = null


    private var charactersIssueList: MutableList<Item>? = null

    private val detailViewModel = AppController.injectDetailViewModel()

    companion object {
        const val ID = "ID"
        val DETAILTYPE = "DETAILTYPE"

        /**
         * call this method to launch the Main Activity
         */

        fun launchActivity(context: Context, id: Int, type: String) {
            val intent = Intent(context, DetailActivity::class.java)
            intent.putExtra(this.ID, id)
            intent.putExtra(DETAILTYPE, type)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detail)
        setSupportActionBar(toolbar)
        //noinspection ConstantConditions
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        init()
    }

    /**
     * initialize the app
     */
    private fun init() {
        comicsIssueList = ArrayList()
        seriesIssueList = ArrayList()
        eventIssueList = ArrayList()
        mDetailComicAdapter = DetailComicAdapter(this, comicsIssueList!!)
        mDetailSeriesAdapter = DetailSeriesAdapter(this, seriesIssueList!!)
        mDetailEventAdapter = DetailEventAdapter(this, eventIssueList!!)

        val id = intent.getIntExtra(ID, 0)
        if (id > 0) {
            checkForType(id)
        }
        //image click listener for zoom
        iv_detail_image.setOnClickListener {
            val stream = ByteArrayOutputStream()
            bitmapImage?.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            val bytes = stream.toByteArray()
            if (bytes != null) {
                ZoomImageActivity.launchActivity(this, bytes)
            }
        }
    }

    private fun checkForType(id: Int) {
        val type = intent.getStringExtra(DETAILTYPE)
        when (type) {
            COMICS -> comicDetails(id)
            CHARACTERS -> characterDetails(id)
            EVENTS -> eventsDetails(id)
            SERIES -> seriesDetails(id)
        }
    }

    private fun characterDetails(id: Int) {
        subscribe(detailViewModel.getCharactersDetail(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
            Log.d("Success", "Received UIModel with ${it.data?.count} characters.")
            setUpDetail(it.data!!.results)
        }, {
            Log.w("throws", it.localizedMessage)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })!!)
    }

    private fun comicDetails(id: Int) {
        subscribe(detailViewModel.getComicsDetail(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
            Log.d("Success", "Received UIModel with ${it.data?.count} characters.")
            setUpDetail(it.data!!.results)
        }, {
            Log.w("throws", it.localizedMessage)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })!!)
    }

    /**
     * get the formatted date
     */
    @SuppressLint("SimpleDateFormat")
    private fun getFormattedDate(date: String): CharSequence? {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd")
        var myDate: Date? = null
        try {
            myDate = dateFormat.parse(date)
        } catch (e: ParseException) {
            e.printStackTrace()
        }
        val timeFormat = SimpleDateFormat("dd MMM, yyy")
        return timeFormat.format(myDate)
    }

    private fun seriesDetails(id: Int) {
        subscribe(detailViewModel.getSeriesDetail(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
            Log.d("Success", "Received UIModel with ${it.data?.count} characters.")
            setUpDetail(it.data!!.results)
        }, {
            Log.w("throws", it.localizedMessage)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })!!)
    }

    private fun eventsDetails(id: Int) {
        subscribe(detailViewModel.getEventDetail(id)?.subscribeOn(Schedulers.io())?.observeOn(AndroidSchedulers.mainThread())?.subscribe({
            Log.d("Success", "Received UIModel with ${it.data?.count} characters.")
            setUpDetail(it.data!!.results)
        }, {
            Log.w("throws", it.localizedMessage)
            Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
        })!!)
    }

    /**
     * set up the details of the character
     */
    @SuppressLint("SetTextI18n")
    private fun setUpDetail(results: List<Result>) {
        for (details in results) {
            val imageUrl = details.thumbnail.path + "." + details.thumbnail.extension

            Glide.with(this)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : SimpleTarget<Bitmap>() {
                        override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>) {
                            iv_detail_image.setImageBitmap(resource)
                            bitmapImage = resource
                        }
                    })

            if (intent.getStringExtra(DETAILTYPE) == EVENTS) {
                ll_detail_published.visibility = View.VISIBLE
                tv_start_date.text = getFormattedDate(details.start)
                tv_end_date.text = getFormattedDate(details.end)
            }

            if (!details.description.isNullOrEmpty()) {
                tv_detail_description.visibility = View.VISIBLE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    tv_detail_description.text = Html.fromHtml(details.description, Html.FROM_HTML_MODE_LEGACY)
                } else {
                    tv_detail_description.text = Html.fromHtml(details.description)
                }
            } else {
                tv_detail_description.text = getString(R.string.description_not_available)
            }

            //set name or title
            if (!details.name.isNullOrEmpty()) {
                toolbar_layout.title = details.name
            } else {
                toolbar_layout.title = details.title
            }

            for (url in details.urls) {
                if (url.type == "detail") {
                    tv_detail.visibility = View.VISIBLE
                    tv_detail.setOnClickListener {
                        openWebUrls(url.url)
                    }
                }
                if (url.type == "wiki") {
                    tv_wiki.visibility = View.VISIBLE
                    tv_wiki.setOnClickListener {
                        openWebUrls(url.url)
                    }
                }
                if (url.type == "comiclink") {
                    tv_comic.visibility = View.VISIBLE
                    tv_comic.setOnClickListener {
                        openWebUrls(url.url)
                    }
                }
            }

            //set up the comics listing
            if (details.comics != null) {
                comicsIssueList?.addAll(details.comics.items)
            }
            if (details.series != null) {
                seriesIssueList?.addAll(details.series.items)
            }
            if (details.events != null) {
                eventIssueList?.addAll(details.events.items)
            }
            if (details.characters != null) {
                charactersIssueList?.addAll(details.characters.items)
            }

            setUpDetailComics(comicsIssueList!!)
            setUpDetailSeries(seriesIssueList!!)
            setUpDetailEvents(eventIssueList!!)
        }
    }

    private fun setUpDetailEvents(item: List<Item>) {
        if (item.isNotEmpty()) {
            rv_detail_event.isNestedScrollingEnabled = false
            rv_detail_event.visibility = View.VISIBLE
            tv_detail_event.visibility = View.VISIBLE

            val layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
            rv_detail_event.layoutManager = layoutManager
            rv_detail_event.adapter = mDetailEventAdapter
            mDetailEventAdapter?.setUserList(item)
        }
    }

    private fun setUpDetailSeries(item: List<Item>) {
        if (item.isNotEmpty()) {
            rv_detail_series.isNestedScrollingEnabled = false
            rv_detail_series.visibility = View.VISIBLE
            tv_detail_series.visibility = View.VISIBLE

            val layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
            rv_detail_series.layoutManager = layoutManager
            rv_detail_series.adapter = mDetailSeriesAdapter
            mDetailSeriesAdapter?.setUserList(item)
        }
    }

    private fun setUpDetailComics(item: List<Item>) {
        if (item.isNotEmpty()) {
            rv_detail_comics.isNestedScrollingEnabled = false
            rv_detail_comics.visibility = View.VISIBLE
            tv_detail_comics.visibility = View.VISIBLE

            val layoutManager = LinearLayoutManager(this, LinearLayout.HORIZONTAL, false)
            rv_detail_comics.layoutManager = layoutManager
            rv_detail_comics.adapter = mDetailComicAdapter
            mDetailComicAdapter?.setUserList(item)
        }
    }

    /**
     * open the url in the web browser
     */
    private fun openWebUrls(url: String) {
        val browserIntent = Intent(Intent.ACTION_VIEW)
        browserIntent.data = Uri.parse(url)
        startActivity(browserIntent)
    }


    /**
     * get Dominant color from the image view
     */
    private fun getDominantColor(bitmap: Bitmap): Int {
        val newBitmap = Bitmap.createScaledBitmap(bitmap, 1, 1, true)
        val color = newBitmap.getPixel(0, 0)
        newBitmap.recycle()
        return color
    }
}