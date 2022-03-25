import de.undercouch.gradle.tasks.download.Download
import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.intellij.tasks.PatchPluginXmlTask
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.io.*
import java.nio.file.Paths

val isCI = !System.getenv("CI").isNullOrBlank()
val commitHash = Runtime.getRuntime().exec("git rev-parse --short HEAD").run {
	waitFor()
	val output = inputStream.bufferedReader().use { it.readLine() }
	destroy()
	output.trim()
}

val pluginComingVersion = "0.3.3"
val pluginVersion = if (isCI) "$pluginComingVersion-$commitHash" else pluginComingVersion
val packageName = "rs.pest"
val asmble = "asmble"
val rustTarget = projectDir.resolve("rust").resolve("target")

group = packageName
version = pluginVersion

plugins {
	java
	id("org.jetbrains.intellij") version "1.4.0"
	id("org.jetbrains.grammarkit") version "2021.2.1"
	id("de.undercouch.download") version "5.0.2"
	kotlin("jvm") version "1.6.10"
	idea
    id("com.diffplug.spotless") version "6.3.0"
}

allprojects { apply { plugin("org.jetbrains.grammarkit") } }

fun fromToolbox(root: String, ide: String) = file(root)
	.resolve(ide)
	.takeIf { it.exists() }
	?.resolve("ch-0")
	?.listFiles()
	.orEmpty()
	.filterNotNull()
	.filter { it.isDirectory }
	.filterNot { it.name.endsWith(".plugins") }
	.maxByOrNull {
		val (major, minor, patch) = it.name.split('.')
		String.format("%5s%5s%5s", major, minor, patch)
	}
	?.also { println("Picked: $it") }

intellij {
	updateSinceUntilBuild.value(false)
	instrumentCode.value(true)
	plugins.value(listOf("org.rust.lang:0.4.165.4438-213", "org.toml.lang:213.5744.224", "java"))
		version.value("2021.3.2")
}

idea {
		module {
				isDownloadSources = true
				generatedSourceDirs.plus(file("gen"))
		}
}

java {
		toolchain {
				languageVersion.set(JavaLanguageVersion.of(11))
		}

		consistentResolution {
				useCompileClasspathVersions()
		}
}

tasks.withType<PatchPluginXmlTask> {
	changeNotes.value(file("res/META-INF/change-notes.html").readText())
	pluginDescription.value(file("res/META-INF/description.html").readText())
	version.value(pluginVersion)
	pluginId.value(packageName)
}

sourceSets {
	main {
		withConvention(KotlinSourceSet::class) {
			listOf(java, kotlin).forEach { it.srcDirs("src", "gen") }
		}
		resources.srcDirs("res", rustTarget.resolve("java").absolutePath)
	}

	test {
		withConvention(KotlinSourceSet::class) {
			listOf(java, kotlin).forEach { it.srcDirs("test") }
		}
		resources.srcDirs("testData")
	}
}

repositories {
	mavenCentral()
	maven { url = uri("https://repo.eclipse.org/content/groups/releases/")}
	maven { url = uri("https://www.jetbrains.com/intellij-repository/releases") }
	maven { url = uri("https://cache-redirector.jetbrains.com/intellij-dependencies") }
    google()
}

dependencies {
	implementation(kotlin("stdlib-jdk8"))
	implementation("org.eclipse.mylyn.github", "org.eclipse.egit.github.core", "2.1.5")
	implementation("org.jetbrains.kotlinx", "kotlinx-html-jvm", "0.7.3")
	implementation(files("$projectDir/rust/target/java"))
	testImplementation(kotlin("test-junit"))
	testImplementation("junit", "junit")
}

configurations.all {
  resolutionStrategy {
    failOnNonReproducibleResolution()
  }
}

task("displayCommitHash") {
	group = "help"
	description = "Display the newest commit hash"
	doFirst { println("Commit hash: $commitHash") }
}

task("isCI") {
	group = "help"
	description = "Check if it's running in a continuous-integration"
	doFirst { println(if (isCI) "Yes, I'm on a CI." else "No, I'm not on CI.") }
}

