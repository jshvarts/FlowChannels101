package com.jshvarts.coroutines.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.RepoItemBinding
import com.jshvarts.coroutines.domain.Repo
import com.jshvarts.coroutines.domain.RepoOwner
import com.squareup.picasso.Picasso

private typealias RepoClickListener = (RepoOwner, ImageView) -> Unit
private const val AVATAR_WIDTH = 250

class RepoAdapter(
    private val clickListener: RepoClickListener
) : ListAdapter<Repo, RepoViewHolder>(RepoDiffCallback()) {
    override fun onCreateViewHolder(parent: ViewGroup, position: Int): RepoViewHolder {
        val binding = RepoItemBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RepoViewHolder(binding, clickListener)
    }

    override fun onBindViewHolder(holder: RepoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}

class RepoViewHolder(
    binding: RepoItemBinding,
    private val clickListener: RepoClickListener
) : RecyclerView.ViewHolder(binding.root), View.OnClickListener {

    private lateinit var item: Repo

    private val repoNameView = binding.repoName
    private val starsView = binding.stars
    private val repoOwnerView = binding.repoOwner
    private val avatarImageView = binding.avatarImageView

    init {
        avatarImageView.setOnClickListener(this)
        repoOwnerView.setOnClickListener(this)
    }

    fun bind(item: Repo) {
        this.item = item
        repoNameView.text = item.name
        starsView.text = itemView.resources.getQuantityString(
            R.plurals.repo_stars, 0, item.stars
        )

        avatarImageView.apply {
            Picasso.get()
                .load(item.owner.avatarUrl)
                .resize(AVATAR_WIDTH, AVATAR_WIDTH)
                .centerCrop()
                .into(this)
            transitionName = item.owner.login
        }

        repoOwnerView.text = item.owner.login
    }

    override fun onClick(v: View?) {
        v?.let { clickListener.invoke(item.owner, avatarImageView) }
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


