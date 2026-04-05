package it.attendance100.mybicocca.util.shared_transitions

data class TaxesSharedElementKey(
    val taxId: String,
    val type: TaxesSharedElementType,
) : SharedElementKey {
    override val id: String = "tax-$taxId-${type.name}"
}

enum class TaxesSharedElementType {
    Title,
    Status,
    Amount,
    Description,
    ExpiryDateText,
    ExpiryDate,
}
