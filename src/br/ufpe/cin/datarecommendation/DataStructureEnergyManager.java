package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class DataStructureEnergyManager {
	
	
	private ArrayList<CollectionMethod> methods;
	
	//For each method we have a type (ConcurrentHashMap, Vector etc) and a value for the energy consumption
	private ArrayList<MethodEnergy> methodsEnergyMap;
	private ArrayList<MethodEnergy> methodsEnergyList;
	private ArrayList<MethodEnergy> methodsEnergySet;

	private EnergyProfileManager profileManager;
	

	public DataStructureEnergyManager(ArrayList<CollectionMethod> methods, EnergyProfileManager profileManager) {
		super();
		this.methods = methods;
		this.profileManager = profileManager;
		methodsEnergyMap = new ArrayList<MethodEnergy>();
		methodsEnergyList = new ArrayList<MethodEnergy>();
		methodsEnergySet = new ArrayList<MethodEnergy>();
		 
		fillMethodsEnergy();		
	}


	private void fillMethodsEnergy() {
		
		
		for (CollectionMethod collectionMethod : methods) {
			HashMap<String, Double> consumptions = new HashMap<String, Double>();
			
			if(isMap(collectionMethod)){
				
				if(collectionMethod.getNome().equals("put") 
						|| collectionMethod.getNome().equals("remove") 
						|| collectionMethod.getNome().equals("get")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getMapTypes(),collectionMethod.getNome());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergyMap.add(method);					
				} 
				
			} else if(isList(collectionMethod)){
				
				if(collectionMethod.getNome().equals("add") 
						|| collectionMethod.getNome().equals("addElement") 
						|| collectionMethod.getNome().equals("remove") 
						|| collectionMethod.getNome().equals("get")
						|| collectionMethod.getNome().equals("elementAt")
						
						|| collectionMethod.getNome().equals("add(value)")
						|| collectionMethod.getNome().equals("add(starting-index;value)")
						|| collectionMethod.getNome().equals("add(middle-index;value)")
						|| collectionMethod.getNome().equals("add(ending-index;value)")
						
						|| collectionMethod.getNome().equals("remove(starting-index)")
						|| collectionMethod.getNome().equals("remove(middle-index)")
						|| collectionMethod.getNome().equals("remove(ending-index)")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getListTypes(),collectionMethod.getNome());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergyList.add(method);					
				} 
				
			} else if(isSet(collectionMethod)){
				if(collectionMethod.getNome().equals("add") 
						|| collectionMethod.getNome().equals("remove") 
						|| collectionMethod.getNome().equals("iterator")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getSetTypes(),collectionMethod.getNome());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergySet.add(method);					
				} 
			}			
		}
		
	}


	private boolean isMap(CollectionMethod collectionMethod) {
		return collectionMethod.getConcreteType().toLowerCase().contains("hashtable")
				|| collectionMethod.getConcreteType().toLowerCase().contains("concurrenthashmap")
				|| collectionMethod.getConcreteType().toLowerCase().equals("ljava/util/map") 
				|| collectionMethod.getConcreteType().toLowerCase().contains("concurrentskiplistmap");
	}
	
	private boolean isList(CollectionMethod collectionMethod) {
		String methodStr = collectionMethod.getConcreteType().toLowerCase();
		return collectionMethod.getConcreteType().toLowerCase().contains("vector")
				|| collectionMethod.getConcreteType().toLowerCase().equals("ljava/util/list") 
				|| collectionMethod.getConcreteType().toLowerCase().contains("copyonwritearraylist")
				|| (methodStr.contains("list")&&!methodStr.contains("map")&&!methodStr.contains("set"));
	}
	
	private boolean isSet(CollectionMethod collectionMethod) {
		return collectionMethod.getConcreteType().toLowerCase().contains("concurrentskiplistset")
				|| collectionMethod.getConcreteType().toLowerCase().equals("ljava/util/set") 
				|| collectionMethod.getConcreteType().toLowerCase().contains("concurrenthashset")
				|| collectionMethod.getConcreteType().toLowerCase().contains("copyonwritehashset");
	}


	private void setConsumptions(CollectionMethod collectionMethod, HashMap<String, Double> consumptions, ArrayList<String> types, String operation) {
		
		for (String type : types) {
			
			Double consumption = EnergyConsumption.energyConsumption(collectionMethod,profileManager.getEnergy(type, operation));
			if(consumption!=null){
				consumptions.put(type, consumption);
			}
		}
	}
	
	public HashMap<String, HashMap<String,Double>> getRecommendationOrder(){
		
		HashMap<String, HashMap<String,Double>> recomendation = new HashMap<String, HashMap<String,Double>>();
		
		
		if(!methodsEnergyMap.isEmpty()){
			
//			ArrayList<String> distinctVariableNames = getDistinctVariableNames(methodsEnergyMap);
//			
//			for (String name : distinctVariableNames) {
//				ArrayList<MethodEnergy> variableMethods = getVariableMethods(name,methodsEnergyMap);
//				
//				for (MethodEnergy methodEnergy : variableMethods) {
//					if(!recomendation.containsKey(name)){
//						recomendation.put(name, methodEnergy.getConsumptions());
//					} else {
//						HashMap<String, Double> consumptions = recomendation.get(name);
//						for (String operation : consumptions.keySet()) {
//							Double consumption = consumptions.get(operation);
//							consumptions.put(operation, consumption+methodEnergy.getConsumptions().get(operation));
//						}
//					}
//				}
//			}
			
			
			setRecomendationForMethods(recomendation,methodsEnergyMap);			
		} 
		
		if(!methodsEnergyList.isEmpty()){
			setRecomendationForMethods(recomendation,methodsEnergyList);
		}
		
		if(!methodsEnergySet.isEmpty()){
			setRecomendationForMethods(recomendation,methodsEnergySet);
		}
		
		return recomendation;
		
	}
	
	
	private void setRecomendationForMethods(HashMap<String, HashMap<String,Double>> recomendation , ArrayList<MethodEnergy> methodsEnergy){
		ArrayList<String> distinctVariableNames = getDistinctVariableNames(methodsEnergy);
		
		for (String name : distinctVariableNames) {
			ArrayList<MethodEnergy> variableMethods = getVariableMethods(name,methodsEnergy);
			
			for (MethodEnergy methodEnergy : variableMethods) {
				if(!recomendation.containsKey(name)){
					recomendation.put(name, methodEnergy.getConsumptions());
				} else {
					HashMap<String, Double> consumptions = recomendation.get(name);
					for (String operation : consumptions.keySet()) {
						Double consumption = consumptions.get(operation);
						consumptions.put(operation, consumption+methodEnergy.getConsumptions().get(operation));
					}
				}
			}
		}
	}
	
	private ArrayList<MethodEnergy> getVariableMethods(String variableName, ArrayList<MethodEnergy> methods){
		
		ArrayList<MethodEnergy> energies = new ArrayList<MethodEnergy>();
		
		for (MethodEnergy methodEnergy : methods) {
			if(methodEnergy.getMethod().getFieldName().equals(variableName)){
				energies.add(methodEnergy);
			}
		}		
		
		return energies;		
	}
	
	private ArrayList<String> getDistinctVariableNames(ArrayList<MethodEnergy> methods){
		ArrayList<String> fieldNames = new ArrayList<String>();
		
		for (MethodEnergy method : methods) {
			if(!fieldNames.contains(method.getMethod().getFieldName())){
				fieldNames.add(method.getMethod().getFieldName());
			}
		}
		
		return fieldNames;
		
	}
	
	
	
	
	
	
	
	

}
