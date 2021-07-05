cd C:\Users\DanielC\Desktop\Developer\Collector-new-version
set path=%path%;C:\Program Files\Java\jdk-15.0.1\bin
set classpath=%classpath=%;.;C:\Users\DanielC\Desktop\Developer\Collector-new-version\lib\mssql-jdbc-9.1.1.jre15.jar;C:\Users\DanielC\Desktop\Developer\Collector-new-version\lib\org.json-chargebee-1.0.jar
javac collector.java
java Collector
