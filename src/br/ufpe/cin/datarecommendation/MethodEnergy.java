package br.ufpe.cin.datarecommendation;

import java.util.HashMap;

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class MethodEnergy {
	
	private CollectionMethod method;
	private HashMap<String,Double> consumptions;
	
	public MethodEnergy(CollectionMethod method, HashMap<String, Double> consumptions) {
		super();
		this.method = method;
		this.consumptions = consumptions;
	}

	public CollectionMethod getMethod() {
		return method;
	}

	public void setMethod(CollectionMethod method) {
		this.method = method;
	}

	public HashMap<String, Double> getConsumptions() {
		return consumptions;
	}

	public void setConsumptions(HashMap<String, Double> consumptions) {
		this.consumptions = consumptions;
	}

}
