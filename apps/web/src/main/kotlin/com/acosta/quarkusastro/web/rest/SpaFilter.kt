package com.acosta.quarkusastro.web.rest

import java.io.IOException
import java.util.regex.Pattern
import javax.servlet.FilterChain
import javax.servlet.ServletException
import javax.servlet.ServletRequest
import javax.servlet.ServletResponse
import javax.servlet.annotation.WebFilter
import javax.servlet.http.HttpFilter
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse


@WebFilter(urlPatterns = ["/*"], asyncSupported = true)
class SpaFilter : HttpFilter() {
    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        servletRequest: ServletRequest,
        servletResponse: ServletResponse,
        chain: FilterChain
    ) {
        val request = servletRequest as HttpServletRequest
        val response = servletResponse as HttpServletResponse
        chain.doFilter(request, response)

        // do not alter an "application" response coming from the API
        if (request.requestURI.startsWith("/api") || request.requestURI.startsWith("/management")) {
            return
        }

        // If the server didn't find the resource
        if (response.status == 404) {
            // Is it a file (eg. image, font, etc.)
            val path = request.requestURI.substring(request.contextPath.length)
                .replace("[/]+$".toRegex(), "")
            if (!FILE_NAME_PATTERN.matcher(path).matches()) {
                // pass the request resolution to the front-end routes
                request.getRequestDispatcher("/").forward(request, response)
            }
        }
    }

    companion object {
        private val FILE_NAME_PATTERN = Pattern.compile(".*[.][a-zA-Z\\d]+")
    }
}
