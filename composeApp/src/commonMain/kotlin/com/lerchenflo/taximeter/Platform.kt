package com.lerchenflo.taximeter

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform