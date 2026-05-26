package com.example.baseapp.ui.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.example.baseapp.data.local.pref.Preference

abstract class BaseFragment<VB : ViewBinding> : Fragment() {
    var isLoaded = false
    protected abstract fun getBindingInflater(
        inflater: LayoutInflater,
        container: ViewGroup?,
        attachToParent: Boolean
    ): VB

    protected abstract fun initData(bundle: Bundle?)
    protected abstract fun initViews()
    protected abstract fun initActions()
    protected abstract fun initObserver()

    lateinit var binding: VB

    private var backPressedTime: Long = 0
    val preference = Preference.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = getBindingInflater(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

//        mNavController = findNavController()
        if(!isLoaded){
            initData(arguments)
            initViews()
            initActions()
            initObserver()
            isLoaded = true
        }

    }

    override fun onResume() {
        super.onResume()
//        if(!isLoaded){
//            initData(arguments)
//            initViews()
//            initActions()
//            initObserver()
//            isLoaded = true
//        }
    }

    override fun onPause() {
        super.onPause()
    }

    override fun onDestroyView() {
        super.onDestroyView()
    }

    fun showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
        Toast.makeText(
            context,
            message,
            duration
        ).show()
    }
}
