package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;

public class EnergyProfileManager {

	private ArrayList<EnergyProfile> profiles;
	private ArrayList<String> mapTypes = new ArrayList<String>();
	private ArrayList<String> listTypes = new ArrayList<String>();
	private ArrayList<String> setTypes = new ArrayList<String>();
	
	public EnergyProfileManager(ArrayList<EnergyProfile> profiles) {
		super();
		this.profiles = profiles;
		
		fillTypes();		
	}

	private boolean shouldAddDataStructure(String type) {
		ICollectionsTypeResolver nameResolver = new CollectionsTypeResolver();
		return (RecommenderConfiguration.INCLUDE_JCF && nameResolver.isFromStandardJCF(type)) || 
				(RecommenderConfiguration.INCLUDE_ECLIPSE_COLLECTIONS && nameResolver.isFromEclipseCollections(type)) ||
				(RecommenderConfiguration.INCLUDE_APACHE_COMMONS_COLLECTIONS && nameResolver.isFromApacheCommonsCollections(type)) ||
				(RecommenderConfiguration.INCLUDE_JSR166e && nameResolver.isFromJSR166e(type));
	}
	
	private void fillTypes() {
		ICollectionsTypeResolver nameResolver = new CollectionsTypeResolver();
		for (EnergyProfile profile : profiles) {
			
			if(nameResolver.isSet(profile.getDataStructureType())){
				if(!setTypes.contains(profile.getDataStructureType())) {
					if(shouldAddDataStructure(profile.getDataStructureType())) {
						setTypes.add(profile.getDataStructureType());
					}
				}
			}else if(nameResolver.isMap(profile.getDataStructureType())){
				if(!mapTypes.contains(profile.getDataStructureType())) {
					if(shouldAddDataStructure(profile.getDataStructureType())) {
						mapTypes.add(profile.getDataStructureType());
					}
				}
			} else if(nameResolver.isList(profile.getDataStructureType())){
				if(!listTypes.contains(profile.getDataStructureType())) {
					if(shouldAddDataStructure(profile.getDataStructureType())) {
						listTypes.add(profile.getDataStructureType());
					}
				}
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
