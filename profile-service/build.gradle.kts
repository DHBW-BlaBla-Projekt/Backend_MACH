plugins {
	java
	jacoco
	id("org.springframework.boot") version "3.4.1"
	id("io.spring.dependency-management") version "1.1.7"
}


group = "com.example.profileservice"
version = "0.0.1-SNAPSHOT"

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(17)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	runtimeOnly("org.postgresql:postgresql")
	runtimeOnly("com.h2database:h2")
	
	// JWT Dependencies
	implementation("io.jsonwebtoken:jjwt-api:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.6")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.6")
	
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()

	systemProperty("spring.datasource.url", "jdbc:h2:mem:profile_test;MODE=PostgreSQL;DB_CLOSE_DELAY=-1")
	systemProperty("spring.datasource.driver-class-name", "org.h2.Driver")
	systemProperty("spring.datasource.username", "sa")
	systemProperty("spring.datasource.password", "")
	systemProperty("spring.jpa.properties.hibernate.dialect", "org.hibernate.dialect.H2Dialect")
	systemProperty("spring.jpa.hibernate.ddl-auto", "create-drop")
	systemProperty("app.jwt.secret", "ci-test-only-secret-key-for-profile-service-0123456789")
}

tasks.register("stage") {
	dependsOn(tasks.named("bootJar"))
}
