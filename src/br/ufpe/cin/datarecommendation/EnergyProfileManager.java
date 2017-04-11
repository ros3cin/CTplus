package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;

public class EnergyProfileManager {

	private ArrayList<EnergyProfile> profiles;
	private ArrayList<String> mapTypes = new ArrayList<String>();
	private ArrayList<String> listTypes = new ArrayList<String>();;
	private ArrayList<String> setTypes = new ArrayList<String>();;
	
	
	public EnergyProfileManager(ArrayList<EnergyProfile> profiles) {
		super();
		this.profiles = profiles;
		
		fillTypes();		
	}

	private void fillTypes() {
		
		for (EnergyProfile profile : profiles) {
			
			if(profile.getDataStructureType().toLowerCase().contains("set")
					&& !setTypes.contains(profile.getDataStructureType())){
				
				setTypes.add(profile.getDataStructureType());
				
			}else if((profile.getDataStructureType().toLowerCase().contains("hash") 
					|| profile.getDataStructureType().toLowerCase().contains("map"))
					&& !profile.getDataStructureType().toLowerCase().contains("set")
					&& !mapTypes.contains(profile.getDataStructureType())){
				
				mapTypes.add(profile.getDataStructureType());
				
			} else if((profile.getDataStructureType().toLowerCase().contains("vector") 
					|| profile.getDataStructureType().toLowerCase().contains("list"))
					&& !profile.getDataStructureType().toLowerCase().contains("map")
					&& !profile.getDataStructureType().toLowerCase().contains("set")
					&& !listTypes.contains(profile.getDataStructureType())){
				
				listTypes.add(profile.getDataStructureType());
				
			} 
		}
	}

	public Double getEnergy(String type, String operation){
		
		for (EnergyProfile energyProfile : profiles) {
			if(energyProfile.getDataStructureType().equals(type)
				&& energyProfile.getOperation().equals(operation)){
				return Double.parseDouble(energyProfile.getErnergyConsumption());
			}
		}
		
		return null;		
	}

	public ArrayList<EnergyProfile> getProfiles() {
		return profiles;
	}

	public void setProfiles(ArrayList<EnergyProfile> profiles) {
		this.profiles = profiles;
	}

	public ArrayList<String> getMapTypes() {
		return mapTypes;
	}

	public void setMapTypes(ArrayList<String> mapTypes) {
		this.mapTypes = mapTypes;
	}

	public ArrayList<String> getListTypes() {
		return listTypes;
	}

	public void setListTypes(ArrayList<String> listTypes) {
		this.listTypes = listTypes;
	}

	public ArrayList<String> getSetTypes() {
		return setTypes;
	}

	public void setSetTypes(ArrayList<String> setTypes) {
		this.setTypes = setTypes;
	}
	
}
