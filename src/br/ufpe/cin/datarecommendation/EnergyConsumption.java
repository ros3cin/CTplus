package br.ufpe.cin.datarecommendation;

import java.util.ArrayList;
import java.util.HashMap;

import br.ufpe.cin.dataanalysis.CollectionMethod;
import br.ufpe.cin.dataanalysis.Complexity;
import br.ufpe.cin.dataanalysis.LoopBlockInfo;

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

	private static double getLoopFactor(ArrayList<LoopBlockInfo> loops) {
		
		double factor = 1;
		for (int i = 0; loops!=null && i < loops.size(); i++) {
			
			Complexity complexity = loops.get(i).getComplexity();			
			
			
			if(complexity!=null){
				if(complexity.equals(Complexity.ON)){
					factor+=1;
				} else if(complexity.equals(Complexity.OLOGN)){
					factor+=0.5;
				}
			}			
		}		
		return factor;
	} 
	
	/*
	 * Fato de loop - Exponencial
	 * */
	private static double getLoopFactor(ArrayList<LoopBlockInfo> loops, int ocorrencias) {
		
		double factor = 1;
		//if(ocorrencias==1) ocorrencias++; //para evitar o 1x1
		ocorrencias++;
		for (int i = 0; loops!=null && i < loops.size(); i++) {
			
			Complexity complexity = loops.get(i).getComplexity();			
			if(complexity!=null){
				if(complexity.equals(Complexity.ON)){
					factor*=ocorrencias;
				} else if(complexity.equals(Complexity.OLOGN)){
					factor*=ocorrencias/0.5;
				}
			}			
		}		
		return factor;
	} 
	
	
	private static double getLoopFactorByDepth(int depth, int occurrencies) {		
				
		if(depth==0){
			return 1;
		}
		return Math.pow(occurrencies+1, depth+1);
	}
	
}
