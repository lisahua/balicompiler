package assignment1;

/**
 * This class is the SaM code template that
 *         generates SaM code based on the input parameters and LocalVariable
 *         Manager.
 * @author Sungmin Cho, Jinruhua 
 * 
 * 
 */
public class CodeTemplate {
	static int labelCounter = 0;

	public static String osSetup() {
		String result = "  PUSHIMM 0\n" + "  LINK\n" + "  JSR main\n"
				+ "  POPFBR\n" + "  STOP\n";
		return result;
	}

	/**
	 * return the SaM code for method call
	 * @param methodName
	 * @param numberOfParameters
	 * @return
	 */
	public static String getMethodCall(String methodName, int numberOfParameters) {
		String format = "  LINK\n" + "  JSR %s\n" + "  POPFBR\n"
				+ "  ADDSP -%d\n";

		String result = String.format(format, methodName, numberOfParameters);

		return result;
	}

	/**
	 * return the SaM code for method declaration
	 * @param methodName method Name
	 * @param body  the body of the method
	 * @param lvManager local variable manager, which is a helper class for method
	 * @return 
	 */
	public static String getMethodDecl(String methodName, String body,
			LocalVariableManager lvManager) {

		int numberOfLocalVariables = lvManager
				.getNumberOfLocalVariables(methodName);
		int rvLocation = lvManager.getReturnValueLocation(methodName);
		String methodNameEnd = methodName + "End";
		String result = String.format("%s:\n", methodName)
				+ String.format("  ADDSP %d\n", numberOfLocalVariables)
				+ String.format("%s\n", body)
				+ String.format("%s:\n", methodName + "End")
				+ String.format("  STOREOFF %d // %d is offset of rv slot\n",
						rvLocation, rvLocation)
				+ String.format("  ADDSP %d\n", -numberOfLocalVariables)
				+ "  JUMPIND\n";
		return result;
	}

	/**
	 * return the SaM code for while loop
	 * @param exp get the expression of the while loop
	 * @param body get the body of the while loop
	 * @param breakPoint get the break point, which is not required now
	 * @return the corresponding Sam Code
	 */
	public static String getWhile(String exp, String body, String breakPoint) {
		String newLabel1 = String.format("L%d", labelCounter++);
		String newLabel2 = String.format("L%d", labelCounter++);
		// TODO Auto-generated method stub
		String result = String.format("  JUMP %s\n", newLabel1)
				+ String.format("%s:\n", newLabel2) + String.format("%s", body)
				+ String.format("%s:\n", newLabel1) + String.format("%s", exp)
				+ String.format("  JUMPC %s\n", newLabel2)
				+ String.format("%s:\n", breakPoint);
		return result;
	}

	/**
	 * return the SaM code for if-else condition
	 * @param exp get the expression of the if-else condition
	 * @param ifStatement get the if statement
	 * @param elseStatement get the else statement
	 * @return  return the corresponding Sam Code
	 */
	public static String getIf(String exp, String ifStatement,
			String elseStatement) {
		String newLabel1 = String.format("L%d", labelCounter++);
		String newLabel2 = String.format("L%d", labelCounter++);
		// TODO Auto-generated method stub
		String result = String.format("%s", exp)
				+ String.format("  JUMPC %s\n", newLabel1)
				+ String.format("%s", elseStatement)
				+ String.format(
						"  JUMP %s // else is implemented, go to end of the if\n",
						newLabel2) + String.format("%s:", newLabel1)
				+ String.format("%s", ifStatement)
				+ String.format("%s:\n", newLabel2);
		return result;
	}
}
