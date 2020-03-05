# slf4j-boodskap-appender
SLF4J / LOG4J Boodskap Platform Appender

# Maven Dependency
```xml
    <dependencies>
        <dependency>
                <groupId>io.boodskap.iot.ext</groupId>
                <artifactId>slf4j-boodskap-appender</artifactId>
                <version>1.0.0</version>
        </dependency>
    </dependencies>
```

# Sample log4j.properties Configuration
```properties
# Root logger option
log4j.rootCategory=INFO, boodskap

# Direct log messages to Boodskap Platform
log4j.appender.boodskap=io.boodskap.iot.ext.log4j.BoodskapAppender
log4j.appender.boodskap.layout=org.apache.log4j.PatternLayout

#Refer to Log4J PatternLayout for more details
log4j.appender.boodskap.layout.ConversionPattern=%d{yyyy-MM-dd HH:mm:ss.SSS} %-5p %c{1}:%L - %m%n

#Enable / Disable synchronous logging, enabling may introduce considerable delay in the execution
log4j.appender.boodskap.sync=false

#Max numer of log events to be buffered in the memory
log4j.appender.boodskap.queueSize=10000

#Boodskap platform API base path
log4j.appender.boodskap.apiBasePath=

#Boodskap platform's Domain Key
log4j.appender.boodskap.domainKey=

#Boodskap platform's API Key
log4j.appender.boodskap.apiKey=

#Boodskap Log Analyzer's Application ID
log4j.appender.boodskap.appId=

```
