package com.notebook.android.ui.splash

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.StrictMode
import android.util.Base64
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.data.preferences.RefferalPreferance
import com.notebook.android.databinding.ActivitySplashPageBinding
import com.notebook.android.ui.dashboard.MainDashboardPage
import org.kodein.di.KodeinAware
import org.kodein.di.android.kodein
import org.kodein.di.generic.instance
import java.lang.reflect.Method
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException


class SplashPage : AppCompatActivity(), KodeinAware, SplashResponseListener{

    private lateinit var splashBinding:ActivitySplashPageBinding
    override val kodein by kodein()
    private val viewModelFactory : SplashViewModelFactory by instance<SplashViewModelFactory>()
    private val splashVM: SplashVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(SplashVM::class.java)
    }

    private val refferalPrefs: RefferalPreferance by lazy {
        RefferalPreferance(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        splashBinding = DataBindingUtil.setContentView(this, R.layout.activity_splash_page)
        splashVM.splashListener = this
//        splashVM.getDrawerCategoryData()
//        printHashKey(this)

        if (Build.VERSION.SDK_INT >= 24) {
            try {
                val m: Method = StrictMode::class.java.getMethod("disableDeathOnFileUriExposure")
                m.invoke(null)
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
            }
        }

        handleScreenForHome()
        FirebaseDynamicLinks.getInstance().getDynamicLink(intent)
            .addOnSuccessListener(
                this
            ) { p0 ->
                var deepLinkUri: Uri?= null
                if(p0 != null){
                    deepLinkUri = p0.link
                }
                Log.e("deepLinkUri", " :: $deepLinkUri")

                if(deepLinkUri != null){
                    val productId = deepLinkUri.getQueryParameter("productID")?.toInt()
                    val refferalCode = deepLinkUri.getQueryParameter("reffer")
                    Log.e("deepLinkUri", " :: $deepLinkUri :: prodID -> " +
                            "$productId :: refferalCode -> $refferalCode")
                    prodID = productId?:-1

                    refferalPrefs.refferCode = refferalCode?:""
                }
            }.addOnFailureListener(this) {
                Log.e("dynamicLinkException", " :: ${it.localizedMessage}")
            }
    }

    private var prodID:Int = -1
    private fun handleScreenForHome(){
        Handler().postDelayed({
            val intentLogin = Intent(this@SplashPage, MainDashboardPage::class.java)
            intentLogin.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            intentLogin.putExtra("prodID", prodID)
            startActivity(intentLogin)
            finishAffinity()
        }, 2000)
    }

    override fun onSuccess() {
        handleScreenForHome()
    }

    override fun onFailure(msg: String) {
//        toastShow(msg)
        handleScreenForHome()
    }

    // This method for generating Hash key for facebook Login credentials....
    fun printHashKey(pContext: Context) {
        try {
            val info: PackageInfo = pContext.packageManager
                .getPackageInfo(pContext.packageName, PackageManager.GET_SIGNATURES)
            for (signature in info.signatures) {
                val md: MessageDigest = MessageDigest.getInstance("SHA")
                md.update(signature.toByteArray())
                val hashKey = String(Base64.encode(md.digest(), 0))
                Log.e("hashkey", "printHashKey() Hash Key: -> $hashKey")
            }
        } catch (e: NoSuchAlgorithmException) {
            Log.e("exception", "printHashKey() -> $e")
        } catch (e: Exception) {
            Log.e("exception", "printHashKey() -> $e")
        }
    }
}