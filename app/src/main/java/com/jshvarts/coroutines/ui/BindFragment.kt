package com.jshvarts.coroutines.ui

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import com.jshvarts.coroutines.R
import com.jshvarts.coroutines.databinding.FragmentBlankBinding

/**
 * View Binding example with a fragment that uses the alternate constructor for inflation and
 * [onViewCreated] for binding.
 */
class BindFragment : Fragment(R.layout.fragment_blank) {

    private var fragmentBlankBinding: FragmentBlankBinding? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val binding = FragmentBlankBinding.bind(view)
        fragmentBlankBinding = binding
        binding.textViewFragment.text = "foo"
    }

    override fun onDestroyView() {
        // Consider not storing the binding instance in a field, if not needed.
        fragmentBlankBinding = null
        super.onDestroyView()
    }
}
