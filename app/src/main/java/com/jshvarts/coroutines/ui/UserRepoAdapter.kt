package com.jshvarts.coroutines.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.UserRepoItemBinding
import com.jshvarts.coroutines.domain.Repo

class UserRepoAdapter :
    ListAdapter<Repo, UserRepoAdapter.UserRepoViewHolder>(UserRepoDiffCallback()) {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): UserRepoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = UserRepoItemBinding.inflate(inflater, parent, false)
        return UserRepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: UserRepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class UserRepoViewHolder(
        private val binding: UserRepoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Repo) {
            binding.repoName.text = item.name
            binding.stars.text = itemView.resources.getQuantityString(
                R.plurals.repo_stars, 0, item.stars
            )
        }
    }
}

class UserRepoDiffCallback : DiffUtil.ItemCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.name == newItem.name
    }
}


