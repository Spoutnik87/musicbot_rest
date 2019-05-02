package fr.spoutnik87.musicbot_rest.util

import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithUserDetailsSecurityContextFactory::class, setupBefore = TestExecutionEvent.TEST_EXECUTION)
annotation class WithCustomUserDetails(val value: String = "user", val userDetailsServiceBeanName: String = "", val setupBefore: TestExecutionEvent = TestExecutionEvent.TEST_EXECUTION)
