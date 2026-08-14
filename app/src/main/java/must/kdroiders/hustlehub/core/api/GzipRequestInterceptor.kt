package must.kdroiders.hustlehub.core.api

import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.Response
import okio.BufferedSink
import okio.GzipSink
import okio.buffer

class GzipRequestInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val originalBody = originalRequest.body

        if (originalBody == null || originalRequest.header("Content-Encoding") != null) {
            return chain.proceed(originalRequest)
        }

        val compressedRequest = originalRequest
            .newBuilder()
            .header("Content-Encoding", "gzip")
            .method(
                originalRequest.method,
                gzip(originalBody),
            ).build()

        return chain.proceed(compressedRequest)
    }

    private fun gzip(body: RequestBody): RequestBody =
        object : RequestBody() {
            override fun contentType(): MediaType? = body.contentType()

            override fun contentLength(): Long = -1L

            override fun writeTo(sink: BufferedSink) {
                val gzipSink = GzipSink(sink).buffer()
                body.writeTo(gzipSink)
                gzipSink.close()
            }
        }
}
