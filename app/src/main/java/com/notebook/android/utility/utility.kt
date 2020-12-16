package com.max.ecomaxgo.maxpe.view.flight.utility

import android.app.Activity
import android.content.Context
import android.graphics.Paint
import android.text.Spanned
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.webkit.WebView
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.getSystemService
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import com.notebook.android.R
import com.notebook.android.utility.Constant.MERCHANT_IMAGE_PATH
import kotlinx.android.synthetic.main.custom_toast_layout.view.*
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern
import kotlin.math.roundToInt

fun Context.toastShow(msg:String) = Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()

fun ProgressBar.showLoading(){
    visibility = View.VISIBLE
}

fun ProgressBar.hideLoading(){
    visibility = View.GONE
}

fun showErrorToast(context: Context, message: String) {
    val parent: ViewGroup? = null
    val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val toastView = inflater.inflate(R.layout.custom_toast_layout, parent)
    toastView.custom_toast_message.text = message
    toast.view = toastView
    toast.show()
}

fun Context.showPermissionExplaination(message: String, requestPermission: () -> Unit) {
    val dialog = AlertDialog.Builder(this)
    dialog.setMessage(message)
    dialog.setPositiveButton("OK") { dialogInterface, _ ->
        requestPermission()
        dialogInterface.dismiss()
    }

    dialog.show()
}

fun showSuccessToast(context: Context, message: String) {
    val parent: ViewGroup? = null
    val toast = Toast.makeText(context, "", Toast.LENGTH_LONG)
    val inflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val toastView = inflater.inflate(R.layout.custom_toast_layout, parent)
    toastView.custom_toast_message.text = message
    toast.view = toastView
    toast.show()
}

fun View.showSnackBarWithAction(message:String){
    Snackbar.make(this, message, Snackbar.LENGTH_LONG).also {snackbar ->
        snackbar.setAction("OK"){
            snackbar.dismiss()
        }
    }.show()
}

fun Context.showKeyboard(){
    val imm = this.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0)
}

fun View.showSnackBar(message:String){
    Snackbar.make(this, message, Snackbar.LENGTH_SHORT).also {snackbar ->
        /*snackbar.setAction("OK"){
            snackbar.dismiss()
        }*/
        snackbar.setTextColor(resources.getColor(R.color.colorAccent))
        snackbar.setBackgroundTint(resources.getColor(R.color.colorWhite))
    }.show()
}

fun validateEmail(email: String): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val EMAIL_PATTERN = "^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$"
    pattern = Pattern.compile(EMAIL_PATTERN)
    matcher = pattern.matcher(email)
    return matcher.matches()
}


fun getUserImageFullPath(img:String) : String{
    return "$MERCHANT_IMAGE_PATH$img"
}

@BindingAdapter(value = ["loadImageBasePath", "imageName"])
fun loadAllTypeImage(imgView: ImageView, loadImageBasePath:String, imageName:String?){
//    val imgBasePah = "https://demo.mbrcables.com/stationarykingdom/public/uploads/product/"
    if(!imageName.isNullOrEmpty()){
        Glide.with(imgView.context).load("$loadImageBasePath$imageName").into(imgView)
    }else{
        Glide.with(imgView.context).load(R.drawable.note_pad).into(imgView)
    }
}

@BindingAdapter("orderIDSet")
fun setOrderIDText(textView: TextView, orderID:String){
    textView.text = "Order ID : $orderID"
}

@BindingAdapter(value = ["price", "discount"])
fun getPercentageOfAmount(textView:TextView, price:Float, discount:Int){
    val result = (price.times(discount)).div(100f)
    Log.e("priceCalculation", " :: ${price} :: ${discount} :: ${result} :: ${price-result}")
    textView.text = "₹${(price-result)}"
}


/*Here are character code to these different style of bullets: • = \u2022,
 ● = \u25CF, ○ = \u25CB, ▪ = \u25AA, ■ = \u25A0, □ = \u25A1, ► = \u25BA*/

@BindingAdapter("keyFeature")
fun arrangeKeyFeature(textView: TextView, feature:List<String>?){

//    Log.e("keyFeature", "${feature[0]} :: ${feature[1]} :: ${feature[2]}")
    val keyValue = StringBuilder()

    if(feature?.isNotEmpty() == true){
        for(element in feature){
            keyValue.append("\u25CF").append("      ").append(element).append("\n\n")
        }
    }
    textView.text = keyValue
}

@BindingAdapter("dataSheet")
fun setDataSheet(webView: WebView, htmlString: String?){
    webView.loadData(htmlString ?: "", "text/html; charset=utf-8", "UTF-8")
}

@BindingAdapter("textStrikeThrough")
fun setTextStrikeThrough(textview:TextView, offerPrice:String){
    textview.text = offerPrice
    textview.paintFlags =
        textview.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
}

@BindingAdapter(value = ["countryName", "countryCode"])
fun setCountryText(textView:TextView, countryName:String, countryCode:String){
    textView.text = "$countryName ($countryCode)"
}

@BindingAdapter(value = ["prodTitle", "colorCode"])
fun setProductContent(textView:TextView, prodTitle:String?, colorCode:String?){
    if(!prodTitle.isNullOrEmpty()){
        textView.text = "$prodTitle ($colorCode)"
    }else{
        textView.text = ""
    }

}

@BindingAdapter("prodRating")
fun setProductRating(textView:TextView, rating:Float?){
    if (rating != null){
        textView.text = "$rating"
    }else{
        textView.text = "4.0"
    }
}

@BindingAdapter("prodReviewCount")
fun setProductRating(textView:TextView, reviewCount:Int?){
    if (reviewCount != 0){
        textView.text = "(${reviewCount} Review)"
    }else{
        textView.text = "(1 Review)"
    }
}

@BindingAdapter(value= ["rating", "reviewCount"])
fun setProdRatingAndReview(textView:TextView, rating:Float?, reviewCount: Int?){
    if (rating != null){
        textView.text = "$rating Rating and $reviewCount Review"
    }else{
        textView.text = "4.0 Rating and 1 Review"
    }
}

@BindingAdapter("dateConverter")
fun formatStringDateToStandard(textview:TextView, date:String?){
//    2020-05-16 16:12:06
    if(date.isNullOrEmpty()){
        textview.text = ""
    }else{
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val dateParse = dateFormat.parse(date)//You will get date object relative to server/client timezone wherever it is parsed
        val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()); //If you need time just put specific format for time like 'HH:mm:ss'
        val convertedDate = formatter.format(dateParse!!)
        textview.text = convertedDate
    }
}

@BindingAdapter("orderedDate")
fun orderDeliveryDate(textview:TextView, date:String?){
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    val formatter = SimpleDateFormat("dd MMM, yyyy", Locale.getDefault()) //If you need time just put specific format for time like 'HH:mm:ss'
    if(!date.isNullOrEmpty()){
        val dateParse = dateFormat.parse(date)//You will get date object relative to server/client timezone wherever it is parsed
        val convertedDate = formatter.format(dateParse!!)
        textview.text = "Delivery on ${convertedDate}"
    }else{
        textview.text = ""
    }
}