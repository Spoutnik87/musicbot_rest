package fr.spoutnik87.musicbot_rest.util

import org.springframework.beans.BeanUtils
import org.springframework.core.GenericTypeResolver
import org.springframework.core.annotation.AnnotatedElementUtils
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.test.context.TestSecurityContextHolder
import org.springframework.security.test.context.support.TestExecutionEvent
import org.springframework.security.test.context.support.WithSecurityContext
import org.springframework.security.test.context.support.WithSecurityContextFactory
import org.springframework.test.context.TestContext
import org.springframework.test.context.support.AbstractTestExecutionListener
import org.springframework.test.util.MetaAnnotationUtils
import java.lang.reflect.AnnotatedElement


class WithSecurityContextTestExecutionListener : AbstractTestExecutionListener() {

    override fun beforeTestMethod(testContext: TestContext) {}

    override fun beforeTestExecution(testContext: TestContext) {
        var testSecurityContext = this.createTestSecurityContext(testContext.testMethod as AnnotatedElement, testContext)
        if (testSecurityContext == null) {
            testSecurityContext = this.createTestSecurityContext(testContext.testClass, testContext)
        }

        if (testSecurityContext != null) {
            val securityContext = testSecurityContext.securityContext
            if (testSecurityContext.testExecutionEvent == TestExecutionEvent.TEST_METHOD) {
                TestSecurityContextHolder.setContext(securityContext)
            } else {
                testContext.setAttribute(SECURITY_CONTEXT_ATTR_NAME, securityContext)
            }
        }

        val securityContext = testContext.removeAttribute(SECURITY_CONTEXT_ATTR_NAME) as SecurityContext?
        if (securityContext != null) {
            TestSecurityContextHolder.setContext(securityContext)
        }

    }

    private fun createTestSecurityContext(annotated: AnnotatedElement, context: TestContext): TestSecurityContext? {
        return this.createTestSecurityContext(annotated,
                AnnotatedElementUtils.findMergedAnnotation(annotated, WithSecurityContext::class.java),
                context)
    }

    private fun createTestSecurityContext(annotated: Class<*>, context: TestContext): TestSecurityContext? {
        val withSecurityContextDescriptor = MetaAnnotationUtils.findAnnotationDescriptor(annotated, WithSecurityContext::class.java)
        val withSecurityContext = withSecurityContextDescriptor?.annotation
        return this.createTestSecurityContext(annotated, withSecurityContext, context)
    }

    private fun createTestSecurityContext(annotated: AnnotatedElement, withSecurityContext: WithSecurityContext?, context: TestContext): TestSecurityContext? {
        var withSecurityContext = withSecurityContext
        if (withSecurityContext == null) {
            return null
        } else {
            withSecurityContext = AnnotationUtils.synthesizeAnnotation(withSecurityContext, annotated)
            val factory: WithSecurityContextFactory<Annotation> = this.createFactory(withSecurityContext, context) as WithSecurityContextFactory<Annotation>
            val type = GenericTypeResolver.resolveTypeArgument(factory.javaClass, WithSecurityContextFactory::class.java) as Class<out Annotation>
            val annotation = this.findAnnotation(annotated, type)
            val initialize = withSecurityContext.setupBefore

            try {
                return TestSecurityContext(factory.createSecurityContext(annotation), initialize)
            } catch (var9: RuntimeException) {
                throw IllegalStateException("Unable to create SecurityContext using " + annotation!!, var9)
            }
        }
    }

    private fun findAnnotation(annotated: AnnotatedElement, type: Class<out Annotation>?): Annotation? {
        val findAnnotation = AnnotationUtils.findAnnotation(annotated, type)
        if (findAnnotation != null) {
            return findAnnotation
        } else {
            val allAnnotations = AnnotationUtils.getAnnotations(annotated)
            val var6 = allAnnotations!!.size

            for (var7 in 0 until var6) {
                val annotationToTest = allAnnotations[var7]
                val withSecurityContext = AnnotationUtils.findAnnotation(annotationToTest.annotationClass::class.java, WithSecurityContext::class.java)
                if (withSecurityContext != null) {
                    return annotationToTest
                }
            }

            return null
        }
    }

    private fun createFactory(withSecurityContext: WithSecurityContext, testContext: TestContext): WithSecurityContextFactory<out Annotation> {
        val clazz = withSecurityContext.factory

        try {
            return testContext.applicationContext.autowireCapableBeanFactory.createBean(clazz.java) as WithSecurityContextFactory<*>
        } catch (var5: IllegalStateException) {
            return BeanUtils.instantiateClass(clazz.java) as WithSecurityContextFactory<*>
        } catch (var6: Exception) {
            throw RuntimeException(var6)
        }
    }

    override fun afterTestMethod(testContext: TestContext) {
        TestSecurityContextHolder.clearContext()
    }

    companion object {
        val SECURITY_CONTEXT_ATTR_NAME = WithSecurityContextTestExecutionListener::class.java.name + ".SECURITY_CONTEXT"

        class TestSecurityContext(val securityContext: SecurityContext, val testExecutionEvent: TestExecutionEvent)
    }
}