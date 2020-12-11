package com.notebook.android.application

import android.app.Application
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.notebook.android.data.db.database.NotebookDatabase
import com.notebook.android.data.network.NetworkConnectionInterceptor
import com.notebook.android.data.network.NotebookApi
import com.notebook.android.ui.auth.factory.AuthViewModelFactory
import com.notebook.android.ui.auth.repository.AuthRepository
import com.notebook.android.ui.category.FilterCommonProductRepository
import com.notebook.android.ui.category.FilterCommonProductVMFactory
import com.notebook.android.ui.dashboard.factory.DashboardViewModelFactory
import com.notebook.android.ui.dashboard.searchProduct.SearchProdRepo
import com.notebook.android.ui.dashboard.searchProduct.SearchProductVMFactory
import com.notebook.android.ui.category.subCategory.SubCategProdRepo
import com.notebook.android.ui.category.subCategory.SubCategoryProductVMFactory
import com.notebook.android.ui.productDetail.DetailProductVMFactory
import com.notebook.android.ui.productDetail.ProdDetailRepo
import com.notebook.android.ui.category.productParts.BSLProductRepo
import com.notebook.android.ui.category.productParts.ProductVMFactory
import com.notebook.android.ui.dashboard.cart.CartRepo
import com.notebook.android.ui.dashboard.cart.CartVMFactory
import com.notebook.android.ui.dashboard.repository.DashboardRepo
import com.notebook.android.ui.drawerFrag.AboutUsVmFactory
import com.notebook.android.ui.drawerFrag.DrawerPartRepo
import com.notebook.android.ui.drawerFrag.DrawerPartVMFactory
import com.notebook.android.ui.filter.FilterRepo
import com.notebook.android.ui.filter.FilterVMFactory
import com.notebook.android.ui.merchant.MerchantRepo
import com.notebook.android.ui.merchant.MerchantVMFactory
import com.notebook.android.ui.myAccount.address.AddressRepo
import com.notebook.android.ui.myAccount.address.AddressVMFactory
import com.notebook.android.ui.myAccount.helpSupport.HelpSupportRepo
import com.notebook.android.ui.myAccount.helpSupport.HelpSupportVMFactory
import com.notebook.android.ui.myAccount.profile.ProfileRepo
import com.notebook.android.ui.myAccount.profile.ProfileVMFactory
import com.notebook.android.ui.myOrder.MyOrderRepo
import com.notebook.android.ui.myOrder.MyOrderVMFactory
import com.notebook.android.ui.orderSummary.OrderSummaryRepo
import com.notebook.android.ui.orderSummary.OrderSummaryVMFactory
import com.notebook.android.ui.splash.SplashRepository
import com.notebook.android.ui.splash.SplashViewModelFactory
import org.kodein.di.Kodein
import org.kodein.di.KodeinAware
import org.kodein.di.android.x.androidXModule
import org.kodein.di.generic.bind
import org.kodein.di.generic.instance
import org.kodein.di.generic.provider
import org.kodein.di.generic.singleton

class NoteBookApplication : Application(), KodeinAware {

    override val kodein = Kodein.lazy {
        import(androidXModule(this@NoteBookApplication))

        bind() from singleton { NetworkConnectionInterceptor(instance()) }
        bind() from singleton { NotebookApi(instance(), instance()) }
        bind() from singleton { NotebookDatabase(instance()) }
        bind() from singleton { AuthRepository(instance(), instance()) }
        bind() from singleton { DashboardRepo(instance(), instance()) }
        bind() from singleton { SplashRepository(instance(), instance()) }
        bind() from singleton { ProdDetailRepo(instance(), instance()) }
        bind() from singleton { BSLProductRepo(instance(), instance()) }
        bind() from singleton { SubCategProdRepo(instance(), instance()) }
        bind() from singleton { SearchProdRepo(instance(), instance()) }
        bind() from singleton { FilterRepo(instance(), instance()) }
        bind() from singleton { MerchantRepo(instance(), instance()) }
        bind() from singleton { DrawerPartRepo(instance(), instance()) }
        bind() from singleton { AddressRepo(instance(), instance()) }
        bind() from singleton { HelpSupportRepo(instance(), instance()) }
        bind() from singleton { MyOrderRepo(instance(), instance()) }
        bind() from singleton { FilterCommonProductRepository(instance(), instance()) }
        bind() from singleton { CartRepo(instance(), instance()) }
        bind() from singleton { ProfileRepo(instance(), instance()) }
        bind() from singleton { OrderSummaryRepo(instance(), instance()) }

        bind() from provider { SplashViewModelFactory(instance()) }
        bind() from provider { AuthViewModelFactory(instance()) }
        bind() from provider { DashboardViewModelFactory(instance()) }
        bind() from provider { DetailProductVMFactory(instance()) }
        bind() from provider { SubCategoryProductVMFactory(instance()) }
        bind() from provider { SearchProductVMFactory(instance()) }
        bind() from provider { ProductVMFactory(instance()) }
        bind() from provider { FilterVMFactory(instance()) }
        bind() from provider { AboutUsVmFactory(instance())}
        bind() from provider { MerchantVMFactory(instance()) }
        bind() from provider { DrawerPartVMFactory(instance()) }
        bind() from provider { AddressVMFactory(instance()) }
        bind() from provider { HelpSupportVMFactory(instance()) }
        bind() from provider { MyOrderVMFactory(instance()) }
        bind() from provider { FilterCommonProductVMFactory(instance()) }
        bind() from provider { CartVMFactory(instance()) }
        bind() from provider { ProfileVMFactory(instance()) }
        bind() from provider { OrderSummaryVMFactory(instance()) }
    }

    override fun onCreate() {
        super.onCreate()

        FacebookSdk.sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(this)
    }
}