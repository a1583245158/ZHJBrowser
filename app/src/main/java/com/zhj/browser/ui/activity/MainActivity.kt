package com.zhj.browser.ui.activity

import android.arch.lifecycle.ViewModelProviders
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.support.design.widget.CoordinatorLayout
import android.support.v4.app.FragmentActivity
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import com.zhj.browser.R
import com.zhj.browser.common.Global
import com.zhj.browser.common.IntentDict
import com.zhj.browser.common.PreferenceDict
import com.zhj.browser.storage.OpenPreference
import com.zhj.browser.ui.fragment.WebFragment
import com.zhj.browser.ui.popup.SearchPopup
import com.zhj.browser.ui.popup.WebToolMenu
import com.zhj.browser.ui.viewModel.WebViewModel
import kotlinx.android.synthetic.main.activity_main.*
import org.jetbrains.anko.dip
import org.jetbrains.anko.startActivity

class MainActivity : FragmentActivity() {

    private lateinit var webViewModel: WebViewModel
    private var openUrlReceiver: BroadcastReceiver? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        webViewModel = ViewModelProviders.of(this).get(WebViewModel::class.java)
        initData()
        initUI()
    }

    private fun initUI() {
        fragmentManager.beginTransaction().add(R.id.webViewContainer,WebFragment()).commit()

        startSearchView.setOnClickListener {
            val searchView = SearchPopup(this)
            searchView.showAtLocation(mainActivityLayout,Gravity.TOP or Gravity.START,0,0)

            goBackBtn.setOnClickListener {
                println("==============go back===========")
                webViewModel.action.value = WebViewModel.ACTION_BACK
            }
            goForwardBtn.setOnClickListener {
                webViewModel.action.value = WebViewModel.ACTION_FORWARD
            }
            goHomeBtn.setOnClickListener {
                webViewModel.action.value = WebViewModel.ACTION_HOME
            }
            updatePageBtn.setOnClickListener {
                webViewModel.action.value = WebViewModel.ACTION_SYNC
            }
        }

        toolMenu.setOnClickListener {
            showToolMenu()
        }
    }

    private fun initData(){
        Global.isFullScreen = OpenPreference.getBoolean(PreferenceDict.isFullScreen,false)
        webViewModel.isNoImgMode.value = OpenPreference.getBoolean(PreferenceDict.isNoImgMode,false)
        webViewModel.isAdaptive.value = OpenPreference.getBoolean(PreferenceDict.isAdaptive, false)

        openUrlReceiver = object : BroadcastReceiver(){
            override fun onReceive(ctx: Context?, intent: Intent?) {
                val searchContent = intent?.getStringExtra(IntentDict.URL)
                if(searchContent != null && searchContent.isNotBlank()){
                    webViewModel.currentUrl.value = searchContent
                }
            }
        }
        val intentFilter = IntentFilter()
        intentFilter.addAction(IntentDict.ACTION_SEARCH_URL)
        registerReceiver(openUrlReceiver,intentFilter)
    }

    private fun toggleFullScreen(){
        Global.isFullScreen = !Global.isFullScreen
        val lp = webViewContainer.layoutParams as CoordinatorLayout.LayoutParams
        if(isFullScreen){

        }
        webViewContainer.layoutParams = lp
    }

    private fun showToolMenu(){
        val popToolView = WebToolMenu(this){tag ->
            when(tag){
                "collection" -> {startActivity<FavActivity>()}
                "fav" -> {webViewModel.action.value = WebViewModel.ACTION_FAVORITE}
                "fullScreen" -> {toggleFullScreen()}
                "update" -> {webViewModel.action.value = WebViewModel.ACTION_SYNC}
                "save" -> {webViewModel.action.value = WebViewModel.ACTION_SAVE}
                "noImage" -> {
                    var isNoImgMode = webViewModel.isNoImgMode.value?:false
                    isNoImgMode = !isNoImgMode
                    OpenPreference.put(PreferenceDict.isNoImgMode,isNoImgMode)
                    webViewModel.isNoImgMode.value = isNoImgMode
                }
                "adaptive" -> {
                    var isAdaptive = webViewModel.isAdaptive.value?:false
                    isAdaptive = !isAdaptive
                    OpenPreference.put(PreferenceDict.isAdaptive,isAdaptive)
                    webViewModel.isAdaptive.value = isAdaptive
                }
                "exit" -> {System.exit(0)}
            }
        }
        popToolView.showAtLocation(mainActivityLayout,Gravity.BOTTOM or Gravity.START,0,0)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode == KeyEvent.KEYCODE_BACK){
            webViewModel.action.value = WebViewModel.ACTION_BACK
        }
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        if(openUrlReceiver != null){
            unregisterReceiver(openUrlReceiver)
            openUrlReceiver = null
        }
    }
}
