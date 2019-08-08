package se.davison.aws.lambda.apigatewayservlet

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.net.URI
import java.nio.ByteBuffer
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.Collections.enumeration
import javax.servlet.*
import javax.servlet.http.*
import kotlin.collections.HashMap


class LambdaHttpServletRequest(event: APIGatewayProxyRequestEvent) : HttpServletRequest {


    private val input: ServletInputStream
    private val path: String
    private val lambdaRemoteHost: String
    private val lambdaRemoteIp: String
    private val headers: Map<String, List<String>>
    private val parameters: Map<String, Array<String>>
    private val httpMethod: String
    private val attributes: MutableMap<String, Any?> = HashMap()
    private var eventQueryString: String?

    init {
        val buffer = event.body?.toByteArray()?.let {
            ByteBuffer.wrap(it)
        }
        this.input = object : ServletInputStream() {
            override fun isFinished() = !(buffer?.hasRemaining() ?: false)

            override fun isReady(): Boolean = buffer?.hasRemaining() ?: false

            override fun setReadListener(readListener: ReadListener) {}

            @Throws(IOException::class)
            override fun read() = buffer?.let {
                if (buffer.hasRemaining()) {
                    buffer.get().toInt()
                } else -1
            } ?: -1
        }

        this.headers = event.headers?.entries?.associate { it.key.toLowerCase() to listOf(it.value) } ?: emptyMap()
        this.attributes[LAMBDA_CONTEXT] = event.requestContext
        this.parameters = event.multiValueQueryStringParameters?.mapValues { it.value.toTypedArray() } ?: emptyMap()
        this.path = event.path
        this.httpMethod = event.httpMethod
        this.lambdaRemoteHost = getHeader("Host") ?: "unknown"
        this.lambdaRemoteIp = (getHeader("X-Forwarded-For") ?: "0.0.0.0, ").split(", ")[0]
        this.eventQueryString = event.queryStringParameters?.entries?.joinToString { "${it.key}=${it.value}" }

    }

    private var protocol = "HTTP/1.1"
    private var servletLocalPort = 8080

    override fun isUserInRole(role: String?) = false
    override fun startAsync(): AsyncContext {
        throw IllegalStateException()
    }

    override fun startAsync(servletRequest: ServletRequest?, servletResponse: ServletResponse?): AsyncContext {
        throw IllegalStateException()
    }

    override fun getPathInfo() = path

    override fun getProtocol() = protocol

    override fun getCookies(): Array<out Cookie> = headers.getOrDefault("cookie", emptyList()).stream()
            .map { parseCookie(it) }
            .toArray<Cookie> { length -> arrayOfNulls(length) }

    override fun getParameterMap() = parameters

    override fun getRequestURL() = StringBuffer("$scheme://$serverName:$serverPort$requestURI")

    override fun getAttributeNames() = enumeration(attributes.keys)

    override fun setCharacterEncoding(env: String?) {

    }

    override fun getParameterValues(name: String) = this.parameters[name]

    override fun getRemoteAddr() = lambdaRemoteIp

    override fun isAsyncStarted() = false

    override fun getContentLengthLong() = getHeader("Content-Length")?.toLongOrNull() ?: -1

    override fun getLocales(): Enumeration<Locale> = enumeration(setOf(Locale.getDefault()))

    override fun getRealPath(path: String?): String {
        throw UnsupportedOperationException()
    }

    override fun login(username: String?, password: String?) {

    }

    override fun getContextPath() = ""

    override fun isRequestedSessionIdValid() = false

    override fun getServerPort(): Int {
        val host = getHeader("Host")
        return if (host != null) {
            URI.create(host).port
        } else localPort
    }

    override fun getAttribute(name: String) = attributes[name]

