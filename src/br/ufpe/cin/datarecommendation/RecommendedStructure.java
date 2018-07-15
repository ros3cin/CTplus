package br.ufpe.cin.datarecommendation;

public class RecommendedStructure implements Comparable<RecommendedStructure> {
	private String structure;
	private Double consumption;
	
	public RecommendedStructure(String structure, Double consumption) {
		this.structure=structure;
		this.consumption=consumption;
	}
	
	public Double getConsumption() {
		return this.consumption;
	}
	
	public String getStructure() {
		return this.structure;
	}
	
	@Override
	public int compareTo(RecommendedStructure o) {
		return this.consumption.compareTo(o.getConsumption());
	} 
}
