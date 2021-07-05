cd C:\Users\JovanJ\Desktop\Developer\Collector-ACTUAL
set path=%path%;C:\Program Files\Java\jdk-15.0.1\bin
set classpath=%classpath=%;.;C:\Users\JovanJ\Desktop\Developer\Collector-ACTUAL\lib\mssql-jdbc-7.4.1.jre12.jar;C:\Users\JovanJ\Desktop\Developer\Collector-ACTUAL\lib\org.json-chargebee-1.0.jar
javac collector.java
java Collector
