package must.kdroiders.hustlehub.ui.features.chat.data.remote

import must.kdroiders.hustlehub.core.api.ApiResponse
import must.kdroiders.hustlehub.core.api.PageResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.ConversationResponse
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.CreateConversationRequest
import must.kdroiders.hustlehub.ui.features.chat.data.remote.dto.MessageResponse
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ConversationApiService {
    @GET("conversations")
    suspend fun getConversations(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 20,
    ): ApiResponse<PageResponse<ConversationResponse>>

    @POST("conversations")
    suspend fun getOrCreateConversation(
        @Body request: CreateConversationRequest,
    ): ApiResponse<ConversationResponse>

    @GET("conversations/{conversationId}/messages")
    suspend fun getMessages(
        @Path("conversationId") conversationId: String,
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50,
    ): ApiResponse<PageResponse<MessageResponse>>

    @PUT("conversations/{conversationId}/read")
    suspend fun markAsRead(
        @Path("conversationId") conversationId: String,
    ): ApiResponse<Unit>

    @DELETE("conversations/{conversationId}")
    suspend fun deleteConversation(
        @Path("conversationId") conversationId: String,
    ): ApiResponse<Unit>

    @DELETE("messages/{messageId}")
    suspend fun deleteMessageForMe(
        @Path("messageId") messageId: String,
    ): ApiResponse<Unit>

    @DELETE("messages/{messageId}/everyone")
    suspend fun deleteMessageForEveryone(
        @Path("messageId") messageId: String,
    ): ApiResponse<Unit>
}
