package com.example.watchit_movieapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.watchit_movieapp.databinding.UserBinding
import com.example.watchit_movieapp.interfaces.UserClickedCallback
import com.example.watchit_movieapp.model.User
import com.example.watchit_movieapp.utilities.ImageLoader

class UserAdapter(
    private var users: List<User> = emptyList(),
    private val callback: UserClickedCallback
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    fun updateData(users: List<User>) {
        this.users = users
        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserViewHolder {
        val binding = UserBinding
            .inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        return UserViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: UserViewHolder,
        position: Int
    ) {
        with(holder) {
            with(getItem(position)) {
                binding.Name.text = this.username
                binding.email.text = this.email
                ImageLoader.getInstance().loadProfile(
                    profileImageUrl,
                    binding.friendIMG
                )
                binding.root.setOnClickListener {
                    callback.userClicked(this.uid)
                }
            }
        }
    }


    fun getItem(position: Int): User = users[position]


    override fun getItemCount(): Int = users.size

    inner class UserViewHolder(val binding: UserBinding) :
        RecyclerView.ViewHolder(binding.root)


}