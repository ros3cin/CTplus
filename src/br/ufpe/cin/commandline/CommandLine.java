package br.ufpe.cin.commandline;

import org.apache.commons.lang3.StringUtils;

import picocli.CommandLine.Option;

/**
 * 
 * @author RENATO
 *
 */
public final class CommandLine {
	@Option(names = {"-t", "--target"}, description = "The target JAR or APK, this is required if the analyze flag is set")
	public String target;
	
	@Option(names = {"--analysis-output-file"}, defaultValue = "analysis.csv", description = "The name of the analysis output file. Defaults to analysis.csv")
	public String analysisOutputFile;
	
	@Option(names = {"-e", "--exclusions-file"}, description = "The path to the scope exclusion file")
	public String exclusions;
	
	@Option(names = {"-a", "--analyze"}, description = "Run the analysis")
	public boolean analyze;
	
	@Option(names = {"--packages"}, arity = "1..*", description = "Space separated packages to include in the scope of the analysis")
	public String[] packages;
	
	@Option(names = {"-p", "--points-to-analysis"}, description = "If set, runs the points-to-analysis on the target")
	public boolean poinsToAnalysis;
	
	@Option(names = {"--points-to-analysis-file"}, defaultValue = "points-to-analysis.txt", description = "The points-to-analysis output file")
	public String pointsToAnalysisFile;
	
	@Option(names = {"--energy-profile-file"}, description = "The energy profile file to be used on the recommender")
	public String energyProfileFile;
	
	@Option(names = {"-r", "--recommend"}, description = "Run the recommendation")
	public boolean recommend;
	
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays this help")
	public boolean usageHelpRequested = false;
	
	private void validateNotEmpty(String str, String msg) throws CommandLineValidationException {
		if (StringUtils.isEmpty(str)) {
			throw new CommandLineValidationException(msg);
		}
	}
	
	private void validateNotFalse(boolean condition, String msg) throws CommandLineValidationException {
		if (!condition) {
			throw new CommandLineValidationException(msg);
		}
	}
	
	public void validate() throws CommandLineValidationException {
		boolean isAnyTaskChoosen = this.analyze || this.recommend || this.poinsToAnalysis;
		validateNotFalse(
				isAnyTaskChoosen,
				"You must set at least one of the available tasks: --analyse, --recommend or --points-to-analysis"
		);
		if (this.analyze) {
			validateNotEmpty(this.target, "The --target option is mandatory if the --analyze flag is set");
		}
		if (this.poinsToAnalysis) {
			validateNotEmpty(this.target, "The --target option is mandatory if the --points-to-analysis flag is set");
		}
		if (this.recommend) {
			validateNotEmpty(this.energyProfileFile, "The --energy-profile-file option is mandatory if the --recommend flag is set");
			validateNotEmpty(this.analysisOutputFile, "The --analysis-output-file option is mandatory if the --recommend flag is set");
		}
	}
}
