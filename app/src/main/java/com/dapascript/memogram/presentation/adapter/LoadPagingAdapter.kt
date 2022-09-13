package com.dapascript.memogram.presentation.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import com.dapascript.memogram.databinding.ItemLoadPagingBinding

class LoadPagingAdapter(
    private val retry: () -> Unit
) : LoadStateAdapter<LoadPagingAdapter.LoadingViewHolder>() {

    override fun onBindViewHolder(holder: LoadingViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoadingViewHolder {
        return LoadingViewHolder(
            ItemLoadPagingBinding.inflate(LayoutInflater.from(parent.context), parent, false),
            retry
        )
    }

    inner class LoadingViewHolder(private val binding: ItemLoadPagingBinding, retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                binding.errorMsg.text = loadState.error.localizedMessage
            }
            binding.apply {
                progressBar.isVisible = loadState is LoadState.Loading
                errorMsg.isVisible = loadState is LoadState.Error
                retryButton.isVisible = loadState is LoadState.Error
            }
        }

        init {
            binding.retryButton.setOnClickListener { retry.invoke() }
        }
    }
}