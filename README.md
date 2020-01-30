# Uber JVM Profiler

[![Build Status](https://api.travis-ci.org/uber-common/jvm-profiler.svg)](https://travis-ci.org/uber-common/jvm-profiler/)

Uber JVM Profiler provides a Java Agent to collect various metrics and stacktraces for Hadoop/Spark JVM processes 
in a distributed way, for example, CPU/Memory/IO metrics. 

Uber JVM Profiler also provides advanced profiling capabilities to trace arbitrary Java methods and arguments on 
the user code without user code change requirement. This feature could be used to trace HDFS name node call latency 
for each Spark application and identify bottleneck of name node. It could also trace the HDFS file paths each Spark 
application reads or writes and identify hot files for further optimization.

This profiler is initially created to profile Spark applications which usually have dozens of or hundreds of 
processes/machines for a single application, so people could easily correlate metrics of these different 
processes/machines. It is also a generic Java Agent and could be used for any JVM process as well.

## How to Build

1. Make sure JDK 8+ and maven is installed on your machine.
2. Run: `mvn clean install`

This command creates **jvm-profiler.jar** file with the default reporters like ConsoleOutputReporter, FileOutputReporter and KafkaOutputReporter bundled in it. If you want to bundle the custom reporters like RedisOutputReporter or InfluxDBOutputReporter in the jar file then provide the maven profile id for that reporter in the build command. For example to build a jar file with RedisOutputReporter, you can execute `mvn -P redis clean package` command. Please check the pom.xml file for available custom reporters and their profile ids.

## Example to Run with Java Application

Following command will start the example application with the profiler agent attached, which will report metrics to the InfluxDB output:
```
exec java -noverify -javaagent:"/usr/ms/jvm-profiler-1.0.0.jar"=configProvider=com.uber.profiling.YamlConfigProvider,configFile="/usr/ms/config.yaml" -cp "/usr/ms/jvm-profiler-1.0.0.jar" $BEFORE_JAR -jar $MS_JAR $AFTER_JAR
```

`"/usr/ms/jvm-profiler-1.0.0.jar"` - path to the profiler .jar file;

`"/usr/ms/config.yaml"` - path to the config.yaml file.

## Configuration

The java agent supports following parameters, which could be used in Java command line like "-javaagent:agent_jar_file.jar=param1=value1,param2=value2":

- reporter: class name for the reporter, e.g. com.uber.profiling.reporters.ConsoleOutputReporter, or com.uber.profiling.reporters.KafkaOutputReporter, which are already implemented in the code. You could implement your own reporter and set the name here.

- configProvider: class name for the config provider, e.g. com.uber.profiling.YamlConfigProvider, which are already implemented in the code. You could implement your own config provider and set the name here.

- configFile: config file path to be used by YamlConfigProvider (if configProvider is set to com.uber.profiling.YamlConfigProvider). This could be a local file path or HTTP URL.

- tag: plain text string which will be reported together with the metrics.

- metricInterval: how frequent to collect and report the metrics, in milliseconds.

- durationProfiling: configure to profile specific class and method, e.g. com.uber.profiling.examples.HelloWorldApplication.publicSleepMethod. It also support wildcard (*) for method name, e.g. com.uber.profiling.examples.HelloWorldApplication.*. Do not provide “com.*” package as the profiler has a “com.uber” package it will try to profile itself and will throw an error.

- argumentProfiling: configure to profile specific method argument, e.g. com.uber.profiling.examples.HelloWorldApplication.publicSleepMethod.1 (".1" means getting value for the first argument and sending out in the reporter).

- sampleInterval: frequency (milliseconds) to do stacktrace sampling, if this value is not set or zero, the profiler will not do stacktrace sampling.

- ioProfiling: whether to profile IO metrics, could be true or false.

- brokerList: broker list if using com.uber.profiling.reporters.KafkaOutputReporter.

- topicPrefix: topic prefix if using com.uber.profiling.reporters.KafkaOutputReporter. KafkaOutputReporter will send metrics to multiple topics with this value as the prefix for topic names.

- outputDir: output directory if using com.uber.profiling.reporters.FileOutputReporter. FileOutputReporter will write metrics into this directory.

## YAML Config File

The parameters could be provided as arguments in java command, or in a YAML config file if you use configProvider=com.uber.profiling.YamlConfigProvider.

Following is an example of the YAML config file:

```
reporter: com.uber.profiling.reporters.InfluxDBOutputReporter
tag: ${service_name}
metricInterval: 1000
durationProfiling: [com.fasterxml.*, org.*]
sampleInterval: 1000
influxdb.host: ${influx_host}
influxdb.port: 8086
influxdb.database: profiling
influxdb.username:
influxdb.password:

```

In order to be able to filter data in Grafana dashboard for each service separately, you need to pass ${service_name} variable to config.yaml.

## Send Metrics to InfluxDB using config file

Uber JVM Profiler supports sending metrics to InfluxDB.

It will send metrics to InfluxDB. Just change the InfluxDB parameters in `config.yaml` file.

•	influxdb.host: InfluxDB host DNS or IP

•	influxdb.port: InfluxDB port

•	influxdb.database: database name in which the metrics will be saved

•	influxdb.username: InfluxDB login

•	influx.password: InfluxDB password


Also you can find Grafana dashboard in `dashboards` folder in this repository.

## Grafana dashboards

Grafana dashboard allows us to analyze the results for each service individually. To do this, you need to configure a service filter.

First part of the dashboard is a panel with slowest java methods. Each method is a link, if you click on it, a new tab will open a panel with a detailed stacktrace for this method.

This stacktrace is a set of all stacktraces that were performed during a test with this method.
You can expand them by clicking on the arrow and go all the way from the selected method to the beginning of the stacktrace.
The red color indicates the path that stacktrace most often follows. Also, on each part of stacktrace there is a percentage ratio of how often this method was executed.
It may be useful for analysis of non-optimal program execution.

The last part of the dashboard is Java metrics such as CPU usage, heap memory, etc.


## More Details

See [JVM Profiler Blog Post](https://eng.uber.com/jvm-profiler/).