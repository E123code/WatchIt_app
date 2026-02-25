package com.example.watchit_movieapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.model.CastMember
import com.example.watchit_movieapp.databinding.CastMemberBinding
import com.example.watchit_movieapp.utilities.ImageLoader


class CastAdapter : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {

    private var castList: List<CastMember> = emptyList()

    fun updateData(newList: List<CastMember>?) {
        this.castList = newList ?: emptyList()
        notifyDataSetChanged()
    }
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CastViewHolder {
        val binding = CastMemberBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return CastViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: CastAdapter.CastViewHolder,
        position: Int
    ) {
        with(holder){
            with(getItem(position)){
                binding.CastName.text = this.name
                binding.CharacterName.text= this.character
                ImageLoader.getInstance().loadProfile(
                    fullCastUrl,
                    binding.CastProfileIMG
                )



            }
        }


    }


    fun getItem(position: Int): CastMember = castList[position]


    override fun getItemCount(): Int = castList.size

    inner class CastViewHolder(val binding: CastMemberBinding) :
        RecyclerView.ViewHolder(binding.root)
}