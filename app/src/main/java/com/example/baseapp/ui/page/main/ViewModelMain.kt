package com.example.baseapp.ui.page.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.domain.usecase.widget.container.WidgetUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ViewModelMain @Inject constructor(
    private val widgetUseCases: WidgetUseCases
) : ViewModel() {

    private val _widgetPack = MutableLiveData<Resource<WidgetPack>>()
    val widgetPack: LiveData<Resource<WidgetPack>> = _widgetPack

    fun loadWidgetPack(id: String) {
        _widgetPack.value = Resource.Loading()
        viewModelScope.launch {
            _widgetPack.value = widgetUseCases.getWidgetPackByIdUseCase(id)
        }
    }

    // hàm để gắn widget vào giao diện sẽ được viết ở đây
}