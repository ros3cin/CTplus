package br.ufpe.cin.datarecommendation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import br.ufpe.cin.dataanalysis.CollectionMethod;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedAlias;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedClass;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedInstanceField;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedLocalVariable;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedMethod;
import br.ufpe.cin.dataanalysis.pointeranalysis.AnalyzedStaticField;
import br.ufpe.cin.dataanalysis.pointeranalysis.PointerAnalysisVariableType;
import br.ufpe.cin.debug.Debug;

public class DataRecommender {

	
	static String energyFilePath;
	static String dataAnalysisFilePath;
	
	public static void doRecommendation(String energyFilePath,ArrayList<CollectionMethod> methods, String recommendationOutputFile,Map<String, AnalyzedClass> pointers) throws IOException{
		
		ArrayList<EnergyProfile> energyProfilesFromFile = ReadFile.getEnergyProfilesFromFile(energyFilePath);		
		
		for (EnergyProfile energyProfile : energyProfilesFromFile) {
			Debug.debug(energyProfile.toString());
		}
		
		EnergyProfileManager profileManager = new EnergyProfileManager(energyProfilesFromFile);
		DataStructureEnergyManager dataEnergyManager = new DataStructureEnergyManager(methods,profileManager);
		TreeMap<CollectionMethod, HashMap<String,Double>> recommendationOrder = dataEnergyManager.getRecommendationOrder();
		
		
		Map<CollectionMethod, List<String>> typesReccommended = new HashMap<CollectionMethod, List<String>>();
		Map<String,List<String>> recommendationsByKey = new HashMap<String,List<String>>();
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		ICollectionTypeCompatibility typeCompatibility = new CollectionTypeCompatibility();
		
		for (CollectionMethod methodInfo : recommendationOrder.keySet()) {
			HashMap<String,Double> typeConsumption = recommendationOrder.get(methodInfo);
			for (String type : typeConsumption.keySet()) {				
				Debug.debug(methodInfo.getFieldName() +";"+type+";"+typeConsumption.get(type));
			}	
			SortedSet<RecommendedStructure> recommendations = getStructureRecommendation(typeConsumption);
			
			if(!typeResolver.isSameCollection(methodInfo.getConcreteType(), recommendations.first().getStructure())) {
				List<String> orderedRecommendations = new ArrayList<String>();
				for(RecommendedStructure recStructure : recommendations) {
					if(typeResolver.isSameCollection(methodInfo.getConcreteType(), recStructure.getStructure()))
						break;
					Set<String> calledConstructors = methodInfo.getCalledConstructors();
					if (calledConstructors != null) {
						boolean isCompatible = true;
						for(String constructor : calledConstructors) {
							if (!typeCompatibility.hasConstructor(constructor, recStructure.getStructure())) {
								isCompatible = false;
								break;
							}
						}
						if(!isCompatible) {
							continue;
						}
					}
					if(!typeCompatibility.canReplace(methodInfo.getConcreteType(), recStructure.getStructure()))
						continue;
					orderedRecommendations.add(recStructure.getStructure());
				}
				if(!orderedRecommendations.isEmpty()) {
					typesReccommended.put(methodInfo,orderedRecommendations);
					String key = "";
					if(methodInfo.isFieldLocal()) {
						key = String.format(
								"%s-%s-%d-%s",
								methodInfo.getClasse(),
								methodInfo.getCallMethodName(),
								methodInfo.getCallMethodNumOfParams(),
								methodInfo.getFieldName()
								);
					} else {
						key = String.format(
								"%s-%s",
								methodInfo.getClasse(),
								methodInfo.getFieldName()
								);
					}
					recommendationsByKey.put(key, orderedRecommendations);
				}
			}
		}
		
		Map<CollectionMethod, List<String>> cleanTypesRecommendation = new HashMap<CollectionMethod, List<String>>();
		for(CollectionMethod methodInfo : typesReccommended.keySet()) {
			boolean shouldAdd = true;
			//If inferred type is an interface, the class or variable is probably defined somewhere else, then continue
			if(typeResolver.isInterface(methodInfo.getConcreteType())) {
				shouldAdd = false;
			}
			else if(methodInfo.isCollectionReturnedOrPassedAsParameter()) {
				shouldAdd = false;
				if (pointers != null) {
					String key = "";
					if(methodInfo.isFieldLocal()) {
						key = String.format(
								"%s-%s-%d-%s",
								methodInfo.getClasse(),
								methodInfo.getCallMethodName(),
								methodInfo.getCallMethodNumOfParams(),
								methodInfo.getFieldName()
						);
					} else {
						key = String.format(
								"%s-%s",
								methodInfo.getClasse(),
								methodInfo.getFieldName()
						);
					}
					//using the first recommendation only
					List<String> recommendations = typesReccommended.get(methodInfo);
					typesReccommended.put(methodInfo,recommendations.subList(0, 1));
					shouldAdd = isRecommendationTheSame(
							pointers,
							recommendationsByKey,
							typesReccommended.get(methodInfo),
							key,
							methodInfo.isFieldLocal(),
							new HashSet<String>());
				} 
			}
			if(shouldAdd) {
				cleanTypesRecommendation.put(methodInfo, typesReccommended.get(methodInfo));
			}
		}
		
		class Result {
			public String fieldName;
			public String classContainingField;
			public String originalCollection;
			public String recommendation;
			public String methodUsingVariable;
			public boolean isFieldLocal;
			public String sourceCodeLine;
		}
		List<Result> results = new ArrayList<Result>();
		for (CollectionMethod methodInfo : cleanTypesRecommendation.keySet()) {
			Result result = new Result();
			result.fieldName = methodInfo.getFieldName();
			result.classContainingField = methodInfo.getClasse();
			StringBuilder recommendations = new StringBuilder();
			for(String recommendation : typesReccommended.get(methodInfo)) {
				recommendations.append(recommendation);
				recommendations.append("<");
			}
			if(!StringUtils.isEmpty(recommendations))
				recommendations.deleteCharAt(recommendations.length()-1);
			result.originalCollection = methodInfo.getConcreteType();
			result.recommendation = recommendations.toString();
			result.methodUsingVariable = methodInfo.getCallMethodName();
			result.isFieldLocal = methodInfo.isFieldLocal();
			result.sourceCodeLine = methodInfo.getInstanceAssignmentsLineNumbersAsString();
			if (!StringUtils.isEmpty(result.sourceCodeLine) && !StringUtils.isEmpty(recommendations))
				results.add(result);
		}
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				return o1.classContainingField.compareTo(o2.classContainingField);
			}
		});
		CSVPrinter printer = new CSVPrinter(new FileWriter(recommendationOutputFile), CSVFormat.DEFAULT);
		printer.printRecord(
				"Field name",
				"Is local?",
				"Source code line",
				"Containing class",
				"A method that uses it",
				"Original collection",
				"Ordered recommendations",
				"Choosen recommendation (used by the CECOTool transfomer, change it accordingly)"
		);
		for(Result result : results) {
			printer.printRecord(
					result.fieldName,
					result.isFieldLocal,
					result.sourceCodeLine,
					result.classContainingField,
					result.methodUsingVariable,
					result.originalCollection,
					result.recommendation,
					"1"
			);
		}
		printer.flush();
		printer.close();
	}
	
	private static boolean isRecommendationTheSame(
			Map<String, AnalyzedClass> pointers,
			Map<String,List<String>> recommendationsByKey,
			List<String> mainOrderedRecommendations,
			String currKey,
			boolean isCurrLocalField,
			Set<String> visited) {
		StringTokenizer strTok = new StringTokenizer(currKey,"-");
		Map<String, AnalyzedAlias> aliases = null;
		visited.add(currKey);
		if ((recommendationsByKey.get(currKey) != null) && !recommendationsByKey.get(currKey).get(0).equals(mainOrderedRecommendations.get(0))) {
			return false;
		}
		if (isCurrLocalField) {
			String className = strTok.nextToken();
			String methodName = strTok.nextToken();
			String methodNumOfParams = strTok.nextToken();
			String methodKey = String.format("%s-%s",methodName,methodNumOfParams);
			String localVariableName = strTok.nextToken();
			if (pointers.containsKey(className) &&
					pointers.get(className).getAnalyzedMethods().containsKey(methodKey) &&
					pointers.get(className).getAnalyzedMethods().get(methodKey).getAnalyzedLocalVariables().containsKey(localVariableName)
					) {
				aliases = pointers.get(className).getAnalyzedMethods().get(methodKey).getAnalyzedLocalVariables().get(localVariableName).getAliases();
			} else {
				return true;
			}
		} else {
			String className = strTok.nextToken();
			String fieldName = strTok.nextToken();
			if (pointers.containsKey(className) &&
					(pointers.get(className).getAnalyzedInstanceFields().containsKey(fieldName) ||
					pointers.get(className).getAnalyzedStaticFields().containsKey(fieldName))
					) {
				if(pointers.get(className).getAnalyzedInstanceFields().containsKey(fieldName)) {
					aliases = pointers.get(className).getAnalyzedInstanceFields().get(fieldName).getAliases();
				} else {
					aliases = pointers.get(className).getAnalyzedStaticFields().get(fieldName).getAliases();
				}
			} else {
				return true;
			}
		}
		if (aliases != null) {
			boolean aliasesResult = true;
			for (AnalyzedAlias alias : aliases.values()) {
				if (!visited.contains(alias.toString())) {
					aliasesResult = aliasesResult && isRecommendationTheSame(
							pointers,
							recommendationsByKey,
							mainOrderedRecommendations,
							alias.toString(),
							alias.getType() == PointerAnalysisVariableType.LOCAL_VARIABLE,
							visited
					);
				}
			}
			return aliasesResult;
		} else {
			return true;
		}
	}
	
	private static SortedSet<RecommendedStructure> getStructureRecommendation(HashMap<String,Double> typeConsumption){
		SortedSet<RecommendedStructure> recommendations = new TreeSet<RecommendedStructure>();
		
		for (String type : typeConsumption.keySet()) {
			recommendations.add(new RecommendedStructure(type, typeConsumption.get(type)));
		}
		
		return recommendations;		
	}
	
	public static void run(String energyProfileFile, String analysisOutputFile, String recommendationOutputFile, String pointsToAnalysisFilePath) throws IOException {
		Debug.info(String.format("Generating the recommendations at %s...",recommendationOutputFile));
		ArrayList<CollectionMethod> collectionMethodFromFile = ReadFile.getCollectionMethodFromFile(analysisOutputFile);
		Map<String, AnalyzedClass> pointers = extractPointsToAnalysisResult(pointsToAnalysisFilePath);
		doRecommendation(energyProfileFile, collectionMethodFromFile,recommendationOutputFile,pointers);	
		Debug.info("Done");
	}

	private static Map<String, AnalyzedClass> extractPointsToAnalysisResult(String filePath) throws FileNotFoundException {
		Gson gson = new Gson();
		Type mapType = new TypeToken<Map<String, AnalyzedClass>>(){}.getType();
		Map<String, AnalyzedClass> pointerResult = null;
		File file = new File(filePath); 
		if(file.exists() && !file.isDirectory()) {
			pointerResult = gson.fromJson(new FileReader(filePath), mapType);
			for (AnalyzedClass analyzedClass : pointerResult.values()) {
				for (AnalyzedMethod analyzedMethod : analyzedClass.getAnalyzedMethods().values()) {
					analyzedMethod.setDeclaringClass(analyzedClass);
					for (AnalyzedLocalVariable variable : analyzedMethod.getAnalyzedLocalVariables().values()) {
						variable.setDeclaringMethod(analyzedMethod);
					}
				}
			}
		}
		return pointerResult;
	}
	
	public static void main(String[] args) throws IOException {		
		run(
				"C:\\Users\\RENATO\\Documents\\Mestrado\\Energy profiles\\complete profile nexus 7.csv",
				"analysis.csv",
				"nexus 7 commons math recommendations.csv",
				"commons math3 points-to-analysis.json"
		);
	}

	
	
	
	
}
