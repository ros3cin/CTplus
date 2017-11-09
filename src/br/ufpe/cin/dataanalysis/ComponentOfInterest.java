package br.ufpe.cin.dataanalysis;

import com.ibm.wala.classLoader.IMethod;

/***
 * Used to filter the methods being analyzed
 * @author RENATO
 *
 */
public class ComponentOfInterest {
	private String packageName;
	private String className;
	private String methodName;
	public ComponentOfInterest(String packageName, String className, String methodName) {
		this.packageName=packageName;
		this.className=className;
		this.methodName=methodName;
	}
	public boolean checkIfMethodMeetsComponent(IMethod method) {
		boolean result = false;
		
		if(method!=null) {
			boolean currentCheck=(this.packageName!=null) || (this.className!=null) || (this.methodName!=null);
			if(this.packageName!=null)
				currentCheck = currentCheck && method.toString().toLowerCase().contains(this.packageName.toLowerCase());
			if(this.className!=null)
				currentCheck = currentCheck && method.toString().toLowerCase().contains(this.className.toLowerCase());
			if(this.methodName!=null)
				currentCheck = currentCheck && method.toString().toLowerCase().contains(this.methodName.toLowerCase());
			result = currentCheck;
		}
		
		return result;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	public String getClassName() {
		return className;
	}
	public void setClassName(String className) {
		this.className = className;
	}
	public String getMethodName() {
		return methodName;
	}
	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}
	
}
