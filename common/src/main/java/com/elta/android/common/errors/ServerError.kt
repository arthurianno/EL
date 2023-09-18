package com.elta.android.common.errors

import java.io.IOException

open class ServerError(message: String) : IOException(message)
