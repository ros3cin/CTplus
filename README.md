# In order to run this project you will need:
1. The Scala IDE 2.12.3 (http://scala-ide.org/download/current.html)
2. Import the project as a Maven project
3. Right click on the project - Build path - Configure build path
4. On the 'Source' tab, scroll down to 'CECOTool/src'
5. Click on the 'Included: **/*.java' option and then click on 'Edit...'
6. On the 'Inclusion and Exclusion Patterns' window, click 'Add..' and type the following pattern: **/*.scala
7. Click 'Finish'


# Analyzing an APK

> On the JavaCollectionsAnalyzer class

## 1. Set a path for you analysis output file:
```
line 145: private static String CAMINHO_CSV = "/Path/To/My/Analysis/Output/";
```

## 2. Inform the path of your APK:
```
line 203: 
AnalysisScope scope = AndroidAnalysisScope.setUpAndroidAnalysisScope(
				(new File("/Path/To/My/Apk.apk")).toURI(), 
				EXCLUSOES,
				JavaCollectionsAnalyser.class.getClassLoader()
		);
```

> You can use the default 'EXCLUSOES' file

## 3. Inform the components on interest you want the analyzer to check.
```
java.util.List<ComponentOfInterest> fastSearchComponentsOfInterest = new ArrayList<ComponentOfInterest>();
		fastSearchComponentsOfInterest.add(new ComponentOfInterest("org/ligi/fast", null, null));
```
### 3.1 Pass it to the analyzer
```
line 252:
traverseMethods(cha,fastSearchComponentsOfInterest);
```

## 4. Run the analyzer

# Running the recommender

> On the DataRecommender class

## 1. Inform the path of your energy profile and of your analysis output file

```
line 113:
energyFilePath = "/Path/To/My/energy_profile.csv";
dataAnalysisFilePath = "/Path/To/My/Analysis/Output/analise.csv";
```

## 2. Run the recommender

The output is going to be printed on the IDE console. The important output are the last ones, indicating the variable name, the class in which it resides, one of the methods which use it and the recommendation.

Sample lines are shown below:
```
variable1-Lpath/to/Clazz1>;Method that uses the variable:aMethodName1;Changes the type from Ljava/util/ArrayList to "treeList(ApacheCommonsCollections)<fastList(EclipseCollections)
variable2-Lpath/to/Clazz2>;Method that uses the variable:aMethodName2;Changes the type from Ljava/util/List to "fastList(EclipseCollections)<treeList(ApacheCommonsCollections)<arrayList
```
> HINT: You may copy this output and "special paste" it into a spreadsheet as an unformatted text, choosing "-" and ";" as separators

Note that sometimes WALA is not able to infer the concrete type of an object, outputting the parent class. When this happens on the collections, there is no way to tell whether it is synchronized or not, so the recommendation includes both kinds of collections and it is the developer's responsibility to check the thread-safeness before changing it.



