package br.ufpe.cin.datarecommendation;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class DataRecommender {

	
	static String energyFilePath;
	static String dataAnalysisFilePath;
	static DecimalFormat df = new DecimalFormat("#.00");
	
	public static void doRecommendation(String energyFilePath,ArrayList<CollectionMethod> methods) throws IOException{
		
		ArrayList<EnergyProfile> energyProfilesFromFile = ReadFile.getEnergyProfilesFromFile(energyFilePath);		
		
		for (EnergyProfile energyProfile : energyProfilesFromFile) {
			System.out.println(energyProfile);
		}
		
		EnergyProfileManager profileManager = new EnergyProfileManager(energyProfilesFromFile);
		DataStructureEnergyManager dataEnergyManager = new DataStructureEnergyManager(methods,profileManager);
		HashMap<String, HashMap<String,Double>> recommendationOrder = dataEnergyManager.getRecommendationOrder();
		
		
		HashMap<String, String> typesReccommended = new HashMap<String, String>();
		 
		for (String name : recommendationOrder.keySet()) {
			
			HashMap<String,Double> typeConsumption = recommendationOrder.get(name);
			for (String type : typeConsumption.keySet()) {				
				System.out.println(name +";"+type+";"+df.format(typeConsumption.get(type)));
			}	
			
			typesReccommended.put(name,getStructureRecommendation(typeConsumption));			
		}
		
		for (String nome : typesReccommended.keySet()) {
			System.out.println(nome+";"+typesReccommended.get(nome));
		}
		
	}
	
	private void printTypesRecommendedNumbers(ArrayList<String> typesReccommended){
		
		for (String string : typesReccommended) {
			
		}
	}
	
	
	private static String getStructureRecommendation(HashMap<String,Double> typeConsumption){
		
		String typeR = "";
		Double minor = new Double(0);
		
		for (String type : typeConsumption.keySet()) {
			if(typeR.isEmpty()){
				typeR = type;
				minor = typeConsumption.get(type);
			}else{
				Double double1 = typeConsumption.get(type);
				if(double1<minor){
					typeR = type;
					minor = typeConsumption.get(type);
				}				
			}
		}
		
		return typeR;		
	}
	
	public static void main(String[] args) {
		try {			
			
			energyFilePath = "/home/ros/Documents/Mestrado/Analise/Profile.csv";
			dataAnalysisFilePath = "/home/ros/Documents/Mestrado/Analise/analise.csv";
			
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
