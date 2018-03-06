package br.ufpe.cin.datarecommendation;

public interface ICollectionTypeCompatibility {
	public boolean isThreadSafenessEqual(String source, String dest);
	public boolean isBehaviorEqual(String source, String dest);
}
