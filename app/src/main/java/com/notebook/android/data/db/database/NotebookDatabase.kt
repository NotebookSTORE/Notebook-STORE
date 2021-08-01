package com.notebook.android.data.db.database

import android.content.Context
import androidx.room.*
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.notebook.android.data.db.dao.*
import com.notebook.android.data.db.entities.*
import com.notebook.android.data.db.typeConverter.Converters

@Database(
    entities = [User::class, Banner::class, Category::class, SubCategory::class,
        LatestProductHome::class, BestSellerHome::class, Brand::class, LatestOffer::class, MerchantBanner::class,
        DiscountedProduct::class, BestSeller::class, LatestProduct::class,
        BrandFilterBy::class, ColorFilterBy::class, CouponFilterBy::class, Cart::class, FilterProduct::class,
        DiscountFilterBy::class, PriceFilterBy::class, RatingFilterBy::class, SearchProduct::class, SubCategoryProduct::class,
        SubSubCategory::class, DrawerCategory::class, DrawerSubCategory::class, DrawerSubSubCategory::class, CategoryProduct::class,
        HomeSubSubCategoryProduct::class, CouponApply::class, FaqExpandable::class, Wishlist::class, RatingReviews::class,
        Country::class, Address::class, OrderHistory::class, ProductDetailEntity::class, MerchantBenefit::class],
    version = 6, exportSchema = false)

@TypeConverters(Converters::class)
abstract class NotebookDatabase : RoomDatabase() {
    abstract fun getUserDao():UserDao
    abstract fun getHomeDao():HomeDao
    abstract fun getDetailProdDao():ProductDetail
    abstract fun getProductDao():ProductDao
    abstract fun getFilterDao():FilterDao
    abstract fun getCategoryDao():CategoryDao
    abstract fun getDrawerDao():DrawerDao
    abstract fun getWishlistDao():WishlistDao
    abstract fun getAddressDao() : AddressDao
    abstract fun getOrderHistoryDao() : OrderHistoryDao
    abstract fun getMerchantDao() : MerchantDao

    companion object{
        @Volatile
        private var instance:NotebookDatabase ?= null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance?: buildDatabase(context).also {
                instance = it
            }
        }

        //    For version from 1 to 2
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {


                database.execSQL("CREATE TABLE IF NOT EXISTS `${DrawerSubCategory::class}` " +
                        "(`id` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))")

                database.execSQL("CREATE TABLE IF NOT EXISTS `${DrawerSubSubCategory::class}` " +
                        "(`id` TEXT NOT NULL, " +
                        "PRIMARY KEY(`id`))")
                /* //Integer values
                 database.execSQL(
                     "ALTER TABLE TarunidhiSurveyData "
                             + " ADD COLUMN dummy INTEGER default 0 NOT NULL"
                 )*/
                //String values
                /*database.execSQL(
                    "ALTER TABLE 'TarunidhiSurveyData' ADD COLUMN 'empID' TEXT"
                )*/

                /*val TABLE_NAME_TEMP = "GameNew"

        // 1. Create new table
        database.execSQL("CREATE TABLE IF NOT EXISTS `$TABLE_NAME_TEMP` " +
                "(`game_name` TEXT NOT NULL, " +
                "PRIMARY KEY(`game_name`))")

        // 2. Copy the data
        database.execSQL("INSERT INTO $TABLE_NAME_TEMP (game_name) "
                + "SELECT game_name "
                + "FROM $TABLE_NAME")

        // 3. Remove the old table
        database.execSQL("DROP TABLE $TABLE_NAME")

        // 4. Change the table name to the correct one
        database.execSQL("ALTER TABLE $TABLE_NAME_TEMP RENAME TO $TABLE_NAME")*/
            }
        }

        //    For version from 1 to 2
        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {


                database.execSQL(
                    "ALTER TABLE 'ProductDetailEntity' ADD COLUMN 'return_days' INTEGER default 0 NOT NULL"
                )
                /* //Integer values
                 database.execSQL(
                     "ALTER TABLE ProductDetailEntity "
                             + " ADD COLUMN return_days INTEGER default 0 NOT NULL"
                 )*/
                //String values
                /*database.execSQL(
                    "ALTER TABLE 'TarunidhiSurveyData' ADD COLUMN 'empID' TEXT"
                )*/

                /*val TABLE_NAME_TEMP = "GameNew"

        // 1. Create new table
        database.execSQL("CREATE TABLE IF NOT EXISTS `$TABLE_NAME_TEMP` " +
                "(`game_name` TEXT NOT NULL, " +
                "PRIMARY KEY(`game_name`))")

        // 2. Copy the data
        database.execSQL("INSERT INTO $TABLE_NAME_TEMP (game_name) "
                + "SELECT game_name "
                + "FROM $TABLE_NAME")

        // 3. Remove the old table
        database.execSQL("DROP TABLE $TABLE_NAME")

        // 4. Change the table name to the correct one
        database.execSQL("ALTER TABLE $TABLE_NAME_TEMP RENAME TO $TABLE_NAME")*/
            }
        }
        //    For version from 1 to 2
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(database: SupportSQLiteDatabase) {


                database.execSQL(
                    "ALTER TABLE 'User' ADD COLUMN 'origincode' TEXT default \"null\""
                )
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(context.applicationContext,
            NotebookDatabase::class.java, "notebookStoreDB.db")
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4) //Only update the schema much recomonded
            .fallbackToDestructiveMigration() //will delete all existing data from device and update new schema
            .build()
    }
}