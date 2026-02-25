package com.example.watchit_movieapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.databinding.ItemProviderBinding
import com.example.watchit_movieapp.model.ProviderItem
import com.example.watchit_movieapp.utilities.ImageLoader

class ProviderAdapter : RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {
    private var providers: List<ProviderItem> = emptyList()

    fun updateData(newList: List<ProviderItem>?) {
        this.providers = newList ?: emptyList()
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ProviderViewHolder {
        val binding = ItemProviderBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return ProviderViewHolder(binding)
    }
    override fun onBindViewHolder(
        holder: ProviderAdapter.ProviderViewHolder,
        position: Int
    ) {
        with(holder){
            with(getItem(position)){
                binding.ivProviderName.text =this.name
                ImageLoader.getInstance().loadImage(
                    fullLogoUrl,
                    binding.ProviderLogo
                )



            }
        }


    }


    fun getItem(position: Int): ProviderItem = providers[position]


    override fun getItemCount(): Int = providers.size

    inner class ProviderViewHolder(val binding: ItemProviderBinding) :
        RecyclerView.ViewHolder(binding.root)

}