plugins {
    `java-library`
    id("com.vanniktech.maven.publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(25)
    }
}

tasks.compileJava {
    options.release = 8
}

dependencies {
    compileOnly("org.jspecify:jspecify:1.0.0")

    compileOnly("io.netty:netty-all:4.2.13.Final")
    testImplementation("io.netty:netty-all:4.2.13.Final")

    testImplementation(platform("org.junit:junit-bom:6.0.3"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation("org.mockito:mockito-core:5.23.0")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates("io.github.takwolf.netty", "netty-virtual-udp", "0.0.4")

    pom {
        name.set("Netty Virtual UDP")
        description.set("A virtual connection abstraction for Netty UDP, mapping datagrams into isolated session channels")
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
