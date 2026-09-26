import org.gradle.api.tasks.SourceSetContainer

// Plain Java regression checks: no Minecraft launch or test-library download needed.
val sourceSets = extensions.getByType<SourceSetContainer>()
val regression = sourceSets.create("regression") {
    java.srcDir(rootProject.file("tests"))
    compileClasspath += sourceSets.getByName("client").output
    runtimeClasspath += sourceSets.getByName("client").output
}

val roomFiltersTest = tasks.register<JavaExec>("roomFiltersTest") {
    group = "verification"
    description = "Runs room-filter regression checks; assertion failures fail the build."
    dependsOn(tasks.named(regression.classesTaskName))
    classpath = regression.runtimeClasspath
    mainClass.set("RoomFiltersTest")
}

tasks.named("check") {
    dependsOn(roomFiltersTest)
}
