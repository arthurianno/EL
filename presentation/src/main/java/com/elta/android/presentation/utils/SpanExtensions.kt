package com.elta.android.presentation.utils

import android.text.Html
import android.text.SpannableString
import android.text.Spanned
import android.text.TextUtils
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.nullgr.core.font.toSpannable
import com.nullgr.core.font.withSpan
import io.reactivex.Observable
import io.reactivex.disposables.Disposables

fun TextView.clickableSpan(spanText: String, fullText: String? = null): Observable<Unit> {
    val spannable = fullText?.toSpannable() ?: text.toSpannable()

    // Normalize text for search: replace newlines and multiple spaces with single space
    val normalizedSpannable = spannable.toString().replace(Regex("\\s+"), " ")
    val normalizedSpanText = spanText.replace(Regex("\\s+"), " ")
    val startIndex = normalizedSpannable.indexOf(normalizedSpanText)

    movementMethod = LinkMovementMethod.getInstance()
    return Observable.create<Unit> {
        if (startIndex == -1) {
            it.onError(IllegalArgumentException("Text '$normalizedSpanText' not found in '$normalizedSpannable'"))
            return@create
        }

        val span = object : ClickableSpan() {
            override fun onClick(widget: View) {
                if (!it.isDisposed) it.onNext(Unit)
            }
        }

        val ss = SpannableString(text)
        TextUtils.copySpansFrom(spannable, 0, ss.length, null, ss, 0)

        text = ss.withSpan {
            setSpan(
                span,
                startIndex,
                startIndex + normalizedSpanText.length,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
            )
        }

        it.setDisposable(
            Disposables.fromAction {
                spannable.removeSpan(span)
            }
        )
    }
}

var TextView.htmlText: CharSequence?
    get() = text
    set(value) {
        text = Html.fromHtml(value.toString())
    }
