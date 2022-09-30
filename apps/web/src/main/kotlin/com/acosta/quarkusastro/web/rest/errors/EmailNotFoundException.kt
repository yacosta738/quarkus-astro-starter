package com.acosta.quarkusastro.web.rest.errors

class EmailNotFoundException :
    BadRequestAlertException("Email address not registered", "userManagement", "emailnotfound")
