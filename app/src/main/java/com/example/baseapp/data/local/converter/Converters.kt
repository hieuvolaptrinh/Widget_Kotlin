package com.example.baseapp.data.local.converter

import android.graphics.Matrix
import android.net.Uri
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Date
import kotlin.let

class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromFloatArray(value: FloatArray): String {
        return gson.toJson(value)
    }

    @TypeConverter
    fun toFloatArray(value: String): FloatArray {
        val listType = object : TypeToken<FloatArray>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromMatrix(matrix: Matrix?): FloatArray? {
        if (matrix == null) {
            return null
        }
        val matrixValues = FloatArray(9)
        matrix.getValues(matrixValues)
        return matrixValues
    }

    @TypeConverter
    fun toMatrix(matrixValues: FloatArray?): Matrix? {
        if (matrixValues == null || matrixValues.size != 9) {
            return null
        }
        val matrix = Matrix()
        matrix.setValues(matrixValues)
        return matrix
    }

    @TypeConverter
    fun fromUri(uri: Uri?): String? {
        return uri?.toString()
    }

    @TypeConverter
    fun toUri(uriString: String?): Uri? {
        return uriString?.let { Uri.parse(it) }
    }

    @TypeConverter
    fun fromDate(date: Date?): Long? = date?.time

    @TypeConverter
    fun toDate(timestamp: Long?): Date? =
        timestamp?.let { Date(it) }
}
