package fr.spoutnik87.musicbot_rest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.context.annotation.Bean
import org.springframework.scheduling.TaskScheduler
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.scheduling.annotation.SchedulingConfigurer
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
import org.springframework.scheduling.config.ScheduledTaskRegistrar
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.transaction.annotation.EnableTransactionManagement

@SpringBootApplication
@EnableScheduling
@EnableTransactionManagement
class MusicbotRestApplication : SchedulingConfigurer {

    @Bean
    fun bCryptPasswordEncoder() = BCryptPasswordEncoder()

    @Bean(name = ["UUID"])
    fun uuid() = UUID()

    @Bean(name = ["AppConfig"])
    fun appConfig() = AppConfig()

    @Bean
    fun springApplicationContext() = SpringApplicationContext()

    override fun configureTasks(registrar: ScheduledTaskRegistrar) {
        registrar.setTaskScheduler(serverTaskScheduler())
    }

    @Bean
    fun serverTaskScheduler(): TaskScheduler {
        val taskScheduler = ThreadPoolTaskScheduler()
        taskScheduler.poolSize = 3
        taskScheduler.setThreadNamePrefix("app-scheduler-")
        taskScheduler.isDaemon = true
        return taskScheduler
    }
}

fun main(args: Array<String>) {
    runApplication<MusicbotRestApplication>(*args)
}