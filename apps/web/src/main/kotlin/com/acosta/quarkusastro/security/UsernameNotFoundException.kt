package com.acosta.quarkusastro.security

import javax.ws.rs.NotAuthorizedException


class UsernameNotFoundException(message: String) : NotAuthorizedException(message)
