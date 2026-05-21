package it.attendance100.mybicocca.data.local.elearning.sync

import kotlinx.serialization.json.Json

internal val ElearningJson: Json = Json {
    ignoreUnknownKeys = true
    encodeDefaults = true
    isLenient = true
}
