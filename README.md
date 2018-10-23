# In order to run this project you will need:
1. The Scala IDE 2.12.3 (http://scala-ide.org/download/current.html)
2. Import the project as a Maven project
3. Right click on the project - Build path - Configure build path
4. On the 'Source' tab, scroll down to 'CECOTool/src'
5. Click on the 'Included: **/*.java' option and then click on 'Edit...'
6. On the 'Inclusion and Exclusion Patterns' window, click 'Add..' and type the following pattern: **/*.scala
7. Click 'Finish'

# To generate the output jar, do:
1. mvn -Dhttps.protocols=TLSv1.2 clean package

# JAR usage:
```
Usage: <main class> [-ahr] [--analysis-output=<analysisOutputFile>]
                    [-e=<exclusions>] [-p=<pointsToAnalysis>] [-t=<target>]
                    [--packages=<packages>]...
      --analysis-output=<analysisOutputFile>
                          The name of the analysis output file. Defaults to analysis.
                            csv
      --packages=<packages>
                          Packages to include in the analysis
  -a, --analyze           Run analyzer, this flag is true by default
  -e, --exclusions=<exclusions>
                          The path to the scope exclusion file
  -h, --help              Displays this help
  -p, --points-to-analysis=<pointsToAnalysis>
                          If set, runs points-to-analysis and generates a file
  -r, --recommend         Run recommender, this flag is false by default
  -t, --target=<target>   The target JAR or APK
```