    override fun getDateHeader(name: String) = getHeader(name)?.let {
        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(it)).toEpochMilli()
    } ?: -1

    override fun getRemoteHost() = lambdaRemoteHost

    override fun getRequestedSessionId() = null

    override fun getServletPath() = path

    override fun getSession(create: Boolean): HttpSession = throw UnsupportedOperationException()

    override fun getSession(): HttpSession = throw UnsupportedOperationException()

    override fun getServerName() = getHeader("Host")?.let {
        URI.create(it).host
    } ?: localName

    override fun getLocalAddr() = LOCAL_IP

    override fun isSecure() = false

    override fun <T : HttpUpgradeHandler?> upgrade(handlerClass: Class<T>?): T {
        throw UnsupportedOperationException()
    }

    override fun isRequestedSessionIdFromCookie() = false

    override fun getPart(name: String) = throw UnsupportedOperationException()

    override fun getRemoteUser() = throw UnsupportedOperationException()

    override fun getLocale() = locales.nextElement()

    override fun getMethod() = httpMethod

    override fun isRequestedSessionIdFromURL() = false

    override fun getLocalPort() = servletLocalPort

    override fun isRequestedSessionIdFromUrl() = false

    override fun getServletContext() = throw UnsupportedOperationException()

    override fun getQueryString() = eventQueryString

    override fun getDispatcherType() = DispatcherType.REQUEST;

    override fun getHeaders(name: String) = enumeration(headers.getOrDefault(name.toLowerCase(), emptyList()))

    override fun getUserPrincipal() = throw UnsupportedOperationException()

    override fun getParts() = throw UnsupportedOperationException()

    override fun getReader() = BufferedReader(InputStreamReader(inputStream, characterEncoding))

    override fun getScheme() = "http"

    override fun logout() = throw UnsupportedOperationException()

    override fun getInputStream() = input

    override fun getLocalName() = LOCAL_HOST

    override fun isAsyncSupported() = false

    override fun getAuthType() = throw UnsupportedOperationException()

    override fun getCharacterEncoding() = getHeader("Content-Type")
            ?.split("; charset=".toRegex(), 2)?.getOrNull(1)

    override fun getParameterNames() = enumeration(parameters.keys)

    override fun authenticate(response: HttpServletResponse) = false

    override fun removeAttribute(name: String) {
        attributes.remove(name)
    }

    override fun getPathTranslated() = null

    override fun getContentLength() = getIntHeader("Content-Length")

    override fun getHeader(name: String) = headers[name.toLowerCase()]?.get(0)

    override fun getIntHeader(name: String) = getHeader(name)?.toIntOrNull() ?: -1

    override fun changeSessionId(): String = ""

    override fun getContentType() = getHeader("Content-Type")

    override fun getAsyncContext(): AsyncContext = throw IllegalStateException()

    override fun getRequestURI() = "$contextPath${(if (path.isEmpty()) "/" else path)}"

    override fun getRequestDispatcher(path: String) = null

    override fun getHeaderNames() = enumeration(headers.keys)

    override fun setAttribute(name: String, o: Any) {
        attributes[name] = o
    }

    override fun getParameter(name: String) = parameters[name]?.let {
        it[0]
    }

    override fun getRemotePort() = 443

    fun parseCookie(string: String): Cookie {
        val records = string.split("; *".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val nameValue = records[0].split(" *= *".toRegex(), 2).toTypedArray()
        val cookie = Cookie(nameValue[0], nameValue[1])
        for (record in records) {
            val tagValue = record.split(" *= *".toRegex(), 2).toTypedArray()
            when (tagValue[0]) {
                "Domain" -> cookie.domain = tagValue[1]
                "Path" -> cookie.path = tagValue[1]
                "Comment" -> cookie.comment = tagValue[1]
                "Version" -> cookie.version = Integer.parseInt(tagValue[1])
                "HttpOnly" -> cookie.isHttpOnly = true
                "Secure" -> cookie.secure = true
                "Max-Age" -> cookie.maxAge = Integer.parseInt(tagValue[1])
                "Expires" -> cookie.maxAge = Duration.between(
                        Instant.now(),
                        Instant.from(DateTimeFormatter.RFC_1123_DATE_TIME.parse(tagValue[1]))
                ).seconds.toInt()
                "Discard" -> cookie.maxAge = -1
            }
        }
        return cookie
    }

    companion object {
        const val LOCAL_HOST = "localhost"
        const val LOCAL_IP = "127.0.0.1"
        const val LAMBDA_CONTEXT = "lambdaContext"
    }

}