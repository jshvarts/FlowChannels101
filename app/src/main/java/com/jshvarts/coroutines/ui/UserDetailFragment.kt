package com.jshvarts.coroutines.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import com.jshvarts.coroutines.CoroutinesApp
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.UserDetailsFragmentBinding
import com.squareup.picasso.Picasso
import javax.inject.Inject

private const val AVATAR_WIDTH = 250

class UserDetailFragment : Fragment() {
    private val args by navArgs<UserDetailFragmentArgs>()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserDetailViewModel by viewModels { viewModelFactory }

    private var _binding: UserDetailsFragmentBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (requireActivity().application as CoroutinesApp)
            .appComponent
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = UserDetailsFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            Picasso.get()
                .load(userDetails.avatarUrl)
                .resize(AVATAR_WIDTH, AVATAR_WIDTH)
                .centerCrop()
                .into(binding.avatarImageView)

            binding.userFullName.text = userDetails.name
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer { isError ->
            if (!isError) return@Observer

            Snackbar.make(
                binding.root,
                R.string.error_message,
                Snackbar.LENGTH_LONG
            ).show()
        })

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
