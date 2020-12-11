package com.notebook.android.ui.myAccount.helpSupport

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.StyleSpan
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation

import com.notebook.android.R
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.FragmentHelpSupportBinding
import com.notebook.android.model.helpSupport.HelpSupportData
import com.notebook.android.ui.myAccount.address.AddressVM
import com.notebook.android.ui.myAccount.address.AddressVMFactory
import com.notebook.android.ui.myAccount.helpSupport.listener.HelpSupportListener
import com.notebook.android.utility.Constant
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.kodein
import org.kodein.di.generic.instance

class HelpSupportFrag : Fragment(), View.OnClickListener, KodeinAware, HelpSupportListener {

    override val kodein by kodein()
    private val viewModelFactory : HelpSupportVMFactory by instance()
    private val helpSupportVM: HelpSupportVM by lazy {
        ViewModelProvider(this, viewModelFactory).get(HelpSupportVM::class.java)
    }
    private lateinit var fragmentHelpSupportBinding: FragmentHelpSupportBinding
    private lateinit var navController:NavController
    private var supportEmail:String ?= null
    private var supportPhone:String ?= null
    private val notebookPrefs by lazy {
        NotebookPrefs(mContext)
    }

    private lateinit var mContext:Context
    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext = context
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        fragmentHelpSupportBinding = DataBindingUtil.inflate(inflater,
            R.layout.fragment_help_support, container, false)
        setTextClickable()
        helpSupportVM.helpSupportListener = this
//        helpSupportVM.getFaqDataFromServer()
        helpSupportVM.getHelpSupportData()
        return fragmentHelpSupportBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)

        fragmentHelpSupportBinding.tvCustomerCareEmailDesc.text = notebookPrefs.userContactEmail
        fragmentHelpSupportBinding.tvPhoneNumberHelp.text = "+91-${notebookPrefs.userContactPhone}"

        fragmentHelpSupportBinding.clHelpFeedback.setOnClickListener(this)
        fragmentHelpSupportBinding.tvReportProblemClickHere.setOnClickListener(this)
        fragmentHelpSupportBinding.clHelpEmail.setOnClickListener(this)
        fragmentHelpSupportBinding.clHelpPhone.setOnClickListener(this)
    }

    private val spanStartFrom = 13
    private val spanEndFrom = 18
    private fun setTextClickable(){
        val ssText = SpannableString(resources.getString(R.string.strHelpSupportDesc))
        ssText.setSpan(
            ForegroundColorSpan(resources.getColor(R.color.colorPrimary)),
            spanStartFrom, spanEndFrom, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        ssText.setSpan(StyleSpan(Typeface.BOLD), spanStartFrom, spanEndFrom, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        val spanRegNow =  object : ClickableSpan() {
            override fun onClick(widget: View) {
                val navController = Navigation.findNavController(widget)
                val bundle = Bundle()
                bundle.putString("faqLink", notebookPrefs.FaqsDynamicLink)
                Log.e("faqHelpSupport", " :: ${notebookPrefs.FaqsDynamicLink}")
                navController.navigate(R.id.faqsFrag, bundle)
            }

            override fun updateDrawState(ds: TextPaint) {
                super.updateDrawState(ds)
                ds.isUnderlineText = false
            }
        }
        ssText.setSpan(spanRegNow, spanStartFrom, spanEndFrom, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        fragmentHelpSupportBinding.tvHelpSupportDescs.movementMethod = LinkMovementMethod.getInstance()
        fragmentHelpSupportBinding.tvHelpSupportDescs.text = ssText
    }

    override fun onClick(v: View?) {
        when(v){
            fragmentHelpSupportBinding.clHelpFeedback -> {
                navController.navigate(R.id.action_helpSupportFrag_to_feedbackFrag)
            }

            fragmentHelpSupportBinding.tvReportProblemClickHere -> {
                navController.navigate(R.id.action_helpSupportFrag_to_reportProblemFrag)
            }

            fragmentHelpSupportBinding.clHelpEmail -> {
                emailSentToCustomerCare()
            }
            fragmentHelpSupportBinding.clHelpPhone -> {
                phoneCallIntent()
            }
        }
    }

    /*info@notebookstore.in*/
    private fun emailSentToCustomerCare(){
        val i = Intent(Intent.ACTION_SEND)
        i.type = "text/plain"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(supportEmail?:"info@notebookstore.in"))
        i.putExtra(Intent.EXTRA_SUBJECT, "Help and Support")
        i.putExtra(Intent.EXTRA_TEXT, "")
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                mContext,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun phoneCallIntent(){
        /*val intent = Intent(Intent.ACTION_CALL, Uri.parse("tel:" + "+91-9876543210"));
        startActivity(intent)*/

        val dialIntent = Intent(Intent.ACTION_DIAL)
        dialIntent.data = Uri.parse("tel:$supportPhone")
        dialIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(dialIntent)
    }

    private fun showErrorView(msg:String){
        fragmentHelpSupportBinding.clErrorView.visibility = View.VISIBLE
        fragmentHelpSupportBinding.tvErrorText.text = msg
        fragmentHelpSupportBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_down))

        Handler().postDelayed({
            fragmentHelpSupportBinding.clErrorView.visibility = View.GONE
            fragmentHelpSupportBinding.clErrorView.startAnimation(AnimationUtils.loadAnimation(mContext,R.anim.slide_up))
        }, 1200)
    }

    override fun onApiCallStarted() {

    }

    override fun onSuccesHelpSupportData(helpSupportData: HelpSupportData.HelpSupportMain) {
        notebookPrefs.userContactEmail = helpSupportData.email
        notebookPrefs.userContactPhone = helpSupportData.phone
       supportEmail = notebookPrefs.userContactEmail
        supportPhone = notebookPrefs.userContactPhone

        fragmentHelpSupportBinding.tvCustomerCareEmailDesc.text = notebookPrefs.userContactEmail
        fragmentHelpSupportBinding.tvPhoneNumberHelp.text = "+91-${notebookPrefs.userContactPhone}"
    }

    override fun onSuccess(msg: String) {

    }

    override fun onFailure(msg: String) {

    }

    override fun onApiFailure(msg: String) {

    }

    override fun onNoInternetAvailable(msg: String) {

    }

    override fun onInvalidCredential() {
        notebookPrefs.clearPreference()
        helpSupportVM.deleteUser()
        helpSupportVM.clearCartTableFromDB()
        navController.navigate(R.id.loginFrag)
    }
}
