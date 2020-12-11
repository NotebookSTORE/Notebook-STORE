package com.notebook.android.data.db.entities

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

/*(tableName = "User", indices = [Index(value = ["id"], unique = true)])*/
@Entity
data class User(
    @NotNull
    @PrimaryKey(autoGenerate = false)
    var id:Int,
    var usertype:Int ?= null,
    var registerfor:Int ?= null,
    var institute_name:String ?= null,
    var profile_image:String ?= null,
    var email:String ?= null,
    var gender:String ?= null,
    var is_verified:Int?=null,
    var is_admin:Int ?= null,
    var name:String ?= null,
    var username:String ?= null,
    var merchant_id:String ?= null,
    var dob:String ?= null,
    var phone:String ?= null,
    var address:String ?= null,
    var identity_detail:String ?= null,
    var identity_image:String ?= null,
    var pancardno:String ?= null,
    var token:String ?= null,
    var pancardimage:String ?= null,
    var cancled_cheque_image:String ?= null,
    var accountno:String ?= null,
    var bankname:String ?= null,
    var ifsccode:String ?= null,
    var upi:String ?= null,
    var banklocation:String ?= null,
    var referralcode:String ?= null,
    var referral_id:Int ?= null,
    var status:Int ?= null, //this is merchant status
var imageupdated:Int ?= null,
var wallet_amounts:String ?= null,
var otp:String?=null)