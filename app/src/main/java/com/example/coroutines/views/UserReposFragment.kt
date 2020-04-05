package com.example.coroutines.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.coroutines.CoroutinesApp
import com.example.coroutines.R
import com.example.coroutines.domain.RepoOwner
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.user_repos_fragment.*
import javax.inject.Inject

private const val GRID_COLUMN_COUNT = 2

class UserReposFragment : Fragment() {

    private val repoOwnerClickListener = this::onRepoOwnerClicked

    private val recyclerViewAdapter = RepoAdapter(repoOwnerClickListener)

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserReposViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity().application as CoroutinesApp)
            .appComponent
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.user_repos_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        repos.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(activity, GRID_COLUMN_COUNT)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.userRepos.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.submitList(it)
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer { isError ->
            if (!isError) return@Observer

            Snackbar.make(
                userReposContainer,
                R.string.error_message,
                Snackbar.LENGTH_LONG
            ).show()
        })

        viewModel.lookupUserRepos("JakeWharton")
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repos_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        viewModel.changeSortOrder()
        return true
    }

    private fun onRepoOwnerClicked(repoOwner: RepoOwner) {
        val action = UserReposFragmentDirections
            .actionReposToUserDetails(repoOwner.login)
        findNavController().navigate(action)
    }
}
