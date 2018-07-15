package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.Comparator;

public class AnalyzedAliasComparator implements Comparator<AnalyzedAlias> {

	@Override
	public int compare(AnalyzedAlias arg0, AnalyzedAlias arg1) {
		int result = 0;
		result = arg0.getClassName().compareTo(arg1.getClassName());
		if(result == 0) {
			if( (arg0.getMethodName()!=null) && (arg1.getMethodName()!=null) ) {
				result = arg0.getMethodName().compareTo(arg1.getMethodName());
			} else if ( (arg0.getMethodName() == null) && (arg1.getMethodName() == null) ) {
				result = 0;
			} else if (arg1.getMethodName() == null) {
				result = -1;
			} else {
				result = 1;
			}
		}
		if(result == 0) {
			result = arg0.getVariableName().compareTo(arg1.getVariableName());
		}
		return result;
	}

}
