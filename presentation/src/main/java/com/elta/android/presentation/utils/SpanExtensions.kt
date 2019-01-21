package com.elta.android.presentation.utils

import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.TextView
import com.nullgr.core.font.toSpannable
import com.nullgr.core.font.withSpan
import io.reactivex.Observable

fun TextView.clickableSpan(spanText: String, fullText: String? = null): Observable<Unit> {
    val spannable = fullText?.toSpannable() ?: text.toSpannable()
    val startIndex = spannable.indexOf(spanText)
    movementMethod = LinkMovementMethod.getInstance()
    return Observable.create<Unit> {
        if (startIndex == -1) it.onError(IllegalArgumentException("Text $spanText not found"))
        text = spannable.withSpan {
            setSpan(object : ClickableSpan() {
                override fun onClick(widget: View) {
                    it.onNext(Unit)
                }
            }, startIndex, startIndex + spanText.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        }
    }
}