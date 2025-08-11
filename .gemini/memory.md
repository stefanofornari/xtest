The user prefers JUnit 4 for tests.
We want always the minimal implementation that makes the tests pass.

Maven Test Instructions:
- JAVA_HOME: /opt/jdk-24.0.1
- Maven Executable: /opt/apache-maven-3.9.5/bin/mvn
- To run specific tests, use: /opt/apache-maven-3.9.5/bin/mvn -Dtest=<TestClassName> test
- When working on a single functionality, run only that specific test method: /opt/apache-maven-3.9.5/bin/mvn -Dtest=<TestClassName>#<testMethodName> test