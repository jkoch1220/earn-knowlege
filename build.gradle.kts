import com.moowork.gradle.node.NodeExtension
import com.moowork.gradle.node.yarn.YarnTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.github.jengelman.gradle.plugins.shadow.ShadowApplicationPlugin
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.jfrog.bintray.gradle.BintrayPlugin
import org.jetbrains.kotlin.gradle.dsl.Coroutines
import org.gradle.api.publish.maven.MavenPom
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import com.jfrog.bintray.gradle.BintrayExtension

val kotlinVersion = plugins.getPlugin(KotlinPluginWrapper::class.java).kotlinPluginVersion
val kotlinxCoroutinesVersion = "0.22.2"

kotlin {
	experimental.coroutines = Coroutines.ENABLE
}

plugins {
	id("org.springframework.boot") version "2.2.6.RELEASE"
	id("io.spring.dependency-management") version "1.0.9.RELEASE"
	id("com.github.hierynomus.license-report") version "0.15.0"
	id("com.github.node-gradle.node") version "2.2.3"
	id("com.github.ben-manes.versions") version "0.28.0"
	id("org.owasp.dependencycheck") version "5.3.2"
	id("com.jfrog.bintray") version "1.8.3"
	id("com.github.johnrengelman.shadow") version "2.0.2"
	kotlin("jvm") version "1.3.71"
	kotlin("plugin.spring") version "1.3.71"
	kotlin("kapt") version "1.3.71"
	kotlin("plugin.jpa") version "1.3.71"
	kotlin("plugin.noarg") version "1.3.71"
	groovy
	`maven-publish`
	maven
}

buildscript {
	repositories {
		jcenter()
		mavenCentral()
		}
}

project.group = "org.jfrog.example.gradle"
project.version = "1.0"

allprojects{
	repositories {
		jcenter()
	}
	apply(plugin = "com.jfrog.bintray")
	apply(plugin = "maven")
	apply(plugin = "maven-publish")
	apply(plugin = "kotlin")
}

fun MavenPom.addDependencies() = withXml {
	asNode().appendNode("dependencies").let { depNode ->
		configurations.compile.allDependencies.forEach {
			depNode.appendNode("dependency").apply {
				appendNode("groupId", it.group)
				appendNode("artifactId", it.name)
				appendNode("version", it.version)
			}
		}
	}
}

val artifactID = "earnkowlege"

val shadowJar: ShadowJar by tasks
shadowJar.apply {
	baseName = artifactID
	classifier = null
}

val publicationName = "MyPublication"
publishing {
	publications {
		create<MavenPublication>("MyPublication") {
			from(components["java"])
			groupId = "com.buissnes.earnkowlege"
			artifactId = rootProject.name
			artifact(shadowJar)
			version = "1.0-SNAPSHOT"

		}
	}
}

fun findProperty(s: String) = project.findProperty(s) as String?
bintray {
	user = "jkoch"
	key = "eb4214c79fe5a5eff0cc60b4a7a70bc1c68803e0"
	publish = true
	setPublications(publicationName)
	pkg(delegateClosureOf<BintrayExtension.PackageConfig> {
		repo = "generic"
		name = "earn-knowledge"
		userOrg = "tarent"
		websiteUrl = "https://gitlab.com/Y-/earn-knowlege"
		vcsUrl = "https://gitlab.com/Y-/earn-knowlege.git"
		description = "TEST"

		setLabels("kotlin")
		desc = description
	})
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

//tasks {
//	withType(GradleBuild::class.java) {
//		dependsOn(shadowJar)
//	}
//	withType<KotlinCompile> {
//		kotlinOptions.jvmTarget = "1.8"
//	}
//	withType(Test::class.java) {
//		testLogging.showStandardStreams = true
//	}
//	withType<GenerateMavenPom> {
//		destination = file("$buildDir/libs/${shadowJar.name}.pom")
//	}
//}
