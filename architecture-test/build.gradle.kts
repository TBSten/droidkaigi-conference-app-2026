plugins {
    kotlin("jvm")
}

kotlin {
    jvmToolchain(21)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        events("passed", "failed", "skipped")
    }
}

dependencies {
    testImplementation("me.tbsten.katachi:katachi:0.1.0")
    testImplementation("me.tbsten.katachi:katachi-konsist:0.1.0")
    testImplementation(platform("org.junit:junit-bom:5.13.4"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}
