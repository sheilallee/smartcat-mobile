package com.application.smartcat.model.dados

import com.google.firebase.Timestamp
import kotlinx.datetime.Instant

fun Timestamp.toKotlinInstant(): Instant {
    return Instant.fromEpochSeconds(this.seconds, this.nanoseconds)
}




