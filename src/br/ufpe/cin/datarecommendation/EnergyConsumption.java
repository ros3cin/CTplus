package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class EnergyConsumption {

	
	public static HashMap<String,Double> consumptionCalculator(CollectionMethod method, ArrayList<EnergyProfile> profiles){
		
		
		
		return null;
	} 
	
	/*
	 * Ocorrencies x consumption factor x loop complextity factor
	 * */
	public static Double energyConsumption(CollectionMethod method, Double energyFactor){
		Double consumption = null;
		if(energyFactor!=null){
			//consumption = method.getOcorrencias()*energyFactor*getLoopFactor(method.getOuterLoops(),method.getOcorrencias());
			consumption = method.getOcorrencias()*energyFactor*getLoopFactorByDepth(method.getOuterLoops().size(), method.getOcorrencias());
		}
		return consumption;
		
	}
	
	private static double getLoopFactorByDepth(int depth, int occurrencies) {		
				
		if(depth==0){
			return 1;
		}
		return Math.pow(occurrencies+1, depth+1);
	}
	
}
