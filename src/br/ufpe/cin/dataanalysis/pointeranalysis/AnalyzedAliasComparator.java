package br.ufpe.cin.dataanalysis.pointeranalysis;

import java.util.Comparator;

public class AnalyzedAliasComparator implements Comparator<AnalyzedAlias> {

	@Override
	public int compare(AnalyzedAlias arg0, AnalyzedAlias arg1) {
		int result = 0;
		result += arg0.getClassName().compareTo(arg1.getClassName());
		if( (arg0.getMethodName()!=null) && (arg1.getMethodName()!=null) ) {
			result += arg0.getMethodName().compareTo(arg1.getMethodName());
		}
		result += arg0.getVariableName().compareTo(arg1.getVariableName());
		return result;
	}

}
