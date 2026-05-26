package com.example.baseapp.ui.base

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import androidx.fragment.app.DialogFragment
import androidx.viewbinding.ViewBinding
import com.example.baseapp.utils.ScreenUtils

abstract class BaseDialogFragment<VB : ViewBinding>: DialogFragment() {
    protected abstract fun getBindingInflater(
        inflater: LayoutInflater
    ): VB

    lateinit var binding: VB

    protected abstract fun initData(bundle: Bundle?)
    protected abstract fun initViews()
    protected abstract fun initActions()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = getBindingInflater(inflater)

        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
//
//        if (findNavController() != null) {
//            mNavController = findNavController()
//        }

        initData(arguments)
        initViews()
        initActions()
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        dialog?.setOnKeyListener { _, keyCode, event ->
            if (keyCode == KeyEvent.KEYCODE_BACK && event.action == KeyEvent.ACTION_UP) {
                dismiss()
                true
            } else {
                // Return false to allow the system to handle the back button press
                dismiss()
                false

            }
        }

        return super.onCreateDialog(savedInstanceState).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
//            setContentView(binding.root)
        }
    }

    override fun onStart() {
        super.onStart()
        setupDialogSize()
    }

    open fun setupDialogSize() {
        val screenWidth = ScreenUtils.getWidthScreen(requireContext())
        val marginHorizontal = 2 * ScreenUtils.dpToPx(requireContext(), 24f)
        val mWindow = dialog?.window

        if (mWindow != null) {
            val layoutParams: WindowManager.LayoutParams = mWindow.attributes
            layoutParams.width = (screenWidth - marginHorizontal).toInt()
            layoutParams.height = WindowManager.LayoutParams.WRAP_CONTENT

            mWindow.attributes = layoutParams
        }
    }
}