plugins {
    application
}

group = "com.egjaedong"
version = "0.1.0-SNAPSHOT"
description = "Java 实现的 tiny-claw（对照 go/ 教程，自行填写）"

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

application {
    mainClass = "com.egjaedong.tinyclaw.Claw"
}

repositories {
    mavenCentral()
}

val lombok = "org.projectlombok:lombok:1.18.46"

dependencies {
    compileOnly(lombok)
    annotationProcessor(lombok)
    testCompileOnly(lombok)
    testAnnotationProcessor(lombok)
}
