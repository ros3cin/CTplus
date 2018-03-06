package br.ufpe.cin.datarecommendation;

public interface ICollectionsNameResolver {
	public boolean isSet(String name);
	public boolean isList(String name);
	public boolean isMap(String name);
}
