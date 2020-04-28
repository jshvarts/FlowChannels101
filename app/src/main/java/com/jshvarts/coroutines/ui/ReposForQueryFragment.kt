package com.jshvarts.coroutines.ui

import android.app.SearchManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jshvarts.coroutines.CoroutinesApp
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.ReposForQueryFragmentBinding
import com.jshvarts.coroutines.domain.RepoOwner
import javax.inject.Inject

private const val GRID_COLUMN_COUNT = 2

class ReposForQueryFragment : Fragment() {

    private val recyclerViewAdapter = RepoAdapter { repoOwner, avatarImageView ->
        onRepoOwnerClicked(repoOwner, avatarImageView)
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: ReposForQueryViewModel by viewModels { viewModelFactory }

    private var _binding: ReposForQueryFragmentBinding? = null
    private val binding get() = _binding!!

    private val refreshAfterErrorListener = View.OnClickListener {
        viewModel.refresh()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        (requireActivity().application as CoroutinesApp)
            .appComponent
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = ReposForQueryFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.reposForQueryRecyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(activity, GRID_COLUMN_COUNT)
        }

        viewModel.repos.observe(viewLifecycleOwner) {
            recyclerViewAdapter.submitList(it)
        }

        viewModel.isError.observe(viewLifecycleOwner) { isError ->
            if (!isError) return@observe

            Snackbar.make(
                binding.root,
                R.string.error_message,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.refresh_button_text, refreshAfterErrorListener)
                .show()
        }

        viewModel.showSpinner.observe(viewLifecycleOwner) { showSpinner ->
            binding.pullToRefresh.isRefreshing = showSpinner
            binding.scrimView.visibility = if (showSpinner) View.VISIBLE else View.GONE
        }

        binding.pullToRefresh.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repos_menu, menu)

        val manager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView

        searchView.setSearchableInfo(manager.getSearchableInfo(requireActivity().componentName))

        val querySearchListener = object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query ?: return false

                searchView.clearFocus()
                searchView.setQuery("", false)
                searchItem.collapseActionView()
                viewModel.lookupRepos(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        }
        searchView.setOnQueryTextListener(querySearchListener)
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }

    private fun onRepoOwnerClicked(repoOwner: RepoOwner, avatarImageView: ImageView) {
        val action = ReposForQueryFragmentDirections
            .actionReposToUserDetails(repoOwner.login)

        val extras = FragmentNavigatorExtras(
            avatarImageView to repoOwner.login
        )
        findNavController().navigate(action, extras)
    }
}