val downloadAsmble = task<Download>("downloadAsmble") {
	group = asmble
	src("https://github.com/pest-parser/intellij-pest/files/3592625/asmble.zip")
	dest(buildDir.absolutePath)
	overwrite(false)
}

val unzipAsmble = task<Copy>("unzipAsmble") {
	group = asmble
	dependsOn(downloadAsmble)
	from(zipTree(buildDir.resolve("asmble.zip")))
	into(buildDir)
}

val wasmFile = rustTarget
	.resolve("wasm32-unknown-unknown")
	.resolve("release")
	.resolve("pest_ide.wasm")
val wasmGcFile = wasmFile.resolveSibling("pest_ide_gc.wasm")

val asmbleExe by lazy {
	buildDir
		.resolve("asmble")
		.resolve("bin")
		.resolve(if (System.getProperty("os.name").startsWith("Windows")) "asmble.bat" else "asmble")
}

val compileRust = task<Exec>("compileRust") {
	val rustDir = projectDir.resolve("rust")
	workingDir(rustDir)
	inputs.dir(rustDir.resolve("src"))
	outputs.dir(rustDir.resolve("target"))
	commandLine("rustup", "run", "nightly", "cargo", "build", "--release")
}

val gcWasm = task/*<Exec>*/("gcWasm") {
	dependsOn(compileRust)
/* Asmble is broken for GCed wasm.
	workingDir(projectDir)
	inputs.file(wasmFile)
	outputs.file(wasmGcFile)
	commandLine("wasm-gc", wasmFile.absolutePath, wasmGcFile.absolutePath)
*/
	doFirst { wasmFile.copyTo(wasmGcFile, overwrite = true) }
}

val translateWasm = task<Exec>("translateWasm") {
	group = asmble
	dependsOn(unzipAsmble, gcWasm)
	workingDir(projectDir)
	val path = wasmGcFile.also { inputs.file(it) }.absolutePath
	val outPath = "${path.dropLast(1)}t".also { outputs.file(it) }
	commandLine(asmbleExe, "translate", path, outPath, "-out", "wast")
	doFirst { println("Output file: $outPath") }
}

val compileWasm = task<Exec>("compileWasm") {
	group = asmble
	dependsOn(unzipAsmble, gcWasm)
	workingDir(projectDir.absolutePath)
	val classRelativePath = listOf("rs", "pest", "vm", "PestUtil.class")
	val outFile = classRelativePath
		.fold(rustTarget.resolve("java"), File::resolve)
		.apply { parentFile.mkdirs() }
		.also { outputs.file(it) }
		.absolutePath
	val wasmGcFile = wasmGcFile.also { inputs.file(it) }.absolutePath
	val cls = classRelativePath.joinToString(separator = ".").removeSuffix(".class")
	commandLine(asmbleExe, "compile", wasmGcFile, cls, "-out", outFile)
	doFirst {
		println("Input file: $wasmGcFile")
		println("Output file: $outFile")
	}
}

fun path(more: Iterable<*>) = more.joinToString(File.separator)

val genParser = task<GenerateParserTask>("genParser") {
	group = tasks["init"].group!!
	description = "Generate the Parser and PsiElement classes"
	source.value("grammar/pest.bnf")
	targetRoot.value("gen/")
	val parserRoot = Paths.get("rs", "pest")
	pathToParser.value(path(parserRoot + "PestParser.java"))
	pathToPsiRoot.value(path(parserRoot + "psi"))
	purgeOldFiles.value(true)
}

val genLexer = task<GenerateLexerTask>("genLexer") {
	group = genParser.group
	description = "Generate the Lexer"
	source.value("grammar/pest.flex")
	targetDir.value(path(Paths.get("gen", "rs", "pest", "psi")))
	targetClass.value("PestLexer")
	purgeOldFiles.value(true)
	dependsOn(genParser)
}

tasks.withType<KotlinCompile> {
	dependsOn(genParser, genLexer, compileWasm)
	kotlinOptions {
		jvmTarget = "11"
		languageVersion = "1.6"
		apiVersion = "1.6"
		freeCompilerArgs = listOf("-Xjvm-default=enable")
	}
}

tasks.getByName("processResources").dependsOn(compileWasm)
