package se.davison.aws.lambda.apigatewayservlet

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.PrintWriter
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.*
import javax.servlet.ServletOutputStream
import javax.servlet.WriteListener
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletResponse


class LambdaHttpServletResponse : HttpServletResponse {

    private var statusMessage: String? = null
    private var responseStatus: Int = 200
    private val headers: MutableMap<String, MutableList<String>> = HashMap()
    private var contentLength = 0L
    private var encoding: String? = null

    private val stream = object : ServletOutputStream() {

        private val buffer = ByteArrayOutputStream()
        private var ready = true

        override fun isReady() = ready

        val byteArray: ByteArray
            get() = buffer.toByteArray()

        override fun write(b: Int) {
            if (!isReady) {
                throw IllegalStateException()
            }
            buffer.write(b)
        }

        override fun setWriteListener(writeListener: WriteListener) {
            try {
                writeListener.onWritePossible()
            } catch (e: IOException) {
                throw RuntimeException(e)
            }
        }

        override fun flush() {
            if (isReady) {
                ready = false
                flushBuffer()
                ready = true
            }
        }

        override fun close() {
            ready = false
        }


    }

    val gatewayResponseObject: APIGatewayProxyResponseEvent
        get() = APIGatewayProxyResponseEvent()
                .withBody(String(stream.byteArray))
                .withStatusCode(status)
                .withHeaders(headers.entries.associate {
                    it.key to it.value.joinToString(",")
                })
                .also {
                    stream.flush()
                    stream.close()
                }

    override fun getCharacterEncoding(): String = encoding ?: Charset.defaultCharset().name()

    override fun setCharacterEncoding(charset: String?) {
//        getHeader("Content-Type")?.let {
//            setHeader("Content-Type", "${it.split("; charset=")[0]}; charset=$charset")
//        }
        encoding = charset
    }


    override fun encodeURL(url: String) = URLEncoder.encode(url, "UTF-8")

    override fun encodeUrl(url: String) = encodeURL(url)

    override fun addIntHeader(name: String, value: Int) = addHeader(name, value.toString())

    override fun addCookie(cookie: Cookie?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun encodeRedirectUrl(url: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun flushBuffer() {
        stream.flush()
    }

    override fun encodeRedirectURL(url: String?): String {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendRedirect(location: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setBufferSize(size: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocale(): Locale {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendError(sc: Int, msg: String?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendError(sc: Int) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setContentLengthLong(len: Long) {
        this.contentLength = len
    }


    override fun addDateHeader(name: String, date: Long) = addHeader(name, date.toString())

    override fun setLocale(loc: Locale?) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getHeaders(name: String): Collection<String> = headers.getOrDefault(name, emptyList())

    override fun setContentLength(len: Int) {
        contentLength = len.toLong()
    }

    override fun getBufferSize(): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun resetBuffer() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun reset() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun setDateHeader(name: String, date: Long) = setHeader(name, date.toString())

    override fun getStatus(): Int = responseStatus

    override fun isCommitted(): Boolean = false

    override fun setStatus(sc: Int) {
        responseStatus = sc
    }

    override fun setStatus(sc: Int, sm: String) {
        responseStatus = sc
        statusMessage = sm
    }

    override fun getHeader(name: String): String? = headers[name]?.firstOrNull()

    override fun getContentType(): String? = getHeader("Content-Type")

    override fun getWriter(): PrintWriter {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun containsHeader(name: String) = headers.containsKey(name)

    override fun setIntHeader(name: String, value: Int) = setHeader(name, value.toString())

    override fun getHeaderNames(): MutableCollection<String> = headers.keys

    override fun setHeader(name: String, value: String) {
        headers[name] = mutableListOf(value)
    }

    override fun addHeader(name: String, value: String) {
        headers.getOrPut(name, ::mutableListOf).add(value)
    }

    override fun getOutputStream(): ServletOutputStream = stream

    override fun setContentType(type: String) {
        setHeader("Content-Type", type)
        val parts = type.split("; charset=".toRegex(), 2).toTypedArray()
        characterEncoding = if (parts.size > 1) parts[1] else characterEncoding
    }


}