package com.acosta.quarkusastro.service

import com.acosta.quarkusastro.config.AppProperties
import com.acosta.quarkusastro.domain.User
import io.quarkus.mailer.MailTemplate
import io.quarkus.qute.Location
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletionStage
import javax.enterprise.context.ApplicationScoped
import javax.inject.Inject


/**
 * Service for sending emails.
 */
@ApplicationScoped
class MailService @Inject constructor(
    appProperties: AppProperties,
    @Location("mail/activationEmail") activationEmail: MailTemplate,
    @Location("mail/creationEmail") creationEmail: MailTemplate,
    @Location("mail/passwordResetEmail") passwordResetEmail: MailTemplate
) {
    private val log = LoggerFactory.getLogger(MailService::class.java)
    private val appProperties: AppProperties
    private val activationEmail: MailTemplate
    private val creationEmail: MailTemplate
    private val passwordResetEmail: MailTemplate

    init {
        this.appProperties = appProperties
        this.activationEmail = activationEmail
        this.creationEmail = creationEmail
        this.passwordResetEmail = passwordResetEmail
    }

    fun sendEmailFromTemplate(
        user: User,
        template: MailTemplate,
        subject: String?
    ): CompletionStage<Void> {
        return template
            .to(user.email)
            .subject(subject)
            .data(BASE_URL, appProperties.mail().baseUrl())
            .data(USER, user)
            .send()
            .subscribeAsCompletionStage()
            .thenAccept { _: Void? ->
                log.debug(
                    "Sent email to User '{}'",
                    user.email
                )
            }
    }

    fun sendActivationEmail(user: User): CompletionStage<Void> {
        log.debug("Sending activation email to '{}'", user.email)
        return sendEmailFromTemplate(
            user,
            activationEmail,
            "Astro Quarkus account activation is required"
        )
    }

    fun sendCreationEmail(user: User): CompletionStage<Void> {
        log.debug("Sending creation email to '{}'", user.email)
        return sendEmailFromTemplate(
            user,
            creationEmail,
            "Astro Quarkus account activation is required"
        )
    }

    fun sendPasswordResetMail(user: User): CompletionStage<Void> {
        log.debug("Sending password reset email to '{}'", user.email)
        return sendEmailFromTemplate(
            user,
            passwordResetEmail,
            "Astro Quarkus password reset"
        )
    }

    companion object {
        private const val USER = "user"
        private const val BASE_URL = "baseUrl"
    }
}
