rm -f ActivityStreamerServer.jar ActivityStreamerClient.jar
mvn clean
mvn package

mv target/Client-jar-with-dependencies.jar ActivityStreamerClient.jar
mv target/Server-jar-with-dependencies.jar ActivityStreamerServer.jar
