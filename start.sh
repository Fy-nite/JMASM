# compile the java code
mvn clean package -Dmaven.test.skip=true
# run the jar file
java -jar $(find target -name "*dependencies.jar")  "$@"