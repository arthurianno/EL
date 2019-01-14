package com.nullgr.android.presentation.core.pm.factory

class PmProviderNotFoundException(clazz: Class<*>) :
    NoSuchElementException("There is no provider for ${clazz.simpleName}. " +
        "Maybe you forgot bind this view model at PmModule.")