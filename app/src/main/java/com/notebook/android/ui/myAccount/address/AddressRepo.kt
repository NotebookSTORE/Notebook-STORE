package com.notebook.android.ui.myAccount.address

import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.db.entities.Address
import com.notebook.android.data.db.entities.Country
import com.notebook.android.data.db.entities.User
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.data.network.SafeApiRequest
import com.notebook.android.model.address.AddAddress
import com.notebook.android.model.address.CountryData
import com.notebook.android.model.address.FetchAddresses
import com.notebook.android.model.address.MakeDefaultAddress
import com.notebook.android.model.cashfree.CFTokenResponse
import com.notebook.android.model.orderSummary.AfterPaymentRawData
import com.notebook.android.model.orderSummary.PaymentSuccesData
import com.notebook.android.model.wallet.AddWallet
import com.notebook.android.model.wallet.AddWalletResponse
import com.notebook.android.model.wallet.WalletAmountRaw
import com.notebook.android.model.wallet.WalletAmountResponse

class AddressRepo(
    val db:NotebookDatabase,
    val apiService:NotebookApi
) : SafeApiRequest() {

    suspend fun getCountryDataFromServer() : CountryData{
        return apiRequest { apiService.getCountryData() }
    }

    suspend fun addAddressToServer(userID:Int, token:String, street:String,
                                   locality:String, state:String, pincode:String,
                                   country:String, city:String) : AddAddress {
        return apiRequest { apiService.addMultiAddressToServer(userID, token, street, locality, state, pincode, country, city)}
    }

    suspend fun updateAddressToServer(userID:Int, token:String, street:String,
                                   locality:String, state:String, pincode:String,
                                   country:String, city:String, multiAddressID:Int) : FetchAddresses {
        return apiRequest { apiService.updateMultiAddress(userID, token, street, locality,
            state, pincode, country, city, multiAddressID)}
    }

    suspend fun makeDefaultAddressThroughServer(userID:Int, token:String, multiAddressID:Int) : MakeDefaultAddress{
        return apiRequest { apiService.makeDefaultAddress(userID, token, multiAddressID) }
    }

    suspend fun deleteAddressThroughServer(userID:Int, token:String, multiAddressID:Int) : FetchAddresses{
        return apiRequest { apiService.deleteMultiAddress(userID, token, multiAddressID) }
    }

    suspend fun fetchAddressesFromServer(userID:Int, token:String) : FetchAddresses{
        return apiRequest { apiService.fetchAddressFromServer(userID, token) }
    }

    suspend fun insertAllAddressToDB(addrList:List<Address>) = db.getAddressDao().insertAllAddressData(addrList)
    fun getAllAddressesFromDB() = db.getAddressDao().getAllAddressData()

    suspend fun insertAllCountryToDB(countryList:List<Country>) = db.getAddressDao().insertAllCountryData(countryList)
    fun getAllCountryFromDB() = db.getAddressDao().getAllCountryData()

    //get user detail...
    fun getUser() = db.getUserDao().getUser()
    suspend fun updateUser(user: User) = db.getUserDao().updateUser(user)
    suspend fun deleteAddressData(addrID:Int) = db.getAddressDao().deleteAddress(addrID)


    suspend fun deleteUser() = db.getUserDao().deleteUser()
    suspend fun clearCartTable() = db.getDetailProdDao().clearCartTable()
    suspend fun clearAddressTable() = db.getAddressDao().clearAddressTable()
    suspend fun clearOrderTable() = db.getOrderHistoryDao().clearOrderHistoryTable()
    suspend fun clearFavouriteTable() = db.getWishlistDao().clearWishlistTable()
}