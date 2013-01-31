package assignment1;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Utility class for method information
 * 
 * @author Sungmin Cho, Jinruhua
 * 
 */
class MethodInfo {
	String methodName;
	List<String> parameters = new ArrayList<String>();
	List<String> locals = new ArrayList<String>();

	/**
	 * constructor of MethodInfo
	 * 
	 * @param methodName method Name
	 */
	public MethodInfo(String methodName) {
		this.methodName = methodName;
	}

	/**
	 * add method parameter
	 * 
	 * @param name local variable name
	 * @param type identify whether it is paramenter or local variable
	 */
	public void put(String name, int type) {
		if (type == LocalVariableManager.PARAMETER) {
			parameters.add(name);
		} else {
			locals.add(name);
		}
	}

	/**
	 * returns the index of the parameter
	 * 
	 * @param name the name of the parameter
	 * @return the index of the parameter
	 */
	public int get(String name) {
		// from the name find the index
		// parameter index rule
		// n is the number of
		// first parameter : fbr - (n
		// 1. Find parameter first
		int count = 0;
		int length = parameters.size();
		for (String s : parameters) {
			if (s.equals(name)) {
				return -length + count;
			}
			count++;
		}
		// if name is not in the parameter, search local next
		count = 1; // first local is located in bfr + 1
		for (String s : locals) {
			if (s.equals(name)) {
				return count + 1;
			}
			count++;
		}
		// When it's not returned, it means that the variable is not in locals
		// or parameters
		String errorMessage = String.format(
				"The variable %s is not in parameters nor in locals", name);
		throw new RuntimeException(errorMessage);
	}

	/**
	 * get the location of the return value
	 * 
	 * @return the location of the return value
	 */
	public int getReturnValueLocation() {
		int length = parameters.size();
		return -(length + 1);
	}

	/**
	 * get the number of parameter
	 * 
	 * @return the number of paramenter
	 */
	public int getNumberOfParameters() {
		// TODO Auto-generated method stub
		return this.parameters.size();
	}

	/**
	 * get the numnber of local variables
	 * 
	 * @return the numnber of local variables
	 */
	public int getNumberOfLocals() {
		// TODO Auto-generated method stub
		return this.locals.size();
	}
}

/**
 * Helper class for functions
 * 
 * @author Sungmin Cho, Jinruhua
 * 
 */
public class LocalVariableManager {

	static int PARAMETER = 1;
	static int LOCAL = 2;

	Map<String, MethodInfo> map = new HashMap<String, MethodInfo>();
	String currentMethod;
	String breakPoint;

	/**
	 * Set the current method we are parsing. Without the method name as the
	 * first parameter, we use the current method.
	 * 
	 * @param method
	 *            name
	 */
	public void setCurrentMethod(String method) {
		MethodInfo methodInfo = new MethodInfo(method);
		map.put(method, methodInfo);
		this.currentMethod = method;
	}

	/**
	 * set the index value
	 * 
	 * @param variable
	 *            local variable name
	 * @param index
	 *            index value
	 */
	public void setIndex(String variable, int index) {
		this.setIndex(this.currentMethod, variable, index);
	}

	/**
	 * get the index value
	 * 
	 * @param variable
	 * @return index value
	 */
	public int getIndex(String variable) {
		// return this.getIndex(this.currentMethod, variable);
		MethodInfo methodInfo = map.get(this.currentMethod);
		return methodInfo.get(variable);
	}

	/**
	 * set index value
	 * 
	 * @param method
	 *            method name
	 * @param variable
	 *            variable name
	 * @param type
	 *            identify it is parameter or local variable
	 */
	public void setIndex(String method, String variable, int type) {
		MethodInfo methodInfo = map.get(method);
		// if (map.containsKey(method))
		// {
		// methodInfo = map.get(method);
		// }
		// else
		// {
		// methodInfo = new MethodInfo(method);
		// map.put(method, methodInfo);
		// }
		methodInfo.put(variable, type);
	}

	/**
	 * based on variable, return the index of that variable in the method
	 * 
	 * @param method
	 * @param variable
	 * @return the index of the variable
	 */
	public int getIndex(String method, String variable) {
		MethodInfo methodInfo = map.get(method);
		return methodInfo.get(variable);
	}

	/**
	 * get the number of parameters
	 * 
	 * @param method
	 * @return the number of parameter
	 */
	public int getNumberOfParameter(String method) {
		MethodInfo methodInfo = map.get(method);
		return methodInfo.getNumberOfParameters();
	}

	/**
	 * get the number of local variables
	 * 
	 * @param method
	 * @return the number of local variables
	 */
	public int getNumberOfLocalVariables(String method) {
		// TODO Auto-generated method stub
		MethodInfo methodInfo = map.get(method);
		return methodInfo.getNumberOfLocals();
	}

	/**
	 * get the location of return value
	 * 
	 * @param method
	 * @return the location of return value
	 */
	public int getReturnValueLocation(String method) {
		MethodInfo methodInfo = map.get(method);
		return methodInfo.getReturnValueLocation();
	}

	/**
	 * get the name of the current parsing method
	 * 
	 * @return the name of the current parsing method
	 */
	public String getCurrentMethod() {
		// TODO Auto-generated method stub
		return this.currentMethod;
	}

	/**
	 * get the break location, which is mainly for the break code outside the
	 * loop
	 * 
	 * @return break location
	 */
	public String getBreakLocation() {
		// TODO Auto-generated method stub
		return this.breakPoint; // "TODO: NO BREAK POSITION CALCULATION NOW";
	}
/**
 * set break point, which is not required now.
 * @param string
 */
	public void setBreakPoint(String string) {
		// TODO Auto-generated method stub
		this.breakPoint = string;
	}
/**
 * get break point, which is not required now
 * @return the break point
 */
	public String getBreakPoint() {
		return this.breakPoint;
	}
/**
 * there is no break point
 */
	public void nullBreakPoint() {
		this.breakPoint = null;
	}

}
