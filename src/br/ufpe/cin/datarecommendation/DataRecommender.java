package br.ufpe.cin.datarecommendation;

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

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class DataRecommender {

	
	static String energyFilePath;
	static String dataAnalysisFilePath;
	
	public static void doRecommendation(String energyFilePath,ArrayList<CollectionMethod> methods) throws IOException{
		
		ArrayList<EnergyProfile> energyProfilesFromFile = ReadFile.getEnergyProfilesFromFile(energyFilePath);		
		
		for (EnergyProfile energyProfile : energyProfilesFromFile) {
			System.out.println(energyProfile);
		}
		
		EnergyProfileManager profileManager = new EnergyProfileManager(energyProfilesFromFile);
		DataStructureEnergyManager dataEnergyManager = new DataStructureEnergyManager(methods,profileManager);
		TreeMap<CollectionMethod, HashMap<String,Double>> recommendationOrder = dataEnergyManager.getRecommendationOrder();
		
		
		HashMap<CollectionMethod, String> typesReccommended = new HashMap<CollectionMethod, String>();
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		
		for (CollectionMethod methodInfo : recommendationOrder.keySet()) {
			
			HashMap<String,Double> typeConsumption = recommendationOrder.get(methodInfo);
			for (String type : typeConsumption.keySet()) {				
				System.out.println(methodInfo.getFieldName() +";"+type+";"+typeConsumption.get(type));
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
				typesReccommended.put(methodInfo,"Changes the type from "+methodInfo.getConcreteType()+" to \""+orderedRecommendations+"\"");
			}
		}
		
		class Result {
			public String fieldName;
			public String classContainingField;
			public String recommendation;
			public String methodUsingVariable;
		}
		List<Result> results = new ArrayList<Result>();
		for (CollectionMethod methodInfo : typesReccommended.keySet()) {
			Result result = new Result();
			result.fieldName = methodInfo.getFieldName();
			result.classContainingField = methodInfo.getClasse();
			result.recommendation = typesReccommended.get(methodInfo);
			result.methodUsingVariable = methodInfo.getCallMethodName();
			results.add(result);
			//System.out.println(methodInfo.getFieldName()+";"+typesReccommended.get(methodInfo));
		}
		Collections.sort(results, new Comparator<Result>() {
			@Override
			public int compare(Result o1, Result o2) {
				return o1.classContainingField.compareTo(o2.classContainingField);
			}
		});
		
		for(Result result : results) {
			System.out.println(result.fieldName+";"+"Method that uses the variable:"+result.methodUsingVariable+";"+result.recommendation);
		}
	}
	
	private static SortedSet<RecommendedStructure> getStructureRecommendation(HashMap<String,Double> typeConsumption){
		
		String typeR = "";
		Double minor = new Double(0);
		
		SortedSet<RecommendedStructure> recommendations = new TreeSet<RecommendedStructure>();
		
		for (String type : typeConsumption.keySet()) {
			/*if(typeR.isEmpty()){
				typeR = type;
				minor = typeConsumption.get(type);
			}else{
				Double double1 = typeConsumption.get(type);
				if(double1<minor){
					typeR = type;
					minor = typeConsumption.get(type);
				}				
			}*/
			recommendations.add(new RecommendedStructure(type, typeConsumption.get(type)));
		}
		
		return recommendations;		
	}
	
	public static void main(String[] args) {
		try {			
			
			energyFilePath = "C:\\Users\\RENATO\\Documents\\Mestrado\\Energy profiles\\complete-profile-inspirondell-7560-50k.csv";
			dataAnalysisFilePath = "C:\\Users\\RENATO\\Documents\\analise.csv";
			
			//energyFilePath = "energyData/energy-profile-note.csv";
			if(args.length>0){
				energyFilePath = args[0];
			}			
			
			//dataAnalysisFilePath = "energyData/analise-tomcat-explicit.csv";
			
			ArrayList<CollectionMethod> collectionMethodFromFile = ReadFile.getCollectionMethodFromFile(dataAnalysisFilePath);
			
			doRecommendation(energyFilePath, collectionMethodFromFile);			
			
		} catch (IOException e) {
			System.out.println("File not found");
			e.printStackTrace();
		}
	}

	
	
	
	
}
