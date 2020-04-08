package com.jshvarts.coroutines.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.RepoItemBinding
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.domain.RepoOwner

class RepoAdapter(private val clickListener: (RepoOwner) -> Unit) :
    ListAdapter<Repo, RepoAdapter.RepoViewHolder>(RepoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RepoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RepoItemBinding.inflate(inflater, parent, false)
        return RepoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class RepoViewHolder(
        private val binding: RepoItemBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(item: Repo) {
            binding.repoName.text = item.name
            binding.stars.text = itemView.resources.getQuantityString(
                R.plurals.repo_stars, 0, item.stars
            )
            binding.repoOwner.apply {
                text = item.owner.login
                setOnClickListener {
                    clickListener.invoke(item.owner)
                }
            }
        }
    }
}

class RepoDiffCallback : DiffUtil.ItemCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.name == newItem.name
    }
}


