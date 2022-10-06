package com.quarkus.astro.security

import javax.ws.rs.NotAuthorizedException


class UsernameNotFoundException(message: String) : NotAuthorizedException(message)
