package com.dapascript.memogram.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.dapascript.memogram.R
import com.google.android.material.snackbar.Snackbar

fun getSnackBar(context: Context, view: View, message: String, button: CardView) {
    val snackBar = Snackbar.make(
        view,
        message,
        Snackbar.LENGTH_LONG
    ).apply {
        anchorView = button
        setBackgroundTint(
            ContextCompat.getColor(
                context,
                R.color.red
            )
        )
    }
    val layoutParams = snackBar.view.layoutParams as ViewGroup.MarginLayoutParams
    layoutParams.setMargins(60, 0, 60, 60)
    snackBar.view.layoutParams = layoutParams
    snackBar.show()
}