package com.acosta.quarkusastro.security

import javax.ws.rs.NotAuthorizedException


class UserNotActivatedException(message: String) : NotAuthorizedException(message)
