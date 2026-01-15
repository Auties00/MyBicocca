package it.attendance100.mybicocca.data.api.logging

import org.aspectj.lang.JoinPoint
import org.aspectj.lang.annotation.AfterReturning
import org.aspectj.lang.annotation.Aspect
import org.aspectj.lang.annotation.Pointcut

/**
 * AspectJ aspect that logs all DTO instantiations during tests.
 *
 * This aspect intercepts constructor calls for all classes in the
 * `it.attendance100.mybicocca.data.dto` package and prints them to stderr.
 */
@Aspect
class DtoLoggingAspect {

    @Pointcut("execution(it.attendance100.mybicocca.data.dto..*.new(..))")
    fun dtoConstruction() {

    }

    @AfterReturning(pointcut = "dtoConstruction()", returning = "dto")
    fun logDtoCreation(joinPoint: JoinPoint, dto: Any) {
        System.err.println("══════════════════════════════════════════════════════════════")
        System.err.println("DTO Created: ${dto::class.simpleName}")
        System.err.println("──────────────────────────────────────────────────────────────")
        System.err.println(dto)
        System.err.println("══════════════════════════════════════════════════════════════")
    }
}
