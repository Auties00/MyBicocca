package it.attendance100.mybicocca.domain.model

sealed interface ElearningCourseSelector {
	data class Filter(
		val status: Status = Status.ALL,
		val sort: Sort = Sort.NEWEST,
		val query: String = "",
	) : ElearningCourseSelector {

		enum class Status {
			ALL,
			ACTIVE,
			PASSED,
			FUTURE
		}

		enum class Sort {
			NEWEST,
			OLDEST,
			NAME_ASC,
			NAME_DESC
		}
	}
}
