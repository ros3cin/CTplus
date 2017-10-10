package br.ufpe.cin.datarecommendation;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import br.ufpe.cin.dataanalysis.CollectionMethod;

public class ReadFile {

	
	/*
	 * CSV Format - Data Structure Type , Operation, Energy Consumption	 * 
	 * */
	public static ArrayList<EnergyProfile> getEnergyProfilesFromFile(String filePath) throws IOException{
		
		ArrayList<EnergyProfile> datalist = new ArrayList<EnergyProfile>();
		
		FileReader fr = new FileReader(filePath);
	    BufferedReader br = new BufferedReader(fr);
	    String stringRead = br.readLine();
		
	    while( stringRead != null )
		{
		    String[] elements = stringRead.split(",");

		    if(elements.length == 3){
			    String type = elements[0];
			    String operation = elements[1];
			    String energy = elements[2];
	
			    EnergyProfile temp = new EnergyProfile(type, operation, energy);
			    datalist.add(temp);
	
			    // read the next line
			    stringRead = br.readLine();
		    }
		}
	    
	    br.close();
	    return datalist;
	}
	
	/*
	 * CSV Format - Data Structure Type , Operation, Energy Consumption	 * 
	 * */
	public static ArrayList<CollectionMethod> getCollectionMethodFromFile(String filePath) throws IOException{
		
		ArrayList<CollectionMethod> datalist = new ArrayList<CollectionMethod>();
		
		FileReader fr = new FileReader(filePath);
	    BufferedReader br = new BufferedReader(fr);
	    br.readLine();//advancing header
	    String stringRead = br.readLine();
		
	    while( stringRead != null )
		{
		    String[] elements = stringRead.split(",");

		    if(elements.length > 10){
			    String type = elements[1];
			    String name = elements[3]+"-"+elements[6];
			    String operation = elements[4];
			    Integer ocurrencies = Integer.parseInt(elements[7]);
			    String loopInfo = elements[9];
			    
			    if(operation.equals("addElement")){
			    	operation = "add";
			    }else if(operation.equals("elementAt")){
			    	operation = "get";
			    }
	
			    CollectionMethodDTO temp = new CollectionMethodDTO(type,name, operation,ocurrencies, loopInfo);
			    datalist.add(temp);
	
			    // read the next line
			    stringRead = br.readLine();
		    }
		}
	    
	    br.close();
	    return datalist;
	}
	
	
}
