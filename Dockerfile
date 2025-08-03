
# Use a base image with Java
#FROM docker.arvancloud.ir/openjdk:21-jdk-alpine as build
FROM openjdk:21-jdk as build

# Set the working directory inside the container. All following commands will run in this directory. If it doesn't exist, Docker will create it.
WORKDIR /app

# Copies everything from your project folder (on your local system) into /app in the container. because We need source code and build files (pom.xml, src/, etc.) to compile the project
COPY . .

# Make mvnw executable
RUN chmod +x mvnw

#build the app. delete old biuld and skip tests
RUN ./mvnw clean package -DskipTests

#Starts a second final stage. This is the image that will be run in production.
# in multi-stage Docker build, the first build stage is completely discarded after the final image is built
FROM openjdk:21-jdk

WORKDIR /app

#Copies the compiled JAR file from the first stage(build) into this second stage and rename as app.jar
# this new image will be much smaller because only containes final file not source code and other files.
COPY --from=build /app/target/*.jar app.jar

# Expose the port your app runs on
EXPOSE 8080

# Run the application. This is the command that runs when the container starts.
ENTRYPOINT ["java", "-jar", "app.jar"]
























