package com.quarkus.astro.service

class EmailAlreadyUsedException : RuntimeException("Email is already in use!")
