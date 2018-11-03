package br.ufpe.cin.dataanalysis;

/**
 * The headers of the analysis file
 * @author RENATO
 *
 */
public enum AnalysisFileHeader {
	SUPER_TYPE("Super type"),
	TYPE("Type"),
	CONTAINING_METHOD("Containing method"),
	FIELD_NAME("Field name"),
	INVOKED_METHOD("Invoked method"),
	SOURCE_CODE_LINE("Soure code line"),
	CONTAINING_CLASS("Containing class"),
	OCCURENCIES("Occurencies"),
	IS_INTO_LOOP("Is into loop?"),
	LOOP_NESTING_INFO("Loop nesting info"),
	IS_IN_RECURSIVE_METHOD("Is inside recursive method?"),
	IS_LOCAL_FIELD("Is local field?");
	
	private String description;
	
	private AnalysisFileHeader(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
