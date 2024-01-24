# Producer Consumer - FIFO Queue

Program functionality demonstrates by running the main method.

## Prerequisites

* Java 17
* Gradle 7.6 or higher

## Run with Gradle

```
gradlew run
```

## Test with Gradle

```
gradlew test
```

## Run with Java Only

### Compile Application

```
javac -classpath lib/derby-10.16.1.1.jar;lib/derbyshared-10.16.1.1.jar;. -d target -sourcepath src/main/java src/main/java/org/twohead/zadanie/App.java
```

### Run Application

```
java -classpath lib/derby-10.16.1.1.jar;lib/derbyshared-10.16.1.1.jar;target org.twohead.zadanie.App
```

## Test with Java Only

### Compile Tests

Compile the application first.

```
javac -classpath lib/junit-4.13.2.jar;lib/derby-10.16.1.1.jar;lib/derbyshared-10.16.1.1.jar;target -d target -sourcepath src/test/java;src/main/java src/test/java/org/twohead/zadanie/UserQueueTest.java
```

### Run Tests

```
java -cp lib/junit-4.13.2.jar;lib/hamcrest-core-1.3.jar;lib/derby-10.16.1.1.jar;lib/derbyshared-10.16.1.1.jar;target org.junit.runner.JUnitCore org.twohead.zadanie.UserQueueTest
```


## Note

Whene running from linux based OS please use <b>:</b> instead of <b>;</b> as the path separator in the classpath.