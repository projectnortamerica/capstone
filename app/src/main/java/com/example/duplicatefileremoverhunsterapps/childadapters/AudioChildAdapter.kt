package com.example.duplicatefileremoverhunsterapps.childadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.FilesChildModel
import com.example.duplicatefileremoverhunsterapps.R
import com.example.duplicatefileremoverhunsterapps.databinding.ChildImageItemBinding

class AudioChildAdapter(val list:List<FilesChildModel>,
                        val context: Context, val adapterListener: MyAdapterListener
)
    : RecyclerView.Adapter<AudioChildAdapter.MyViewHolder>() {

   inner class MyViewHolder(val binding: ChildImageItemBinding) : RecyclerView.ViewHolder(binding.root) {
        init {
            itemView.setOnClickListener {
                list[adapterPosition].check=!list[adapterPosition].check
                if(list[adapterPosition].check){
                    list[adapterPosition].background= R.drawable.ic_checked
                }
                else{
                    list[adapterPosition].background=R.drawable.ic_uncheck
                }
                notifyItemChanged(adapterPosition)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(ChildImageItemBinding.inflate
            (LayoutInflater.from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(R.drawable.btn_music).into(holder.binding.thumb)
        Glide.with(context).load(list[position].background).into(holder.binding.checkbox)
        holder.binding.tvName.text=list[position].name
        if (position==0){
            list[position].check=false
            Glide.with(context).load(R.drawable.ic_uncheck).into(holder.binding.checkbox)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}