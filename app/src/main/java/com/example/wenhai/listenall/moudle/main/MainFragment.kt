package com.example.wenhai.listenall.moudle.main

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v4.view.ViewPager
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.LinearLayout
import butterknife.BindView
import butterknife.ButterKnife
import butterknife.OnClick
import butterknife.Unbinder
import com.example.wenhai.listenall.R
import com.example.wenhai.listenall.moudle.main.local.LocalFragment
import com.example.wenhai.listenall.moudle.main.online.OnLineFragment
import com.example.wenhai.listenall.moudle.search.SearchFragment
import com.example.wenhai.listenall.utils.FragmentUtil
import com.example.wenhai.listenall.utils.LogUtil

/**
 * main Fragment
 *
 * Created by Wenhai on 2017/8/4.
 */


class MainFragment : Fragment() {


    companion object {
        const val TAG = "MainFragment"

        const val PAGE_POSITION_MY_SONGS = 0
        const val PAGE_POSITION_ONLINE_SONGS = 1
        const val PAGE_COUNT = 2

    }

    @BindView(R.id.main_pager)
    lateinit var mPager: ViewPager
    @BindView(R.id.main_btn_local_songs)
    lateinit var mBtnMySongs: Button
    @BindView(R.id.main_btn_online_songs)
    lateinit var mBtnOnlineSongs: Button
    @BindView(R.id.main_btn_search)
    lateinit var mBtnSearch: ImageButton
    @BindView(R.id.main_et_search)
    lateinit var mEtSearch: EditText
    @BindView(R.id.main_tab)
    lateinit var mTab: LinearLayout
    @BindView(R.id.main_btn_cancel)
    lateinit var mCancelSearch: Button


    lateinit var mUnBinder: Unbinder
    lateinit var mPagerAdapter: MainPagerAdapter
    var searchFragment: SearchFragment? = null
    var textWatch: TextWatcher? = null
    var isTextChangedByUser = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val contentView = inflater !!.inflate(R.layout.fragment_main, container, false)
        mUnBinder = ButterKnife.bind(this, contentView)
        initView()
        return contentView
    }

    fun initView() {
        mPagerAdapter = MainPagerAdapter(fragmentManager)
        mPager.adapter = mPagerAdapter
        onButtonClick(mBtnMySongs)
        mPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrollStateChanged(state: Int) {
            }

            override fun onPageScrolled(position: Int, positionOffset: Float, positionOffsetPixels: Int) {
            }

            @Suppress("DEPRECATION")
            override fun onPageSelected(position: Int) {
                when (position) {
                    PAGE_POSITION_MY_SONGS -> {
                        mBtnMySongs.setTextColor(resources.getColor(R.color.colorBlack))
                        mBtnOnlineSongs.setTextColor(resources.getColor(R.color.colorNotSelected))
                    }
                    PAGE_POSITION_ONLINE_SONGS -> {
                        mBtnOnlineSongs.setTextColor(resources.getColor(R.color.colorBlack))
                        mBtnMySongs.setTextColor(resources.getColor(R.color.colorNotSelected))
                    }
                }
            }

        })

    }

    @Suppress("DEPRECATION")
    @OnClick(R.id.main_btn_local_songs, R.id.main_btn_online_songs, R.id.main_btn_slide_menu,
            R.id.main_btn_search, R.id.main_btn_cancel)
    fun onButtonClick(v: View) {
        mEtSearch.clearFocus()
        when (v.id) {
            R.id.main_btn_local_songs -> {
                mEtSearch.animation
                mPager.currentItem = PAGE_POSITION_MY_SONGS
                mBtnMySongs.setTextColor(resources.getColor(R.color.colorBlack))
                mBtnOnlineSongs.setTextColor(resources.getColor(R.color.colorNotSelected))
            }
            R.id.main_btn_online_songs -> {
                mPager.currentItem = PAGE_POSITION_ONLINE_SONGS
                mBtnOnlineSongs.setTextColor(resources.getColor(R.color.colorBlack))
                mBtnMySongs.setTextColor(resources.getColor(R.color.colorNotSelected))
            }
            R.id.main_btn_slide_menu -> (activity as MainActivity).openDrawer()
            R.id.main_btn_search -> {
                showSearchBar()
            }
            R.id.main_btn_cancel -> {
                hideSearchBar()
            }
        }
    }

    fun hideSearchBar() {
        mTab.visibility = View.VISIBLE
        mBtnSearch.visibility = View.VISIBLE
        mEtSearch.visibility = View.GONE
        mEtSearch.removeTextChangedListener(textWatch)
        textWatch = null
        mCancelSearch.visibility = View.GONE
        if (searchFragment != null) {
            FragmentUtil.removeFragment(fragmentManager, searchFragment !!)
            searchFragment = null
        }
        hideSoftInput()
    }

    fun hideSoftInput() {
        mEtSearch.clearFocus()
        val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(mEtSearch.windowToken, 0)
    }

    @Suppress("DEPRECATION")
    private fun showSearchBar() {
        if (searchFragment == null) {
            searchFragment = SearchFragment()
        }
        FragmentUtil.addFragmentToView(fragmentManager, searchFragment !!, R.id.main_pager_container)

        mTab.visibility = View.GONE
        mBtnSearch.visibility = View.GONE
        mCancelSearch.visibility = View.VISIBLE
        mEtSearch.visibility = View.VISIBLE
        mEtSearch.setText("")
        showSoftInput()

        textWatch = object : TextWatcher {
            override fun afterTextChanged(editable: Editable?) {
                val text = editable !!.toString()
                if (text == "") {
                    searchFragment !!.showSearchHistory()
                    mEtSearch.setTextColor(context.resources.getColor(R.color.colorGray))
                } else {
                    // if user typed keyword ,then load recommend keywords
                    if (isTextChangedByUser) {
                        searchFragment !!.showSearchRecommend(text)
                    } else {
                        isTextChangedByUser = true
                    }
                    mEtSearch.setTextColor(context.resources.getColor(R.color.colorBlack))
                }
            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        }
        mEtSearch.addTextChangedListener(textWatch)

    }

    fun showSoftInput() {
        mEtSearch.requestFocus()
        val inputManager: InputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.showSoftInput(mEtSearch, InputMethodManager.SHOW_FORCED)
    }

    fun setSearchKeyword(keyword: String) {
        isTextChangedByUser = false
        mEtSearch.setText(keyword)
    }

    override fun onStop() {
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mUnBinder.unbind()
    }


    class MainPagerAdapter(fragmentManager: FragmentManager)
        : FragmentPagerAdapter(fragmentManager) {
        var onlineFragment: OnLineFragment? = null
        var localFragment: LocalFragment? = null

        override fun getItem(position: Int): Fragment {
            LogUtil.d("MainPagerAdapter", "getItem:$position")
            if (position == PAGE_POSITION_ONLINE_SONGS) {
                if (onlineFragment == null) {
                    onlineFragment = OnLineFragment()
                }
                return onlineFragment as OnLineFragment
            } else {
                if (localFragment == null) {
                    localFragment = LocalFragment()
                }
                return localFragment as LocalFragment
            }
        }

        override fun getCount(): Int = PAGE_COUNT
    }
}