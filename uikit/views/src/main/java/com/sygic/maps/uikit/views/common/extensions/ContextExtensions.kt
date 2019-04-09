package com.sygic.maps.uikit.views.common.extensions

import android.content.*
import android.content.Context.CLIPBOARD_SERVICE
import android.content.Context.LOCATION_SERVICE
import android.location.LocationManager
import android.net.Uri
import android.text.TextUtils
import android.util.TypedValue
import android.view.View
import android.widget.Toast
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import androidx.annotation.StringRes
import androidx.annotation.StyleRes
import androidx.appcompat.app.AppCompatActivity
import com.sygic.maps.uikit.views.R

fun Context.applyStyle(@StyleRes resId: Int, force: Boolean = false) {
    theme.applyStyle(resId, force)
}

@ColorInt
fun Context.getColorFromAttr(@AttrRes resId: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): Int {
    typedValue.let {
        theme.resolveAttribute(resId, it, resolveRefs)
        return it.data
    }
}

fun Context.getStringFromAttr(@AttrRes resId: Int, typedValue: TypedValue = TypedValue(), resolveRefs: Boolean = true): String {
    typedValue.let {
        theme.resolveAttribute(resId, it, resolveRefs)
        if (it.type == TypedValue.TYPE_STRING && it.string != null) {
            return it.string.toString()
        }
        return EMPTY_STRING
    }
}

fun Context.openActivity(targetClass: Class<out AppCompatActivity>) {
    startActivity(Intent(this, targetClass))
}

fun Context.isRtl(): Boolean {
    return this.resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL
}

fun Context.openUrl(url: String) {
    if (!TextUtils.isEmpty(url)) {
        var parsedUrl = url
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            parsedUrl = "http://$url"
        }

        try {
            startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(parsedUrl)))
        } catch (e: ActivityNotFoundException) {
            longToast(R.string.no_browser_client)
        }
    }
}

fun Context.openEmail(mailto: String) {
    if (!TextUtils.isEmpty(mailto)) {
        val emailIntent = Intent(Intent.ACTION_SENDTO)

        emailIntent.data = Uri.parse("mailto:$mailto")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(mailto))
        emailIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        try {
            startActivity(Intent.createChooser(emailIntent, getString(R.string.send_email)))
        } catch (e: ActivityNotFoundException) {
            longToast(R.string.no_email_client)
        }
    }
}

fun Context.openPhone(phoneNumber: String) {
    if (!TextUtils.isEmpty(phoneNumber)) {
        val phoneIntent = Intent(Intent.ACTION_DIAL)

        phoneIntent.data = Uri.parse("tel:$phoneNumber")

        try {
            startActivity(Intent.createChooser(phoneIntent, null))
        } catch (e: ActivityNotFoundException) {
            longToast(R.string.no_phone_client)
        }
    }
}

fun Context.copyToClipboard(text: String) {
    clipboardManager?.let {
        it.primaryClip = ClipData.newPlainText(text, text)
        longToast(R.string.copied_to_clipboard)
    }
}

fun Context.shortToast(@StringRes text: Int) {
    Toast.makeText(this, text, Toast.LENGTH_SHORT).show()
}

fun Context.longToast(@StringRes text: Int) {
    Toast.makeText(this, text, Toast.LENGTH_LONG).show()
}

inline val Context.locationManager: LocationManager?
    get() = getSystemService(LOCATION_SERVICE) as? LocationManager

inline val Context.clipboardManager: ClipboardManager?
    get() = getSystemService(CLIPBOARD_SERVICE) as? ClipboardManager