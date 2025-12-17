package it.attendance100.mybicocca.data.dto.elearning
import com.google.gson.annotations.SerializedName

data class GetCategoriesRequest(
    @SerializedName("criteria") val criteria: List<CategoryCriteria>? = null,
    @SerializedName("addsubcategories") val addSubcategories: Boolean? = null
)

data class CategoryCriteria(
    @SerializedName("key") val key: String,
    @SerializedName("value") val value: String
)