package com.example.coroutines.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutines.R
import com.example.coroutines.databinding.RepoItemBinding
import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner

typealias RepoOwnerClickListener = (RepoOwner) -> Unit

class RepoAdapter(private val clickListener: RepoOwnerClickListener) :
    ListAdapter<Repo, RepoViewHolder>(RepoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RepoViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = RepoItemBinding.inflate(inflater, parent, false)

        return RepoViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = getItem(position)
        holder.bind(repo)
    }
}

class RepoViewHolder(
    private val binding: RepoItemBinding,
    private val clickListener: RepoOwnerClickListener
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

class RepoDiffCallback : DiffUtil.ItemCallback<Repo>() {
    override fun areItemsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Repo, newItem: Repo): Boolean {
        return oldItem.name == newItem.name
    }
}
