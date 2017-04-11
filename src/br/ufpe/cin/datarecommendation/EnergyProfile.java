package br.ufpe.cin.datarecommendation;

public class EnergyProfile {

	String dataStructureType;
	String operation;
	String ernergyConsumption; 
	
	public EnergyProfile(String dataStructureType, String operation, String ernergyConsumption) {
		super();
		this.dataStructureType = dataStructureType;
		this.operation = operation;
		this.ernergyConsumption = ernergyConsumption;
	}
	
	@Override
	public String toString() {
		
		return dataStructureType+","+operation+","+ernergyConsumption;
	}
	
	public String getDataStructureType() {
		return dataStructureType;
	}
	public void setDataStructureType(String dataStructureType) {
		this.dataStructureType = dataStructureType;
	}
	public String getOperation() {
		return operation;
	}
	public void setOperation(String operation) {
		this.operation = operation;
	}
	public String getErnergyConsumption() {
		return ernergyConsumption;
	}
	public void setErnergyConsumption(String ernergyConsumption) {
		this.ernergyConsumption = ernergyConsumption;
	}
	
	
	
	
}
