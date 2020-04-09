package com.jshvarts.coroutines.ui

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
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.jshvarts.coroutines.CoroutinesApp
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.UserReposFragmentBinding
import com.jshvarts.coroutines.domain.MinStarCount
import com.jshvarts.coroutines.domain.NoMinStarCount
import javax.inject.Inject

private const val GRID_COLUMN_COUNT = 2
private const val STAR_COUNT_OVER_1_000 = 1_000
private const val STAR_COUNT_OVER_100 = 100

private const val DEFAULT_USERNAME = "JakeWharton"

class UserReposFragment : Fragment() {

    private val recyclerViewAdapter = UserRepoAdapter()

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: UserReposViewModel by viewModels { viewModelFactory }

    private var _binding: UserReposFragmentBinding? = null
    private val binding get() = _binding!!

    private val refreshAfterErrorListener = View.OnClickListener {
        viewModel.lookupUserRepos(DEFAULT_USERNAME)
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
        _binding = UserReposFragmentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.userReposRecyclerView.apply {
            adapter = recyclerViewAdapter
            layoutManager = GridLayoutManager(activity, GRID_COLUMN_COUNT)
        }

        viewModel.userRepos.observe(viewLifecycleOwner, Observer {
            recyclerViewAdapter.submitList(it)
        })

        viewModel.isError.observe(viewLifecycleOwner, Observer { isError ->
            if (!isError) return@Observer

            Snackbar.make(
                binding.root,
                R.string.error_message,
                Snackbar.LENGTH_INDEFINITE
            ).setAction(R.string.refresh_button_text, refreshAfterErrorListener)
                .show()
        })

        viewModel.showSpinner.observe(viewLifecycleOwner, Observer { showSpinner ->
            binding.pullToRefresh.isRefreshing = showSpinner
        })

        viewModel.lookupUserRepos(DEFAULT_USERNAME)

        binding.pullToRefresh.setOnRefreshListener {
            viewModel.lookupUserRepos(DEFAULT_USERNAME)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.repos_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.over_1000_stars -> {
                viewModel.filterRepos(MinStarCount(STAR_COUNT_OVER_1_000))
                true
            }
            R.id.over_100_stars -> {
                viewModel.filterRepos(MinStarCount(STAR_COUNT_OVER_100))
                true
            }
            R.id.any_stars -> {
                viewModel.filterRepos(NoMinStarCount)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}
