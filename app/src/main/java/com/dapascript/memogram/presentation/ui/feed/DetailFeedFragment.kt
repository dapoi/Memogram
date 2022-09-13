package com.dapascript.memogram.presentation.ui.feed

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.dapascript.memogram.databinding.FragmentDetailFeedBinding
import com.dapascript.memogram.presentation.ui.MainActivity
import com.dapascript.memogram.utils.formatDate

class DetailFeedFragment : Fragment() {

    private lateinit var binding: FragmentDetailFeedBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentDetailFeedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.apply {
            toolbar.setNavigationOnClickListener { findNavController().popBackStack() }

            val photo = arguments?.getString("photo")
            val name = arguments?.getString("name")
            val date = arguments?.getString("date")
            val desc = arguments?.getString("description")

            Glide.with(this@DetailFeedFragment)
                .load(photo)
                .into(ivDetail)
            tvName.text = name
            tvDate.text = date?.let { formatDate(it) }
            tvDesc.text = desc
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (activity as MainActivity).hideBottomNavigationView()
    }

    override fun onDetach() {
        super.onDetach()
        (activity as MainActivity).showBottomNavigationView()
    }
}