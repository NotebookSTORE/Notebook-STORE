package com.notebook.android.application

import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import android.util.Base64
import android.util.Log
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.*
import kotlin.collections.ArrayList

/**
 * This is a helper class to generate your message hash to be included in your SMS message.
 *
 * Without the correct hash, your app won't recieve the message callback. This only needs to be
 * generated once per app and stored. Then you can remove this helper class from your code.
 *
 * For More Detail: https://developers.google.com/identity/sms-retriever/verify#computing_your_apps_hash_string
 *
 */

class AppSignatureHelper(private val context: Context) : ContextWrapper(context) {

    companion object {
        val TAG = AppSignatureHelper::class.java.simpleName

        private const val HASH_TYPE = "SHA-256";
        const val NUM_HASHED_BYTES = 9;
        const val NUM_BASE64_CHAR = 11;
    }

    /**
     * Get all the app signatures for the current package
     * @return
     */
    fun getAppSignatures(): ArrayList<String> {
        val appCodes = ArrayList<String>();

        try {
            // Get all package signatures for the current package
            val signatures = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNATURES
            ).signatures;

            // For each signature create a compatible hash
            for (signature in signatures) {
                val hash = hash(packageName, signature.toCharsString());
                if (hash != null) {
                    appCodes.add(String.format("%s", hash));
                }
            }
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e(TAG, "Unable to find package to obtain hash.", e);
        }
        return appCodes;
    }

    private fun hash(packageName: String, signature: String): String? {
        val appInfo = "$packageName $signature";
        try {
            val messageDigest = MessageDigest.getInstance(HASH_TYPE);
            messageDigest.update(appInfo.toByteArray(StandardCharsets.UTF_8))
            var hashSignature = messageDigest.digest();

            // truncated into NUM_HASHED_BYTES
            hashSignature = Arrays.copyOfRange(hashSignature, 0, NUM_HASHED_BYTES)
            // encode into Base64
            var base64Hash = Base64.encodeToString(hashSignature, Base64.NO_PADDING or Base64.NO_WRAP)
            base64Hash = base64Hash.substring(0, NUM_BASE64_CHAR);

            Log.e(TAG, String.format("pkg: %s -- hash: %s", packageName, base64Hash))
            return base64Hash;
        } catch (e: NoSuchAlgorithmException) {
            Log.e(TAG, "hash:NoSuchAlgorithm", e);
        }
        return null;
    }
}