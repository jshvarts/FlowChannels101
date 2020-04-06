package com.example.coroutines.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.coroutines.R
import com.example.coroutines.domain.Repo
import com.example.coroutines.domain.RepoOwner

typealias RepoOwnerClickListener = (RepoOwner) -> Unit

class RepoAdapter(private val clickListener: RepoOwnerClickListener) :
    ListAdapter<Repo, RepoViewHolder>(RepoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RepoViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.repo_item, parent, false)

        return RepoViewHolder(itemView, clickListener)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        val repo = getItem(position)
        holder.bindTo(repo)
    }
}

class RepoViewHolder(itemView: View, private val clickListener: RepoOwnerClickListener) :
    RecyclerView.ViewHolder(itemView) {
    private val repoName: TextView = itemView.findViewById(R.id.repoName)
    private val repoOwner: TextView = itemView.findViewById(R.id.repoOwner)
    private val stars: TextView = itemView.findViewById(R.id.stars)

    fun bindTo(item: Repo) {
        repoName.text = item.name

        stars.text = stars.resources.getQuantityString(
            R.plurals.repo_stars, 0, item.stars
        )

        repoOwner.text = item.owner.login
        repoOwner.setOnClickListener {
            clickListener.invoke(item.owner)
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
