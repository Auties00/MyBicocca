package it.attendance100.mybicocca.data.api.elearning

import retrofit2.http.*
import retrofit2.Call
import okhttp3.RequestBody
import com.google.gson.annotations.SerializedName

import it.attendance100.mybicocca.data.dto.elearning.ElearningCoreCalendarDeleteSubscription200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningErrorResponse
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBookGetBooksByCourses200Response
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBookGetBooksByCoursesRequest
import it.attendance100.mybicocca.data.dto.elearning.ElearningModBookViewBookRequest

interface ModBookApi {
    /**
     * POST mod_book_get_books_by_courses
     * Returns a list of book instances in a provided set of courses,                             if no courses are provided then all the book instances the user has access to will be returned.
     * Returns a list of book instances in a provided set of courses,                             if no courses are provided then all the book instances the user has access to will be returned.
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBookGetBooksByCoursesRequest 
     * @return [Call]<[ElearningModBookGetBooksByCourses200Response]>
     */
    @POST("mod_book_get_books_by_courses")
    fun modBookGetBooksByCourses(@Body elearningModBookGetBooksByCoursesRequest: ElearningModBookGetBooksByCoursesRequest): Call<ElearningModBookGetBooksByCourses200Response>

    /**
     * POST mod_book_view_book
     * Simulate the view.php web interface book: trigger events, completion, etc...
     * Simulate the view.php web interface book: trigger events, completion, etc...
     * Responses:
     *  - 200: Successful response
     *  - 400: Invalid parameter value detected
     *
     * @param elearningModBookViewBookRequest 
     * @return [Call]<[ElearningCoreCalendarDeleteSubscription200Response]>
     */
    @POST("mod_book_view_book")
    fun modBookViewBook(@Body elearningModBookViewBookRequest: ElearningModBookViewBookRequest): Call<ElearningCoreCalendarDeleteSubscription200Response>

}
