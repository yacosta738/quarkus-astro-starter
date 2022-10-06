package com.quarkus.astro.domain

import com.quarkus.astro.port.CustomerService
import javax.enterprise.context.ApplicationScoped

@ApplicationScoped
class CustomerServiceImpl : CustomerService {
    override fun getMessage(): String {
        return "howdy dude leb"
    }
}
