package com.jshvarts.coroutines.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.transition.TransitionInflater
import com.google.android.material.snackbar.Snackbar
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.UserDetailsFragmentBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso
import dagger.hilt.android.AndroidEntryPoint

private const val AVATAR_WIDTH = 500

@AndroidEntryPoint
class UserDetailFragment : Fragment() {
    private val args by navArgs<UserDetailFragmentArgs>()

    private val viewModel: UserDetailViewModel by viewModels()

    private var _binding: UserDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    private val imageLoadingCallback = object : Callback {
        override fun onSuccess() {
            startPostponedEnterTransition()
        }

        override fun onError(e: Exception?) {
            // no-op
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context)
            .inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        postponeEnterTransition()
        _binding = UserDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userDetails.observe(viewLifecycleOwner) { userDetails ->
            Picasso.get()
                .load(userDetails.avatarUrl)
                .resize(AVATAR_WIDTH, AVATAR_WIDTH)
                .centerCrop()
                .into(binding.avatarImageView, imageLoadingCallback)

            binding.avatarImageView.transitionName = args.username
            binding.userFullName.text = userDetails.name
        }

        viewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (!isError) return@observe

            Snackbar.make(
                binding.root,
                R.string.error_message,
                Snackbar.LENGTH_LONG
            ).show()
        }

        viewModel.lookupUser(args.username)

        binding.userReposButton.setOnClickListener {
            val action = UserDetailFragmentDirections
                .actionUserDetailsToUserRepos(args.username)
            findNavController().navigate(action)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
