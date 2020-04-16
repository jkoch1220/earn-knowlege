import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.yarn.YarnTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.github.hierynomus.license-report") version "0.15.0"
	id("com.github.node-gradle.node") version "2.2.3"
	id("com.github.ben-manes.versions") version "0.28.0"
	id("org.owasp.dependencycheck") version "5.3.2"
	kotlin("jvm") version "1.3.71"
	kotlin("plugin.spring") version "1.3.71"
	kotlin("kapt") version "1.3.71"
	kotlin("plugin.jpa") version "1.3.71"
	kotlin("plugin.noarg") version "1.3.71"
	groovy
}

version = "0.0.1"
java.sourceCompatibility = JavaVersion.VERSION_11

// see: https://docs.gradle.org/current/userguide/declaring_dependencies.html#sec:defining-custom-configurations
val developmentOnly: Configuration by configurations.creating

configurations {
	runtimeClasspath {
		extendsFrom(developmentOnly)
	}
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencyManagement {
	dependencies {
		dependencySet("org.spockframework:1.3-groovy-2.5") {
			entry("spock-core")
			entry("spock-spring")
		}
		dependency("org.springdoc:springdoc-openapi-kotlin:1.3.0")
		dependency("org.springdoc:springdoc-openapi-ui:1.3.0")
	}
}

dependencies {
	// Basics
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-actuator")


	// Kotlin
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	// --- Testing ---

	// Core
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
		exclude(group = "junit", module = "junit")
	}
	testImplementation("org.springframework.security:spring-security-test")

	// Spock testing
	testImplementation("org.spockframework:spock-core")
	testImplementation("org.spockframework:spock-spring")
}

tasks.withType<Test> {
	useJUnit()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = JavaVersion.VERSION_11.toString()
	}
}

// ******
// 'bootRun' task sets "local" profile
// ******

tasks.bootRun {
	systemProperty("spring.profiles.active", "local")
}


tasks.register<Copy>("moveBuild"){
from("$rootDir/src/main/webapp/build/")
into("$buildDir/dist/webapp")
}


// ******
// 'clean' task also cleans frontend build artifacts
// ******

//tasks.clean {
//	delete("$rootDir/src/main/webapp/build")
//	println("TESTING______________________")
//}

// ******
// 'build' task also builds frontend
// ******

node {
	// Version of node to use.
	version = "12.16.1"

	// Version of Yarn to use.
	yarnVersion = "1.22.4"

	// Base URL for fetching node distributions (change if you have a mirror).
	distBaseUrl = "https://nodejs.org/dist"

	// If true, it will download node using above parameters.
	// If false, it will try to use globally installed node.
	download = true

	// Set the work directory for unpacking node
	workDir = file("${project.buildDir}/nodejs")

	// Set the work directory where node_modules should be located
	nodeModulesDir = file("$rootDir/src/main/webapp")
}


val yarnBuild by tasks.registering(YarnTask::class) {
	dependsOn(tasks.yarn)
	args = listOf("build")
	setWorkingDir(project.extensions.getByType(NodeExtension::class).nodeModulesDir)
	inputs.files(
			fileTree(mapOf(
					"dir" to "$rootDir/src/main/webapp/",
					"include" to "*",
					"exclude" to listOf("$rootDir/src/main/webapp/build/", "$rootDir/src/main/webapp/node_modules/")
			)).files
	)
	outputs.dirs(
			listOf("$buildDir/dist/webapp")
	)
}

configure<SourceSetContainer> {
	named("main") {
		output.dir(mapOf("builtBy" to "yarnBuild"), "${buildDir}/dist")
	}
}
