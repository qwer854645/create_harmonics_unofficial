package io.github.qwer854645.createresonance.foundation.registry

import io.github.qwer854645.createresonance.foundation.services.PlatformService

/**
 * Marks a [Registrable] object so [autoRegister] can skip it without initializing the object
 * (avoids loading client-only classes on the dedicated server).
 */
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
annotation class RegistrableEnv(
    val value: PlatformService.Environment,
)
