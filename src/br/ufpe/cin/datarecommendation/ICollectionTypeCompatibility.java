package br.ufpe.cin.datarecommendation;

public interface ICollectionTypeCompatibility {
	public boolean isThreadSafenessEqual(String source, String dest);
	public boolean canReplace(String source, String dest);
	public boolean hasConstructor(String sourceConstructor, String targetCollection);
}
