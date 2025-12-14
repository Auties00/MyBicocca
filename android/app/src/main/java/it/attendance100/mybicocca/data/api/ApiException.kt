package it.attendance100.mybicocca.data.api

import java.io.*

open class ApiException(val code: Int, message: String) : IOException(message)

class ExpiredJWTApiException(code: Int, message: String) : ApiException(code, message)
