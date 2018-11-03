package br.ufpe.cin.datarecommendation;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import br.ufpe.cin.dataanalysis.AnalysisFileHeader;
import br.ufpe.cin.dataanalysis.CollectionMethod;

public class ReadFile {
	public static ArrayList<EnergyProfile> getEnergyProfilesFromFile(String filePath) throws IOException{
		final CSVParser reader = new CSVParser(new FileReader(filePath), CSVFormat.DEFAULT.withHeader());
		ArrayList<EnergyProfile> datalist = new ArrayList<EnergyProfile>();
		
		for (final CSVRecord line : reader) {
			String type = line.get(0);
			String operation = line.get(1);
			String energy = line.get(2);
			
			EnergyProfile energyProfile = new EnergyProfile(type, operation, energy);
		    datalist.add(energyProfile);
		}
		
		reader.close();
		
	    return datalist;
	}
	
	public static ArrayList<CollectionMethod> getCollectionMethodFromFile(String filePath) throws IOException{
		final CSVParser reader = new CSVParser(new FileReader(filePath), CSVFormat.DEFAULT.withHeader());
		ArrayList<CollectionMethod> datalist = new ArrayList<CollectionMethod>();
		
		for (final CSVRecord line : reader) {
			String type = line.get(AnalysisFileHeader.TYPE.getDescription());
			String classContainingField = line.get(AnalysisFileHeader.CONTAINING_CLASS.getDescription());
			String fieldName = line.get(AnalysisFileHeader.FIELD_NAME.getDescription());
			String invokedMethod = line.get(AnalysisFileHeader.INVOKED_METHOD.getDescription());
			Integer occurencies = Integer.parseInt(line.get(AnalysisFileHeader.OCCURENCIES.getDescription()));
			String loopNestingInfo = line.get(AnalysisFileHeader.LOOP_NESTING_INFO.getDescription());
			String containingMethod = line.get(AnalysisFileHeader.CONTAINING_METHOD.getDescription());
			boolean isFieldLocal = Boolean.parseBoolean(line.get(AnalysisFileHeader.IS_LOCAL_FIELD.getDescription()));
			
			CollectionMethodDTO info = new CollectionMethodDTO(
					type,
					fieldName,
					invokedMethod,
					occurencies,
					loopNestingInfo,
					classContainingField,
					isFieldLocal
			);
			info.setCallMethodName(containingMethod);
			
		    datalist.add(info);
		}
		
		reader.close();
		
	    return datalist;
	}
	
	
}
