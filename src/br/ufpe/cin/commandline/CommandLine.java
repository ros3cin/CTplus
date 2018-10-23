package br.ufpe.cin.commandline;

import picocli.CommandLine.Option;

/**
 * 
 * @author RENATO
 *
 */
public final class CommandLine {
	@Option(names = {"-t", "--target"}, description = "The target JAR or APK, this is required if the analyze flag is set")
	public String target;
	
	@Option(names = {"--analysis-output"}, description = "The name of the analysis output file. Defaults to analysis.csv")
	public String analysisOutputFile;
	
	@Option(names = {"-e", "--exclusions"}, description = "The path to the scope exclusion file")
	public String exclusions;
	
	@Option(names = {"-a", "--analyze"}, description = "Run analyzer, this flag is true by default")
	public boolean analyze = true;
	
	@Option(names = {"--packages"}, description = "Space separated packages to include in the scope of the analysis")
	public String[] packages;
	
	@Option(names = {"-p", "--points-to-analysis"}, description = "If set, runs points-to-analysis and generates a file")
	public String pointsToAnalysis;
	
	@Option(names = {"-r", "--recommend"}, description = "Run recommender, this flag is false by default")
	public boolean recommend = false;
	
	@Option(names = {"-h", "--help"}, usageHelp = true, description = "Displays this help")
	public boolean usageHelpRequested = false;
	
	public void validate() throws CommandLineValidationException {
		if (this.analyze) {
			if ((this.target == null) || this.target.isEmpty()) {
				throw new CommandLineValidationException("Analyze flag set and target not set");
			}
		}
	}
}
