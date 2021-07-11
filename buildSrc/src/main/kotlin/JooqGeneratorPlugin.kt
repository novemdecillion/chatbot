package io.github.novemdecillion

import org.flywaydb.core.Flyway
import org.flywaydb.core.api.Location
import org.flywaydb.core.internal.configuration.ConfigUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

import org.gradle.kotlin.dsl.*
import org.flywaydb.core.internal.jdbc.DriverDataSource
import org.jooq.codegen.DefaultGeneratorStrategy
import org.jooq.codegen.GenerationTool
import org.jooq.codegen.GeneratorStrategy
import org.jooq.codegen.KotlinGenerator
import org.jooq.meta.Definition
import org.jooq.meta.jaxb.*
import org.jooq.meta.jaxb.Target
import java.sql.DriverManager

class GeneratorStrategy : DefaultGeneratorStrategy() {
  override fun getJavaClassName(definition: Definition?, mode: GeneratorStrategy.Mode?): String {
    return super.getJavaClassName(definition, mode)
      .let {
        when (mode) {
          GeneratorStrategy.Mode.DEFAULT -> "${it}Table"
          GeneratorStrategy.Mode.POJO -> "${it}Entity"
          else -> it
        }
      }
  }
}

open class JooqGeneratorPlugin : Plugin<Project> {
  companion object {
    const val PLUGIN_NAME = "jooqGenerator"
  }

  override fun apply(project: Project): Unit {
    project.extensions.create(PLUGIN_NAME, JooqGeneratorExtension::class)

    project.tasks {
      register(PLUGIN_NAME) {
        group = "novemdecillion"
        doLast {
          val extension = project.extensions.getByName(PLUGIN_NAME) as JooqGeneratorExtension

          Class.forName(extension.driver)
          DriverManager.getConnection(extension.url, extension.user, extension.password)
            .use {
              val flywayConfig: Map<String, String> = mapOf(
                ConfigUtils.DRIVER to extension.driver,
                ConfigUtils.URL to extension.url,
                ConfigUtils.USER to extension.user,
                ConfigUtils.PASSWORD to extension.password,
                ConfigUtils.LOCATIONS to "${Location.FILESYSTEM_PREFIX}${project.projectDir.absolutePath}/src/main/resources/db/migration",
                ConfigUtils.CLEAN_ON_VALIDATION_ERROR to true.toString()
              )

              val flyway = Flyway.configure().configuration(flywayConfig).load()
              flyway.migrate()

              val directory = if (extension.directory.isEmpty()) {
                "${project.projectDir.absolutePath}/src/main/jooq"
              } else {
                "${project.projectDir.absolutePath}${extension.directory}"
              }
              val packageName = extension.packageName.ifEmpty {
                "${project.group}"
              }

              val config = Configuration()
                .withLogging(Logging.DEBUG)
                .withBasedir(project.projectDir.absolutePath)
                .withGenerator(
                  Generator()
                    .withName(KotlinGenerator::class.java.canonicalName)
                    .withStrategy(
                      Strategy().withName(io.github.novemdecillion.GeneratorStrategy::class.java.canonicalName)
                    )
                    .withTarget(
                      Target()
                        .withPackageName(packageName)
                        .withDirectory(directory)
                    )
                    .withDatabase(
                      Database()
                        .withInputSchema("public")
                        .withOutputSchemaToDefault(true)
//                        .withExcludes(excludes)
                        .also { database ->
                          extension.appendForcedTypes?.also {
                            database.withForcedTypes(it)
                          }
                        }
                    )
                    .withGenerate(
                      Generate()
                        .withPojos(true)
                        .withDaos(true)
                        .withInterfaces(true)
                        .withGeneratedAnnotation(false)
                        .withJavaTimeTypes(true)
                    )
                )
              val generator = GenerationTool()
              generator.setConnection(flyway.configuration.dataSource.connection)
              generator.run(config)

              (flyway.configuration.dataSource as DriverDataSource).shutdownDatabase()
            }
        }
      }
    }
  }
}