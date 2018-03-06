package br.ufpe.cin.datarecommendation;

public class CollectionTypeCompatibility implements ICollectionTypeCompatibility {
	
	
	
	private boolean isListThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isListInterface = typeResolver.isListInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeList(source)==typeResolver.isThreadSafeList(dest);
		return isListInterface||isSameThreadSafeness;
	}
	private boolean isMapThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isMapInterface = typeResolver.isMapInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeMap(source)==typeResolver.isThreadSafeMap(dest);
		return isMapInterface||isSameThreadSafeness;
	}
	private boolean isSetThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		boolean isSetInterface = typeResolver.isSetInterface(source);
		boolean isSameThreadSafeness = typeResolver.isThreadSafeSet(source)==typeResolver.isThreadSafeSet(dest);
		return isSetInterface||isSameThreadSafeness;
	}

	@Override
	public boolean isThreadSafenessEqual(String source, String dest) {
		ICollectionsTypeResolver typeResolver = new CollectionsTypeResolver();
		if(typeResolver.isList(source))
			return isListThreadSafenessEqual(source, dest);
		else if (typeResolver.isMap(source))
			return isMapThreadSafenessEqual(source, dest);
		else if (typeResolver.isSet(source))
			return isSetThreadSafenessEqual(source, dest);
		else
			return false;
	}

	@Override
	public boolean isBehaviorEqual(String source, String dest) {
		return false;
	}

}
