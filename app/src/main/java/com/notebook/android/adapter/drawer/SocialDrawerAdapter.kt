package com.notebook.android.adapter.drawer

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.notebook.android.BR
import com.notebook.android.R
import com.notebook.android.data.db.entities.SocialData
import com.notebook.android.databinding.SocialLinkDrawerLayoutBinding

class SocialDrawerAdapter(val mCtx: Context, val socialDataList:List<SocialData>,
                          val socialDataListener: SocialDataListener
) : RecyclerView.Adapter<SocialDrawerAdapter.SocialVH>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): SocialVH {
        val socialLinkBinding: SocialLinkDrawerLayoutBinding = DataBindingUtil.inflate(
            LayoutInflater.from(mCtx), R.layout.social_link_drawer_layout, parent, false)
        return SocialVH(socialLinkBinding)
    }

    override fun getItemCount(): Int {
        return socialDataList.size
    }

    override fun onBindViewHolder(holder: SocialVH, position: Int) {
      val socialData = socialDataList[position]
        holder.bind(socialData)
    }

    inner class SocialVH(val socialLinkBinding: SocialLinkDrawerLayoutBinding)
        : RecyclerView.ViewHolder(socialLinkBinding.root) {

        fun bind(socialData:SocialData){
            socialLinkBinding.setVariable(BR.socialDataModal, socialData)
            socialLinkBinding.executePendingBindings()
            socialLinkBinding.clSocialLinkLayout.setOnClickListener {
                socialDataListener.showSocialPage(socialData.url)
            }
        }
    }

    interface SocialDataListener{
        fun showSocialPage(url:String)
    }
}