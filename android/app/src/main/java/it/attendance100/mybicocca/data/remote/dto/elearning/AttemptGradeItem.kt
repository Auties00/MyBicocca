package it.attendance100.mybicocca.data.remote.dto.elearning
import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class AttemptGradeItem(
    @SerializedName("name") val name: String? = null,
    @SerializedName("grade") val grade: BigDecimal? = null,
    @SerializedName("maxgrade") val maxGrade: BigDecimal? = null
)
