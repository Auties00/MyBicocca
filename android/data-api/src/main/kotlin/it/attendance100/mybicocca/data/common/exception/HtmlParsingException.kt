package it.attendance100.mybicocca.data.common.exception

/**
 * Exception thrown when parsing of an HTML or JSON response fails.
 *
 * This covers cases such as missing expected elements, mismatched table
 * headers and rows, missing JavaScript variables, and failed data transforms.
 */
class HtmlParsingException(
    message: String
) : Exception(message)
