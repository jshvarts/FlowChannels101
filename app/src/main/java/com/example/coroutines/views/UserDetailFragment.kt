package com.example.coroutines.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import com.example.coroutines.CoroutinesApp
import com.example.coroutines.R
import com.google.android.material.snackbar.Snackbar
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.user_details_fragment.*
import javax.inject.Inject

private const val AVATAR_WIDTH = 250

class UserDetailFragment : Fragment() {
    private val args by navArgs<UserDetailFragmentArgs>()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserDetailViewModel by viewModels { viewModelFactory }

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
        return inflater.inflate(R.layout.user_details_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            Picasso.get()
                .load(userDetails.avatarUrl)
                .resize(AVATAR_WIDTH, AVATAR_WIDTH)
                .centerCrop()
                .into(avatarImageView)

            userFullName.text = userDetails.name
            userCompany.text = getString(R.string.user_company, userDetails.company)
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer { isError ->
            if (!isError) return@Observer

            Snackbar.make(
                userDetailsContainer,
                R.string.error_message,
                Snackbar.LENGTH_LONG
            ).show()
        })

        viewModel.lookupUser(args.username)
    }
}
