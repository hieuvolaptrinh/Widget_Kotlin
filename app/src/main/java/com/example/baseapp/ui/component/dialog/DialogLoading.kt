package com.example.baseapp.ui.component.dialog

import android.os.Bundle
import android.view.LayoutInflater
import com.example.baseapp.databinding.DialogLoadingBinding
import com.example.baseapp.ui.base.BaseDialogFragment

class DialogLoading : BaseDialogFragment<DialogLoadingBinding>() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        setStyle(DialogFragment.STYLE_NORMAL, R.style.FullscreenDialogTheme)
        isCancelable = false
    }

    override fun getBindingInflater(inflater: LayoutInflater): DialogLoadingBinding {
        return DialogLoadingBinding.inflate(inflater)
    }

    override fun initData(bundle: Bundle?) {
    }

    override fun initViews() {
    }

    override fun initActions() {
    }

    override fun dismiss() {
        if (isAdded) {
            super.dismiss()
        }
    }


}