package com.notebook.android.data.db.typeConverter

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.notebook.android.data.db.entities.DrawerSubCategory
import com.notebook.android.data.db.entities.DrawerSubSubCategory
import java.lang.reflect.Type
import java.util.*

class DrawerObjectConverter {
    var gson = Gson()

    //Category into list converter....
    @TypeConverter
    fun stringToDrawerSubCategory(data: String?): List<DrawerSubCategory?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<DrawerSubCategory?>?>() {}.getType()
        return gson.fromJson<List<DrawerSubCategory?>>(data, listType)
    }

    @TypeConverter
    fun drawerSubCategoryToString(someObjects: List<DrawerSubCategory?>?): String? {
        return gson.toJson(someObjects)
    }

    @TypeConverter
    fun stringToDrawerSubSubCategory(data: String?): List<DrawerSubSubCategory?>? {
        if (data == null) {
            return Collections.emptyList()
        }
        val listType: Type = object : TypeToken<List<DrawerSubSubCategory?>?>() {}.getType()
        return gson.fromJson<List<DrawerSubSubCategory?>>(data, listType)
    }

    @TypeConverter
    fun drawerSubSubCategoryToString(someObjects: List<DrawerSubSubCategory?>?): String? {
        return gson.toJson(someObjects)
    }
}