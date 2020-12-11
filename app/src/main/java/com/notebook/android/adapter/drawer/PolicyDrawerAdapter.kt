package com.notebook.android.adapter.drawer

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.PolicyData
import com.notebook.android.data.db.entities.SocialData
import com.notebook.android.data.preferences.NotebookPrefs
import com.notebook.android.databinding.PolicyPartDrawerLayoutBinding
import com.notebook.android.databinding.SocialLinkDrawerLayoutBinding

class PolicyDrawerAdapter(val mCtx: Context, val policyDataList:List<PolicyData>,
                          val policyDataListener: PolicyDataListener
) : RecyclerView.Adapter<PolicyDrawerAdapter.SocialVH>() {

    private val notebookPrefs by lazy {
        NotebookPrefs(mCtx)
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SocialVH {
        val policyPartBinding: PolicyPartDrawerLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.policy_part_drawer_layout, parent, false)
        return SocialVH(policyPartBinding)
    }

    override fun getItemCount(): Int {
        return policyDataList.size
    }

    override fun onBindViewHolder(holder: SocialVH, position: Int) {
      val socialData = policyDataList[position]
        holder.bind(socialData)
    }

    inner class SocialVH(val policyPartBinding: PolicyPartDrawerLayoutBinding)
        : RecyclerView.ViewHolder(policyPartBinding.root) {

        fun bind(policyData:PolicyData){
            policyPartBinding.setVariable(BR.policyDataModal, policyData)
            policyPartBinding.executePendingBindings()

            if (policyData.title?.contains("terms & conditions", true) == true){
                Log.e("termsPolicyTitle", " :: ${policyData.url}")
                notebookPrefs.TermsConditionLink = policyData.url
            }

            policyPartBinding.clSocialLinkLayout.setOnClickListener {
                policyDataListener.showPolicyPage(policyData.url?:"", policyData.title?:"")
            }
        }
    }

    interface PolicyDataListener{
        fun showPolicyPage(policyID:String, title:String)
    }
}