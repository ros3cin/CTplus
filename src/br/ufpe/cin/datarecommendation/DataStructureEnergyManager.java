package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.TreeMap;

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
		
		ICollectionsTypeResolver nameResolver = new CollectionsTypeResolver();
		for (CollectionMethod collectionMethod : methods) {
			HashMap<String, Double> consumptions = new HashMap<String, Double>();
			
			if(nameResolver.isMap(collectionMethod.getConcreteType())){
				
				if(collectionMethod.getNome().equals("put(key;value)") 
						|| collectionMethod.getNome().equals("remove(key)") 
						|| collectionMethod.getNome().equals("iterator")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getMapTypes(),collectionMethod.getNome(),collectionMethod.getConcreteType());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergyMap.add(method);					
				} 
				
			} else if(nameResolver.isList(collectionMethod.getConcreteType())){
				
				if(collectionMethod.getNome().equals("randomGet")
						|| collectionMethod.getNome().equals("sequentialGet")
						|| collectionMethod.getNome().equals("iterator")
						
						|| collectionMethod.getNome().equals("add(value)")
						|| collectionMethod.getNome().equals("add(starting-index;value)")
						|| collectionMethod.getNome().equals("add(middle-index;value)")
						|| collectionMethod.getNome().equals("add(ending-index;value)")
						
						|| collectionMethod.getNome().equals("remove(starting-index)")
						|| collectionMethod.getNome().equals("remove(middle-index)")
						|| collectionMethod.getNome().equals("remove(ending-index)")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getListTypes(),collectionMethod.getNome(),collectionMethod.getConcreteType());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergyList.add(method);					
				} 
				
			} else if(nameResolver.isSet(collectionMethod.getConcreteType())){
				if(collectionMethod.getNome().equals("add(value)") 
						|| collectionMethod.getNome().equals("remove(key)") 
						|| collectionMethod.getNome().equals("iterator")){
				
					setConsumptions(collectionMethod, consumptions,profileManager.getSetTypes(),collectionMethod.getNome(),collectionMethod.getConcreteType());
					MethodEnergy method = new MethodEnergy(collectionMethod,consumptions);
					methodsEnergySet.add(method);					
				} 
			}			
		}
		
	}


	private void setConsumptions(CollectionMethod collectionMethod, HashMap<String, Double> consumptions, ArrayList<String> types, String operation, String sourceConcreteType) {
		ICollectionTypeCompatibility collectionCompatibility = new CollectionTypeCompatibility();
		for (String type : types) {
			if(collectionCompatibility.isThreadSafenessEqual(sourceConcreteType, type)) {
				Double consumption = EnergyConsumption.energyConsumption(collectionMethod,profileManager.getEnergy(type, operation));
				if(consumption!=null){
					consumptions.put(type, consumption);
				}
			}
		}
	}
	
	public TreeMap<CollectionMethod, HashMap<String,Double>> getRecommendationOrder(){
		
		TreeMap<CollectionMethod, HashMap<String,Double>> recomendation = 
				new TreeMap<CollectionMethod, HashMap<String,Double>>(new Comparator<CollectionMethod>() {

			@Override
			public int compare(CollectionMethod o1, CollectionMethod o2) {
				return (o1.getFieldName()).compareTo(o2.getFieldName());
			}
			
		});
		
		if(!methodsEnergyMap.isEmpty()){			
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
	
	
	private void setRecomendationForMethods(TreeMap<CollectionMethod, HashMap<String,Double>> recomendation , ArrayList<MethodEnergy> methodsEnergy){
		ArrayList<String> distinctVariableNames = getDistinctVariableNames(methodsEnergy);
		
		for (String name : distinctVariableNames) {
			ArrayList<MethodEnergy> variableMethods = getVariableMethods(name,methodsEnergy);
			
			for (MethodEnergy methodEnergy : variableMethods) {
				if(!recomendation.containsKey(methodEnergy.getMethod())){
					recomendation.put(methodEnergy.getMethod(), methodEnergy.getConsumptions());
				} else {
					HashMap<String, Double> consumptions = recomendation.get(methodEnergy.getMethod());
					for (String operation : consumptions.keySet()) {
						Double consumption = consumptions.get(operation);
						if(methodEnergy.getConsumptions().get(operation)!=null)
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
