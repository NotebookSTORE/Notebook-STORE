package com.notebook.android.data.db.entities

import androidx.room.*
import com.notebook.android.data.db.typeConverter.DrawerObjectConverter

@TypeConverters(DrawerObjectConverter::class)
@Entity(tableName = "DrawerCategory", indices = [Index(value = ["id"], unique = true)])
data class DrawerCategory(
    @PrimaryKey(autoGenerate = false)
    var id: Int,
    var title: String,
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
    var isDrawerCategoryOpen:Boolean = false,
    var isDrawerCategArrowOpen:Boolean = false,
    var subsubcategorys:List<DrawerSubCategory> ?= null
)