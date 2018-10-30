package br.ufpe.cin.datarecommendation;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

import br.ufpe.cin.dataanalysis.CollectionMethod;
import br.ufpe.cin.debug.Debug;

public class DataRecommender {

	
	static String energyFilePath;
	static String dataAnalysisFilePath;
	
	public static void doRecommendation(String energyFilePath,ArrayList<CollectionMethod> methods, String recommendationOutputFile) throws IOException{
		
		ArrayList<EnergyProfile> energyProfilesFromFile = ReadFile.getEnergyProfilesFromFile(energyFilePath);		
		
		for (EnergyProfile energyProfile : energyProfilesFromFile) {
			Debug.println(energyProfile.toString());
		}
		
		EnergyProfileManager profileManager = new EnergyProfileManager(energyProfilesFromFile);
		DataStructureEnergyManager dataEnergyManager = new DataStructureEnergyManager(methods,profileManager);
		TreeMap<CollectionMethod, HashMap<String,Double>> recommendationOrder = dataEnergyManager.getRecommendationOrder();
		
		
		HashMap<CollectionMethod, String> typesReccommended = new HashMap<CollectionMethod, String>();
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		
		for (CollectionMethod methodInfo : recommendationOrder.keySet()) {
			
			HashMap<String,Double> typeConsumption = recommendationOrder.get(methodInfo);
			for (String type : typeConsumption.keySet()) {				
				Debug.println(methodInfo.getFieldName() +";"+type+";"+typeConsumption.get(type));
			}	
			SortedSet<RecommendedStructure> recommendations = getStructureRecommendation(typeConsumption);
			
			if(!typeResolver.isSameCollection(methodInfo.getConcreteType(), recommendations.first().getStructure())) {
				String orderedRecommendations = recommendations.first().getStructure();
				recommendations.remove(recommendations.first());
				for(RecommendedStructure recStructure : recommendations) {
					if(typeResolver.isSameCollection(methodInfo.getConcreteType(), recStructure.getStructure()))
						break;
					orderedRecommendations += String.format("<%s",recStructure.getStructure());
				}
				typesReccommended.put(methodInfo,"Changes the type from "+methodInfo.getConcreteType()+" to "+orderedRecommendations);
			}
		}
		
		class Result {
			public String fieldName;
			public String classContainingField;
			public String recommendation;
			public String methodUsingVariable;
			public boolean isFieldLocal;
		}
		List<Result> results = new ArrayList<Result>();
		for (CollectionMethod methodInfo : typesReccommended.keySet()) {
			Result result = new Result();
			result.fieldName = methodInfo.getFieldName();
			result.classContainingField = methodInfo.getClasse();
			result.recommendation = typesReccommended.get(methodInfo);
			result.methodUsingVariable = methodInfo.getCallMethodName();
			result.isFieldLocal = methodInfo.isFieldLocal();
			results.add(result);
		}
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				return o1.classContainingField.compareTo(o2.classContainingField);
			}
		});
		CSVPrinter printer = new CSVPrinter(new FileWriter(recommendationOutputFile), CSVFormat.DEFAULT);
		printer.printRecord("Field name", "Is local?", "Containing class", "A method that uses it", "Ordered recommendations");
		for(Result result : results) {
			printer.printRecord(result.fieldName, result.isFieldLocal, result.classContainingField, result.methodUsingVariable, result.recommendation);
		}
		printer.close();
	}
	
	private static SortedSet<RecommendedStructure> getStructureRecommendation(HashMap<String,Double> typeConsumption){
		
		String typeR = "";
		Double minor = new Double(0);
		
		SortedSet<RecommendedStructure> recommendations = new TreeSet<RecommendedStructure>();
		
		for (String type : typeConsumption.keySet()) {
			recommendations.add(new RecommendedStructure(type, typeConsumption.get(type)));
		}
		
		return recommendations;		
	}
	
	public static void run(String energyProfileFile, String analysisOutputFile, String recommendationOutputFile) throws IOException {
		Debug.logger.info(String.format("Generating the recommendations at %s...",recommendationOutputFile));
		ArrayList<CollectionMethod> collectionMethodFromFile = ReadFile.getCollectionMethodFromFile(analysisOutputFile);
		doRecommendation(energyProfileFile, collectionMethodFromFile,recommendationOutputFile);	
		Debug.logger.info("Done");
	}
	
	public static void main(String[] args) {
		try {			
			energyFilePath = "C:\\Users\\RENATO\\Documents\\Mestrado\\Energy profiles\\complete-profile-inspirondell-7560-50k.csv";
			dataAnalysisFilePath = "C:\\Users\\RENATO\\scala-ide-workspace\\CECOTool\\target\\analysis.csv";
			
			if(args.length>0){
				energyFilePath = args[0];
			}			
			
			ArrayList<CollectionMethod> collectionMethodFromFile = ReadFile.getCollectionMethodFromFile(dataAnalysisFilePath);
			doRecommendation(energyFilePath, collectionMethodFromFile,"recommendations.csv");			
		} catch (IOException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}

	
	
	
	
}
