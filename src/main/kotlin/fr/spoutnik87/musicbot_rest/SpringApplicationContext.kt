package fr.spoutnik87.musicbot_rest

import org.springframework.context.ApplicationContext
import org.springframework.context.ApplicationContextAware
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder

class SpringApplicationContext : ApplicationContextAware {
    override fun setApplicationContext(appContext: ApplicationContext) {
        applicationContext = appContext
    }

    companion object {
        private lateinit var applicationContext: ApplicationContext

        private fun getBean(name: String) = applicationContext.getBean(name)

        val uuid: UUID
            get() = getBean("UUID") as UUID

        val appConfig: AppConfig
            get() = getBean("AppConfig") as AppConfig

        val bCryptPasswordEncoder: BCryptPasswordEncoder
            get() = getBean("BCryptPasswordEncoder") as BCryptPasswordEncoder
    }
}
