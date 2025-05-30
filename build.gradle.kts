import io.gitlab.arturbosch.detekt.extensions.DetektExtension
import kotlinx.kover.api.CounterType
import kotlinx.kover.api.DefaultIntellijEngine
import kotlinx.kover.api.VerificationValueType
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.openapitools.generator.gradle.plugin.tasks.GenerateTask

plugins {
    application
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("org.jetbrains.kotlinx.kover")
    id("com.google.devtools.ksp")
    id("io.gitlab.arturbosch.detekt")
    id("com.github.johnrengelman.shadow")
    id("org.openapi.generator")
}

group = "de.hennihaus"
version = "0.0.1"

application {
    mainClass.set("io.ktor.server.cio.EngineMain")
}

tasks.shadowJar {
    manifest {
        attributes("Main-Class" to "io.ktor.server.cio.EngineMain")
    }
}

sourceSets {
    main {
        java.srcDirs(
            "build/generated/ksp/main/kotlin",
            "build/generated/openapi/src/main/kotlin",
        )
    }
}

repositories {
    maven {
        url = uri("https://maven.pkg.github.com/hennihaus/bamdatamodel")
        credentials {
            username = "hennihaus"
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    mavenCentral()
}

configurations.all {
    // exclude kotlin test libraries
    exclude("org.jetbrains.kotlin", "kotlin-test")
    exclude("org.jetbrains.kotlin", "kotlin-test-common")
    exclude("org.jetbrains.kotlin", "kotlin-test-annotations-common")
    exclude("org.jetbrains.kotlin", "kotlin-test-junit")
}

dependencies {
    val ktorVersion: String by project
    val logbackVersion: String by project
    val kotestVersion: String by project
    val kotestLibariesVersion: String by project
    val mockkVersion: String by project
    val junitVersion: String by project
    val koinVersion: String by project
    val koinLibrariesVersion: String by project
    val koinAnnotationsVersion: String by project
    val konformVersion: String by project
    val bamdatamodelVersion: String by project

    // ktor common plugins
    implementation("io.ktor:ktor-serialization-jackson-jvm:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")

    // ktor server plugins
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cors-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-status-pages-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-call-logging-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-cio-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")

    // ktor client plugins
    implementation("io.ktor:ktor-client-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-client-logging-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-client-mock-jvm:$ktorVersion")

    // koin plugins
    implementation("io.insert-koin:koin-ktor:$koinLibrariesVersion")
    compileOnly("io.insert-koin:koin-annotations:$koinAnnotationsVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinLibrariesVersion")
    ksp("io.insert-koin:koin-ksp-compiler:$koinAnnotationsVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion")
    testImplementation("io.insert-koin:koin-test-junit5:$koinVersion")

    // utility plugins
    implementation("io.konform:konform-jvm:$konformVersion")

    // model plugins
    implementation("de.hennihaus:bamdatamodel:$bamdatamodelVersion")
    testImplementation("de.hennihaus:bamdatamodel:$bamdatamodelVersion:tests")

    // test plugins
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-property-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-extensions-jvm:$kotestVersion")
    testImplementation("io.kotest:kotest-assertions-ktor-jvm:$kotestLibariesVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

val generateCreditModel by tasks.registering(GenerateTask::class) {
    generatorName.set("kotlin")
    outputDir.set("$buildDir/generated/openapi")
    inputSpec.set("$projectDir/docs/credit.json")
    configFile.set("$projectDir/docs/config.json")
    globalProperties.set(mapOf("models" to "", "modelDocs" to "false", "apis" to "false"))
    modelPackage.set("de.hennihaus.models.generated")
    skipValidateSpec.set(false)
    typeMappings.set(mapOf("string+date-time" to "OffsetDateTime"))
    importMappings.set(mapOf("OffsetDateTime" to "java.time.OffsetDateTime"))
}

ktlint {
    ignoreFailures.set(false)
    baseline.set(file("config/ktlint/baseline.xml"))
    filter {
        exclude("**/generated/**")
    }
}

detekt {
    config = files("config/detekt/detekt.yml")
    baseline = file("config/detekt/detekt-baseline.xml")
    source = files(
        DetektExtension.DEFAULT_SRC_DIR_JAVA,
        DetektExtension.DEFAULT_TEST_SRC_DIR_JAVA,
        "src/integrationTest/java",
        DetektExtension.DEFAULT_SRC_DIR_KOTLIN,
        DetektExtension.DEFAULT_TEST_SRC_DIR_KOTLIN,
        "src/integrationTest/kotlin",
    )
}

tasks.init {
    dependsOn(tasks.ktlintApplyToIdea)
}

kover {
    engine.set(DefaultIntellijEngine)
}

koverMerged {
    enable()
    xmlReport {
        onCheck.set(true)
    }
    htmlReport {
        onCheck.set(true)
    }
    verify {
        onCheck.set(true)
        rule {
            bound {
                val minTestCoverageInPercent: String by project
                minValue = minTestCoverageInPercent.toInt()
                counter = CounterType.LINE
                valueType = VerificationValueType.COVERED_PERCENTAGE
            }
        }
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()
        }

        val integrationTest by registering(JvmTestSuite::class) {
            dependencies {
                implementation(project())
                implementation(sourceSets.test.get().output)
            }

            targets {
                all {
                    testTask.configure {
                        shouldRunAfter(test)
                    }
                }
            }
        }
    }
}

tasks.withType(Test::class) {
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
    }
}

val integrationTestImplementation: Configuration by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}

tasks.compileKotlin {
    dependsOn(generateCreditModel)
}

tasks.check {
    dependsOn(testing.suites.named("integrationTest"))
}
