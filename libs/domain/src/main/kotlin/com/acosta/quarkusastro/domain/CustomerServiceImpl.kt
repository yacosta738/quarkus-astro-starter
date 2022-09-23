package com.acosta.quarkusastro.domain

import com.acosta.quarkusastro.port.CustomerService
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CustomerServiceImpl : CustomerService {
    override fun getMessage(): String {
        return "howdy dude leb"
    }
}