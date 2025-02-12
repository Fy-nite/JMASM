# compile the java code
mvn clean install package
# run the jar file
java -jar $(find target -name "*dependencies.jar") "$@"