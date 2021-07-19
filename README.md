# TB-Profiler
Command-line tool created for  [TaintBench](https://taintbench.github.io).

# Build
Build the project with Maven: ``mvn install``

# Run
Run ``java -jar tb-profiler-0.0.1.jar'`` followed by the following command line option: 

1. profile an apk statically to check whether the attributes in the [TAF-file](https://github.com/TaintBench/TaintBench/blob/master/TAF-schema.json) are correct:

``
 -apk <apk> -f <TAF-file>  -p <android platform jars> -c <path to
           configuration files>
``

2. calculate statistics of sources and sinks appears in the apk based on two lists ([merged_sources.txt](config/merged_sources.txt), [merged_sinks.txt](config/merged_sinks.txt)):

``
 -statSS <apks> -p <android platform jars> -c <path to configuration
           files>
``

3. calculate statistics of attributes read from a list of TAF-files:

``
  -statAttr <TAF-files>
``
