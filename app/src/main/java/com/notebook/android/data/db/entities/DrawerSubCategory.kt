package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.notebook.android.data.db.typeConverter.DrawerObjectConverter

@TypeConverters(DrawerObjectConverter::class)
@Entity(tableName = "DrawerSubCategory", indices = [Index(value = ["id"], unique = true)])
data class DrawerSubCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var category_id: Int,
    var title: String? = null,
    var image: String? = null,
    var status: Int? = null,
    var alias: String? = null,
    var popular:String ?= null,
    var mobile_popular:String ?= null,
    var updated_at: String? = null,
    var deleted_at:String ?= null,
    var created_at:String ?= null,
    var homeimage: String? = null,
    var homeimage_mobile:String ?= null,
    var description:String ?= null,
    var isDrawerSubCategoryOpen:Boolean = false,
    var isDrawerSubCategArrowOpen:Boolean = false,
    var subsubcategory:List<DrawerSubSubCategory> ?= null
) {
}