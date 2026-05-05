plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

tasks.compileJava {
    options.release = 8
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")

    compileOnly("io.netty:netty-all:4.2.12.Final")
    testImplementation("io.netty:netty-all:4.2.12.Final")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates("io.github.takwolf.netty", "netty-virtual-udp", "0.0.2")

    pom {
        name.set("Netty Virtual UDP")
        description.set("A Netty virtual UDP library that provides a TCP-like transport abstraction")
        url.set("https://github.com/TakWolf/netty-virtual-udp")
        inceptionYear.set("2026")
        licenses {
            license {
                name.set("Apache-2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                distribution.set("repo")
            }
        }
        developers {
            developer {
                id.set("TakWolf")
                name.set("TakWolf")
                url.set("https://github.com/TakWolf")
            }
        }
        scm {
            url.set("https://github.com/TakWolf/netty-virtual-udp")
            connection.set("scm:git:git://github.com/TakWolf/netty-virtual-udp.git")
            developerConnection.set("scm:git:ssh://git@github.com/TakWolf/netty-virtual-udp.git")
        }
    }

    publishToMavenCentral()
    signAllPublications()
}
