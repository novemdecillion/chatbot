import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
  id("org.springframework.boot") version "2.5.1"
  id("io.spring.dependency-management") version "1.0.11.RELEASE"
  id("io.github.novemdecillion.jooq-generator")
  kotlin("jvm") version "1.5.10"
  kotlin("plugin.spring") version "1.5.10"
}

sourceSets.main {
  java.srcDir("src/main/jooq")
}

group = "jp.co.supportas.chat"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
  mavenCentral()
}

dependencies {
  // Web
  implementation("org.springframework.boot:spring-boot-starter-websocket")

  implementation("com.graphql-java-kickstart:graphql-spring-boot-starter:11.1.0") {
    exclude(group = "javax.websocket", module = "javax.websocket-api")
  }

  // DB
  implementation("org.springframework.boot:spring-boot-starter-jooq")
  runtimeOnly("org.postgresql:postgresql")
  runtimeOnly("org.flywaydb:flyway-core")

  implementation("io.projectreactor:reactor-core")


  implementation("org.jetbrains.kotlin:kotlin-reflect")
  implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

  implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.3")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml:2.12.3")

  implementation("org.asciidoctor:asciidoctorj:2.5.1")

  developmentOnly("org.springframework.boot:spring-boot-devtools")

  // Test
  testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<KotlinCompile> {
  kotlinOptions {
    freeCompilerArgs = listOf("-Xjsr305=strict")
    jvmTarget = "11"
  }
}

tasks.withType<Test> {
  useJUnitPlatform()
}

tasks.getByName<BootJar>("bootJar") {
  requiresUnpack("**/asciidoctorj-*.jar")
}

jooqGenerator {
  driver = "org.testcontainers.jdbc.ContainerDatabaseDriver"
  url = "jdbc:tc:postgresql:11:///build-test"
  user = "admin"
  password = "password123"
  packageName ="io.github.novemdecillion.adapter.jooq"
//  appendForcedTypes = listOf(
//    ForcedType()
//      .also {
//        it.isEnumConverter = true
//        it.includeExpression = """.*\.ROLE"""
//        it.userType = "io.github.novemdecillion.domain.Role[]"
//      },
//    org.jooq.meta.jaxb.ForcedType()
//      .also {
//        it.isEnumConverter = true
//        it.includeExpression = """STUDY\.STATUS"""
//        it.userType = "io.github.novemdecillion.domain.StudyStatus"
//      }  )
}