#!/bin/env pwsh
mvn clean install package
# run the jar file
java -jar target/MASM-1.0-SNAPSHOT-jar-with-dependencies.jar $args
