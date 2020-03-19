package com.example.coroutines.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.coroutines.CoroutinesApp
import com.example.coroutines.R
import kotlinx.android.synthetic.main.user_details_fragment.*
import javax.inject.Inject

class UserDetailFragment : Fragment() {

    companion object {
        fun newInstance() = UserDetailFragment()
    }

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

        viewModel.userDetails.observe(viewLifecycleOwner, Observer {
            messageTitle.text = resources.getText(R.string.message_title_success)
            detail.text = resources.getText(R.string.message_detail)
            println("success getting user details: $it")
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer {
            messageTitle.text = resources.getText(R.string.message_title_error)
            detail.text = resources.getText(R.string.message_detail)
            println("error getting user details: $it")
        })

        viewModel.lookupUser("jshvarts")
    }
}
