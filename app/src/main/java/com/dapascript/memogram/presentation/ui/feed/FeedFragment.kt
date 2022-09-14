package com.dapascript.memogram.presentation.ui.feed

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.dapascript.memogram.R
import com.dapascript.memogram.data.preference.UserPreference
import com.dapascript.memogram.databinding.FragmentFeedBinding
import com.dapascript.memogram.presentation.adapter.FeedAdapter
import com.dapascript.memogram.presentation.adapter.LoadPagingAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.*

@AndroidEntryPoint
class FeedFragment : Fragment() {

    private lateinit var binding: FragmentFeedBinding
    private lateinit var feedAdapter: FeedAdapter
    private lateinit var userPreference: UserPreference
    private val feedViewModel: FeedViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        if (this::binding.isInitialized) {
            binding
        } else {
            binding = FragmentFeedBinding.inflate(inflater, container, false)
            setAdapter()
        }
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setViewModel()

        Calendar.getInstance().also {
            with(binding.tvGreeting) {
                when (it.get(Calendar.HOUR_OF_DAY)) {
                    in 0..11 -> text = "Hi, Selamat Pagi"
                    in 12..15 -> text = "Hi, Selamat Siang"
                    in 16..18 -> text = "Hi, Selamat Sore"
                    in 19..23 -> text = "Hi, Selamat Malam"
                }
            }
        }

        val name = userPreference.userName
        name.observe(viewLifecycleOwner) {
            binding.tvName.text = it
        }
    }

    private fun setAdapter() {
        feedAdapter = FeedAdapter(requireActivity())
        feedAdapter.onClick = {
            val entity = it
            findNavController().navigate(
                R.id.action_nav_feed_to_detail_feed_fragment,
                Bundle().apply {
                    putParcelable("feedEntity", entity)
                }
            )
        }
        binding.rvFeed.apply {
            adapter = feedAdapter.withLoadStateFooter(
                footer = LoadPagingAdapter { feedAdapter.retry() }
            )
            layoutManager = LinearLayoutManager(requireActivity())
        }
    }

    private fun setViewModel() {
        userPreference = UserPreference(requireActivity())
        val token = userPreference.userToken

        token.observe(viewLifecycleOwner) {
            feedViewModel.getFeed(it).observe(viewLifecycleOwner) {
                viewLifecycleOwner.lifecycleScope.launch {
                    feedAdapter.submitData(it)
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            feedAdapter.loadStateFlow.collectLatest { loadState ->
                binding.apply {
                    when (loadState.refresh) {
                        is LoadState.Loading -> {
                            progressBar.visibility = View.VISIBLE
                            clEmptyState.visibility = View.GONE
                        }
                        is LoadState.NotLoading -> {
                            progressBar.visibility = View.GONE
                            clEmptyState.visibility = View.GONE
                        }
                        is LoadState.Error -> {
                            if (feedAdapter.itemCount == 0) {
                                progressBar.visibility = View.GONE
                                clEmptyState.visibility = View.VISIBLE
                                btnRetry.setOnClickListener {
                                    feedAdapter.retry()
                                }
                            } else {
                                progressBar.visibility = View.GONE
                                clEmptyState.visibility = View.GONE
                                Log.e("FeedFragment", "Error: ${loadState.refresh}")
                            }
                        }
                    }
                }
            }
        }
    }
}