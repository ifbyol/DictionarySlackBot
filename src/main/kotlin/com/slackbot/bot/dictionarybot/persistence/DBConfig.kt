package com.slackbot.bot.dictionarybot.persistence

import org.springframework.context.annotation.Configuration
import java.net.URI
import javax.sql.DataSource
import org.springframework.boot.jdbc.DataSourceBuilder
import org.springframework.context.annotation.Bean


@Configuration
class DBConfig {

    @Bean
    fun getDataSource(): DataSource {
        val dbUri = URI(System.getenv(DB_URL))

        val username = dbUri.userInfo.split(":")[0]
        val password = dbUri.userInfo.split(":")[1]

        val dbUrl = "jdbc:mysql://${dbUri.host}${dbUri.path}"

        val builder = DataSourceBuilder.create()
        builder.url(dbUrl)
        builder.password(password)
        builder.username(username)

        return builder.build()
    }

    companion object {
        const val DB_URL = "DATABASE_URL"
    }
}