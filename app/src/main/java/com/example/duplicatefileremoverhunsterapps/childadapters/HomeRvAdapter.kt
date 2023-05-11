package com.example.duplicatefileremoverhunsterapps.childadapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.Home
import com.example.duplicatefileremoverhunsterapps.databinding.HomeRvItemsBinding

class HomeRvAdapter(val context: Context, val list:List<Home>, val adapterListener: MyAdapterListener)
    : RecyclerView.Adapter<HomeRvAdapter.MyViewHolder>() {

   inner class MyViewHolder(val binding: HomeRvItemsBinding) : RecyclerView.ViewHolder(binding.root){
       init {
           itemView.setOnClickListener {
               adapterListener.getPos(adapterPosition)
           }
       }
   }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(HomeRvItemsBinding.inflate(LayoutInflater.
        from(parent.context),parent,false))
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(list[position].image).into(holder.binding.imageView)
        holder.binding.textView.text=list[position].text
    }

    override fun getItemCount(): Int {
        return list.size
    }

}