package com.example.baseapp.domain.usecase.widget

import com.example.baseapp.data.remote.Resource
import com.example.baseapp.data.remote.dto.WidgetPack
import com.example.baseapp.domain.repository.WidgetRepository
import javax.inject.Inject

class GetWidgetPackByIdUseCase @Inject constructor(
    private val repository: WidgetRepository
) {
    suspend operator fun invoke(id: String): Resource<WidgetPack> {
        return repository.getThemePacksById(id)
    }
}