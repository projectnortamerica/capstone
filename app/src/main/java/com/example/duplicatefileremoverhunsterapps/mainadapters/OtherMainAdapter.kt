package com.example.duplicatefileremoverhunsterapps.mainadapters

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.duplicatefileremoverhunsterapps.childadapters.OtherChildAdapter
import com.example.duplicatefileremoverhunsterapps.interfaces.MyAdapterListener
import com.example.duplicatefileremoverhunsterapps.models.FilesModel
import com.example.duplicatefileremoverhunsterapps.databinding.MainImageItemBinding

class OtherMainAdapter(val list:List<FilesModel>,
                       val context: Context, val adapterListener: MyAdapterListener
) :
    RecyclerView.Adapter<OtherMainAdapter.MyViewHolder>() {

    class MyViewHolder(val binding: MainImageItemBinding) : RecyclerView.ViewHolder(binding.root)
    var pos:Int=0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        return MyViewHolder(
            MainImageItemBinding.inflate(LayoutInflater.
        from(parent.context),parent,false))
    }

    @SuppressLint("SetTextI18n")
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        pos++
        holder.binding.tvGroup.text= "Others Set $pos"
        holder.binding.recyclerView.layoutManager=LinearLayoutManager(context)
        holder.binding.recyclerView.adapter= OtherChildAdapter(
            list[position].imagesList,context,adapterListener)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}