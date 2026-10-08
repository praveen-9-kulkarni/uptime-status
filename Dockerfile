# Multi-stage: build the bootJar, then run it on a slim JRE.
# Same image can serve API (default profile) or worker (SPRING_PROFILES_ACTIVE=worker).

FROM eclipse-temurin:21-jdk-jammy AS build
WORKDIR /workspace

COPY gradlew settings.gradle build.gradle ./
COPY gradle ./gradle
COPY src ./src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon -x test \
    && find build/libs -name '*.jar' ! -name '*-plain.jar' -exec cp {} /workspace/app.jar \;

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Corp TLS inspection CA (Flipkart) — required for outbound HTTPS from this network.
COPY certs/FlipkartRootCA.cer /tmp/FlipkartRootCA.cer
RUN keytool -importcert -noprompt \
	-alias flipkart-root-ca \
	-file /tmp/FlipkartRootCA.cer \
	-keystore "$JAVA_HOME/lib/security/cacerts" \
	-storepass changeit \
	&& rm /tmp/FlipkartRootCA.cer

COPY --from=build /workspace/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
