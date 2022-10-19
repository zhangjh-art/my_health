FROM java:8-jre
VOLUME /app
ADD user-manager-service-app.jar /app/user-server.jar
RUN sh -c 'touch /app/user-server.jar'
ENV JAVA_OPTS="-Xmx1024m"
ENTRYPOINT [ "sh", "-c", "java $JAVA_OPTS -jar /app/user-server.jar" ]