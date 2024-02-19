FROM flowable/flowable-ui:latest

# Install Maven
USER root
RUN apk add --no-cache maven

# Copy the custom package into the image
COPY modules/flowable-ui/ /app/

# Build the Flowable UI application
WORKDIR /app/
RUN mvn clean package -DskipTests

# Set the classpath
ENV CLASSPATH=/app/modules/flowable-ui/flowable-ui-app/target/classes:${CLASSPATH}

# Expose the default Flowable UI port
EXPOSE 8080

# Run the Flowable UI application
CMD ["java", "-jar", "flowable-ui.war"]
