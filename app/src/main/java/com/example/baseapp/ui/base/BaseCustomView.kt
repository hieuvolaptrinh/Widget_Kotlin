package com.example.baseapp.ui.base

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.viewbinding.ViewBinding

abstract class BaseCustomView<VB : ViewBinding> @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : FrameLayout(context, attrs) {
    protected abstract fun getBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): VB
    abstract fun initView()
    abstract fun initData()
    abstract fun initAction()
    var binding: VB = getBindingInflater(LayoutInflater.from(context), this, true)
    override fun onFinishInflate() {
        super.onFinishInflate()
        initView()
        initData()
        initAction()
    }
}