package br.ufpe.cin.commandline;

import br.ufpe.cin.dataanalysis.JavaCollectionsAnalyser;
import br.ufpe.cin.debug.Debug;
import picocli.CommandLine.MissingParameterException;

final public class Entry {
	public static void main(String[] args) {
		try {
			CommandLine cmd = new CommandLine();
			new picocli.CommandLine(cmd).parse(args);
			if (cmd.usageHelpRequested) {
				picocli.CommandLine.usage(new CommandLine(), System.out);
				return;
			}
			cmd.validate();
			
			Debug.logger.info(String.format("Program started at %s",Debug.getCurrentTime()));
			if (cmd.analyze) {
				JavaCollectionsAnalyser.run(
						cmd.target, 
						cmd.exclusions, 
						cmd.packages, 
						cmd.analysisOutputFile, 
						cmd.pointsToAnalysisFile,
						cmd.poinsToAnalysis
				);
			}
		} catch (MissingParameterException e) {
			Debug.logger.error(e);
			picocli.CommandLine.usage(new CommandLine(), System.out);
			return;
		} catch (CommandLineValidationException e) {
			Debug.logger.error(e);
			picocli.CommandLine.usage(new CommandLine(), System.out);
			return;
		} catch (Exception e) { 
			Debug.logger.error(e);
		} finally {
			Debug.logger.info(String.format("Program finished at %s\n",Debug.getCurrentTime()));
		}
	}
}
