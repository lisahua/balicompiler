package assignment1;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;
import edu.cornell.cs.sam.io.TokenizerException;

/**
 * Main class for Recursive descent parser and SaM code generator.
 * 
 * @author Sungmin Cho, Jinruhua
 * 
 */
public class BaliCompiler {
	int breakCount = 0;
	LocalVariableManager lvManager = new LocalVariableManager();

	// this field is for returning the number of parameters in invoked
	// method. For example, calling f(1,2,3) stores this field 3.
	// Why do we need this?
	// It's because "CodeTemplate.getMethodCall" needs the parameter number
	// When a() calls b(), b() can be located after a(). In this case,
	// we can't know the number of parameters, so we need to store the
	// parameter info when we invoke the method.
	//
	// String result = getActuals(f); METHOD '(' ACTUALS? ')'
	// is called within the getMethodCall().
	private int numberOfParametersForInvokingMethod;

	// Set<String> methodSymbolTable = new HashSet<String>();
	// Set<String> variableSymbolTable = new HashSet<String>();

	public BaliCompiler() {

	}

	private void checkAndPushBack(SamTokenizer f) {
		if (f.canPushBack())
			f.pushBack();
		else {
			int lineNo = Thread.currentThread().getStackTrace()[2]
					.getLineNumber();
			System.err.printf("ERROR,  pushback not possible - check line %d",
					lineNo);
		}
	}

	void errorReporting(SamTokenizer f, String method, String expected) {
		int lineNo = f.lineNo();
		int lineNoInSource = Thread.currentThread().getStackTrace()[2]
				.getLineNumber();
		String result;
		if (expected.startsWith("MESSAGE")) {
			result = String.format(
					"ERROR in line %d : in %s - [%s] Check code %d\n", lineNo,
					method, expected, lineNoInSource);
		} else {
			result = String
					.format("ERROR in line %d : in %s - Expected (\"%s\"): Check code %d\n",
							lineNo, method, expected, lineNoInSource);
		}
		// System.err.println(result);
		throw new RuntimeException("Error in Parsing" + result);
	}

	/**
	 * get the input file of Bali program and return the generated SaM code
	 * 
	 * @param fileName
	 * @return return the corresponding Sam Code
	 */
	public String compiler(String fileName) {
		// returns SaM code for program in file
		try {
			SamTokenizer f = new SamTokenizer(fileName);
			// Toplevel is PROGRAM
			String pgm = getProgram(f);
			return pgm;
		} catch (Exception e) {
			System.err.println("Fatal error: could not compile program\n" + e);
			return "STOP\n";
		}
	}

	// PROGRAM -> METH_DECL*
	String getProgram(SamTokenizer f) {
		try {
			StringBuilder pgm = new StringBuilder();
			while (f.peekAtKind() != TokenType.EOF) {
				// METH_DECL*
				pgm.append(getMethodDecl(f));
			}
			return CodeTemplate.osSetup() + pgm.toString();
		} catch (Exception e) {
			System.err.println("Fatal error: could not compile program " + e);
			return "STOP\n";
		}
	}

	/**
	 * METH_DECL -> TYPE ID '(' FORMALS? ')' BODY
	 */
	String getMethodDecl(SamTokenizer f) {
		// TYPE
		if (!f.check("int")) { // must match at begining
			errorReporting(f, "getMethodDecl",
					"MESSAGE: TYPE is wrong (not int)");
		}
		// ID
		String id = f.getWord();
		lvManager.setCurrentMethod(id);
		// '('
		if (!f.check('(')) { // must match at begining
			errorReporting(f, "getMethodDecl", "(");
		}

		String formals = null;
		// formals?, it can be null, so check if next token is ")" or not
		if (f.peekAtKind() != TokenType.OPERATOR) {
			formals = getFormals(f);
		}

		if (!f.check(')')) { // must match at begining
			errorReporting(f, "getMethodDecl", ")");
		}

		// You would need to read in formals if any
		// And then have calls to getDeclarations and getStatements.
		String body = getBody(f);

		// Set<String> methodSymbolTable = new HashSet<String>();
		// methodSymbolTable.add(id);
		return CodeTemplate.getMethodDecl(id, body, lvManager);
	}

	// FORMALS -> TYPE ID (',' TYPE ID)*
	String getFormals(SamTokenizer f) {
		// TYPE - now we only support int
		f.check("int"); // must match at begining
		// ID
		String id = getId(f);
		int type = LocalVariableManager.PARAMETER;
		lvManager.setIndex(id, type);
		// (',' TYPE ID)*
		while (Utilities.peekIfTheNextTokenIsExpectedOperator(f, ',')) {
			// f.check(',');
			if (!f.check(',')) {
				errorReporting(f, "getFormals", ",");
			}
			if (!f.check("int")) {
				errorReporting(f, "getFormals", "int");
			}
			id = getId(f);
			lvManager.setIndex(id, type);
		}

		return null;
	}

	// BODY -> '{' VAR_DECL* STMT* '}'
	String getBody(SamTokenizer f) {
		// '{'
		// f.check('{');
		if (!f.check('{')) { // must match at begining
			errorReporting(f, "getBody", "{");
		}

		StringBuilder temp = new StringBuilder();
		// VAR_DECL* - get the first set for VAR_DECL (TYPE)
		// VAR_DECL -> TYPE ID ('=' EXP)? (',' ID ('=' EXP)?)* ';'
		while (Utilities.peekIfTheNextTokenIsType(f)) {
			temp.append(getVarDecl(f));
		}
		String varDecl = temp.toString();

		// STMT*// STMT -> ASSIGN ';' (LOCATION '=' EXP) (LOCATION -> ID)
		// | return EXP ';'
		// | if '(' EXP ')' STMT else STMT
		// | while '(' EXP ')' STMT
		// | break ';'
		// | BLOCK (BLOCK -> '{' STMT* '}')
		// | ';'

		temp = new StringBuilder();
		// t == TokenType.WORD
		// || t == TokenType.CHARACTER
		// || t == TokenType.OPERATOR)
		while (Utilities.peekIfTheNextTokenIsInFirstSetOfStatement(f)) {
			temp.append(getStatement(f));
		}
		String statements = temp.toString();
		// '}'
		if (!f.check('}')) { // must match at begining
			errorReporting(f, "getBody", "}");
		}
		// f.check('}');
		return String.format("%s\n%s\n", varDecl, statements);
	}

	// STMT -> ASSIGN ';'
	// | return EXP ';'
	// | if '(' EXP ')' STMT else STMT
	// | while '(' EXP ')' STMT
	// | break ';'
	// | BLOCK
	// | ';'

	String getStatement(SamTokenizer f) {
		TokenType t = f.peekAtKind();
		int lineNo = f.lineNo();
		// ';' or '{'
		if (t == TokenType.OPERATOR) {
			char op = f.getOp();
			// | ';'
			if (op == ';') {
				// no code is generated for ';'
				return "// ';' --> nop statement\n";
				// | BLOCK
			} else if (op == '{') {
				checkAndPushBack(f); // Block
				String block = getBlock(f);
				return block;
			} else {
				errorReporting(f, "getStatement", "MESSAGE: ({ nor ;} provided");
				throw new RuntimeException(
						"Error in getExp Parsing (statement) { nor ; provided");
			}
		}
		// STMT -> ASSIGN ';' <-- Assignment also starts with "WORD(ID)"
		// | return EXP ';'
		// | if '(' EXP ')' STMT else STMT
		// | while '(' EXP ')' STMT
		// | break ';'
		else if (t == TokenType.WORD) {
			String word = f.getWord();
			// | return EXP ';'
			if (word.equals("return")) {
				// f.match("return");
				String exp = getExp(f);
				// f.check(';');
				if (!f.check(';')) { // must match at begining
					errorReporting(f, "getStatement", ";"); // : ({ nor ;}
															// provided");
				}
				String exitPosition = lvManager.getCurrentMethod() + "End";
				return String.format("%s  JUMP %s\n", exp, exitPosition);
			}
			// | if '(' EXP ')' STMT else STMT
			else if (word.equals("if")) {
				// f.match("if");

				if (!f.check('(')) {
					errorReporting(f, "getStatement/if", "("); // : ({ nor ;}
																// provided");
				}
				String exp = getExp(f);
				if (!f.check(')')) {
					errorReporting(f, "getStatement/if", ")"); // : ({ nor ;}
																// provided");
				}
				String ifStatement = getStatement(f);

				if (!f.check("else")) {
					errorReporting(f, "getStatement", "else");
				}
				String elseStatement = getStatement(f);
				return CodeTemplate.getIf(exp, ifStatement, elseStatement);
			}
			// | while '(' EXP ')' STMT
			else if (word.equals("while")) {
				String breakPoint = String.format("BREAK%d", breakCount++);
				lvManager.setBreakPoint(breakPoint);
				// f.check("while");
				// f.match("while");
				// f.check('(');
				if (!f.check('(')) {
					errorReporting(f, "getStatement/while", "(");
				}
				String exp = getExp(f);
				// f.check(')');
				if (!f.check(')')) {
					errorReporting(f, "getStatement/while", ")");
				}
				String statement = getStatement(f);
				// String breakPoint = lvManager.getBreakLocation();
				lvManager.nullBreakPoint(); // To catch an error for calling
											// break point outside the loop
				return CodeTemplate.getWhile(exp, statement, breakPoint);
			}
			// | break ';'
			else if (word.equals("break")) {
				// f.check("break");
				// f.match("break");
				if (!f.check(';')) {
					errorReporting(f, "getStatement/break", ";");
				}
				String breakLocation = lvManager.getBreakLocation();
				if (breakLocation == null) {
					System.err
							.println("WARNING: break code outside loop: ingnoring the break");
					return "";
				}
				return String.format("  JUMP %s\n", breakLocation);
			}
			// STMT -> ASSIGN ';'
			else {
				// We already popped up the location name.
				// However, the assign is another production that needs that
				// popped
				// location name, wo we should push it back.
				checkAndPushBack(f);

				String assign = getAssign(f);

				if (!f.check(';')) {
					errorReporting(f, "getStatement/assign", ";");
				}
				return assign;
			}
		} else {
			throw new RuntimeException("Error in getExp Parsing (statement)");
		}
		// return null;
	}

	// BLOCK -> '{' STMT* '}'
	String getBlock(SamTokenizer f) {
		StringBuilder temp = new StringBuilder();
		if (!f.check('{')) {
			errorReporting(f, "getBlock", "{");
		}

		while (Utilities.peekIfTheNextTokenIsInFirstSetOfStatement(f)) {
			temp.append(getStatement(f));
		}
		if (!f.check('}')) {
			errorReporting(f, "getBlock", "}");
		}
		// f.check('}');
		return temp.toString();
	}

	// ASSIGN -> LOCATION '=' EXP
	String getAssign(SamTokenizer f) {
		String location = getLocation(f);
		int index = lvManager.getIndex(location);
		if (!f.check('=')) {
			errorReporting(f, "getAssign", "=");
		}
		// f.check('=');
		String exp = getExp(f);
		return String.format("%s  STOREOFF %d\n", exp, index);
	}

	// LOCATION -> ID
	String getLocation(SamTokenizer f) {
		String id = getId(f);
		return id;
	}

	// VAR_DECL -> TYPE ID ('=' EXP)? (',' ID ('=' EXP)?)* ';'
	// local variable setup, first variable is accessible with +1, and the
	// second with +2, and on
	String getVarDecl(SamTokenizer f) {
		StringBuilder temp = new StringBuilder();
		// f.check("int");
		if (!f.check("int")) {
			errorReporting(f, "getVarDecl", "int");
		}
		String id = getId(f);
		int type = LocalVariableManager.LOCAL;
		lvManager.setIndex(id, type);

		// This counter is used to locate the position of local variable
		// The first variable starts with +2
		// 0 : bp, +1 : return address
		int counter = 2;

		// first variable
		// variableSymbolTable.add(id);

		// ('=' EXP)?
		// There should be three cases when the next token is OPERATOR
		if (f.peekAtKind() == TokenType.OPERATOR) {
			char op = f.getOp();

			// = EXP case, so get expression
			if (op == '=') {
				// exp already prints out the code
				// ex) b = 6, 6 is interpreted as "PUSHIMM 6", and it's stored
				// in exp
				String exp = getExp(f);
				// we know the index as we just assigned it, but it is
				// automatically increased.
				// so (index - 1) is required.
				temp.append(String.format(
						"%s  STOREOFF %d // %s (variable decl)\n", exp,
						counter, id));
			}
			// no assign, and this is the last variable assignment
			else if (op == ';') {
				checkAndPushBack(f);
			}
			// no assign, but there are other assignments.
			else if (op == ',') {
				checkAndPushBack(f);
			}
			// f.check('=');
			else {
				// String exp = getExp(f);
				errorReporting(f, "getVarDecl", "=/;/,");
			}
		}

		// (',' ID ('=' EXP)?)*
		// TODO: Code duplication for checking additional declarations
		while (Utilities.peekIfTheNextTokenIsExpectedOperator(f, ',')) {
			// f.check(',');
			if (!f.check(',')) {
				errorReporting(f, "getVarDecl", ",");
			}
			id = getId(f);
			++counter; // increas +1 to indicate the next local variable
						// position
			type = LocalVariableManager.LOCAL;
			lvManager.setIndex(id, type);
			// variableSymbolTable.add(id);

			if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '=')) {
				// f.check('=');
				if (!f.check('=')) {
					errorReporting(f, "getVarDecl", "=");
				}
				String exp = getExp(f);
				temp.append(String.format(
						"%s  STOREOFF %d // %s (variable decl)\n", exp,
						counter, id));
			}
		}

		if (!f.check(';')) {
			errorReporting(f, "getVarDecl", ";");
		}

		return temp.toString();
	}

	// EXP -> LOCATION
	// | LITERAL
	// | METHOD '(' ACTUALS? ')'
	//
	String getExp(SamTokenizer f) {
		// First case LITERAL
		// Rule: PUSHIMM and return
		// LITERAL -> INT | true | false
		if (Utilities.peekIfTheNextTokenIsInFirstSetOfLiteral(f)) {
			if (Utilities.peekInteger(f)) {
				// if (f.peekAtKind() == TokenType.INTEGER) {
				int number = getInt(f);
				// code for integer
				return String.format("  PUSHIMM %d // expression(integer)\n",
						number); // + + "\n";
			}
			String trueOrFalse = f.getWord();
			if (trueOrFalse.equals("true")) {
				// true
				return String.format("  PUSHIMM 1  // true\n");
			} else {
				// false
				return String.format("  PUSHIMM 0  // false\n");
			}
		}
		// Second case METHOD INVOCATION OR LOCATION
		// | METHOD '(' ACTUALS? ')' -> ID
		// LOCATION -> ID
		if (Utilities.peekIfTheNextTokenIsId(f)) {
			// get ID
			String id = f.getWord();
			if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '(')) {
				// Method call
				StringBuilder temp = new StringBuilder();
				// without this code, when invoking the method with no
				// parameters, it will return the previous invocation.
				// This is one of the dangers of using global variables.
				this.numberOfParametersForInvokingMethod = 0;
				f.match('(');

				// We need to setup the lvManager for the parameters
				List<String> parameterNames = new ArrayList<String>();
				if (!Utilities.peekIfTheNextTokenIsExpectedOperator(f, ')')) {
					// Method call with parameter
					String result = getActuals(f);
					temp.append(result);
				}
				f.match(')');
				// 1. the number of the count makes the information about the
				// parameter
				// -> fsr - (count + 1) is the location of return value
				// -> fsr - 1 is the last parameter location
				// int count = lvManager.getNumberOfParameter(id);
				return String
						.format("  PUSHIMM 0 // reserve the return value before calling method %s\n",
								id)
						+ temp.toString()
						+ CodeTemplate.getMethodCall(id,
								this.numberOfParametersForInvokingMethod);
			} else {
				// 2.2 LOCATION
				// ex) b = *c* <-- c is the location
				checkAndPushBack(f);
				// LOCATION -> ID
				String location = getId(f);
				int index = lvManager.getIndex(location);
				// int indexInLocalsTable = getIndexInLocalsTable(location);
				return String.format("  PUSHOFF %d // Location %s \n", index,
						location);
			}
		}
		// Third case: EXPRESSION that starts with '(' ... ')'
		if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '(')) {
			// | '(' EXP ')'
			f.match('(');
			String result = null;
			if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '-')) {
				// | '(''-' EXP')'
				f.match('-');
				String exp = getExp(f);
				result = String.format("%sPUSH -1\nTIMES\n%s", exp);
			} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '!')) {
				// | '(''!' EXP')'
				f.match('!');
				String exp = getExp(f);
				result = String.format("NOT\n%s", exp);
			} else {
				String firstExp = getExp(f);
				String secondExp = null;
				if (Utilities.peekIfTheNextTokenIsExpectedOperator(f, '+')) {
					// | '('EXP '+' EXP')'
					f.match('+');
					secondExp = getExp(f);
					result = String
							.format("%s%s\n  ADD\n", firstExp, secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'-')) {
					// | '('EXP '-' EXP')'
					f.match('-');
					secondExp = getExp(f);
					result = String
							.format("%s%s\n  SUB\n", firstExp, secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'*')) {
					// | '('EXP '*' EXP')'
					f.match('*');
					secondExp = getExp(f);
					result = String.format("%s%s\n  TIMES\n", firstExp,
							secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'/')) {
					// | '('EXP '/' EXP')'
					f.match('/');
					secondExp = getExp(f);
					result = String
							.format("%s%s\n  DIV\n", firstExp, secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'&')) {
					// | '('EXP '&' EXP')'
					f.match('&');
					secondExp = getExp(f);
					result = String
							.format("%s%s\n  AND\n", firstExp, secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'|')) {
					// | '('EXP '|' EXP')'
					f.match('|');
					secondExp = getExp(f);
					result = String.format("%s%s\n  OR\n", firstExp, secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'>')) {
					// | '('EXP '>' EXP')'
					f.match('>');
					secondExp = getExp(f);
					result = String.format("%s%s\n  GREATER\n", firstExp,
							secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'<')) {
					// | '('EXP '<' EXP')'
					f.match('<');
					secondExp = getExp(f);
					result = String.format("%s%s\n  LESS\n", firstExp,
							secondExp);
				} else if (Utilities.peekIfTheNextTokenIsExpectedOperator(f,
						'=')) {
					// | '('EXP '=' EXP')'
					f.match('=');
					secondExp = getExp(f);
					result = String.format("%s%s\n  EQUAL\n", firstExp,
							secondExp);
				} else {
					// ERROR
					System.out.println("ERROR!");
					throw new RuntimeException(
							"Error in getExp Parsing : Non support binary operation or check if you have unmatching parenthesis");
				}
			}
			if (!f.check(')')) {
				errorReporting(f, "getExp", ")");
			}
			return result;
			// f.check(')');
		}
		// This is thrown with an example : Exp + ? <- missing second exp
		// TODO <-- Remove this once you're done with the exp programming
		throw new RuntimeException(
				"Error in getExp Parsing : Check Exp + ? <- missing second exp ");
	}

	// ACTUALS -> EXP (',' EXP )*
	// | METHOD '(' ACTUALS? ')' <-- ACTUALS is a invoked method parameters
	String getActuals(SamTokenizer f) {
		StringBuilder temp = new StringBuilder();
		String exp = getExp(f);
		temp.append(exp);
		// this actual is called means that it has at least one parameter
		this.numberOfParametersForInvokingMethod = 1;
		// if next token is ','
		while (Utilities.peekIfTheNextTokenIsExpectedOperator(f, ',')) {
			f.match(',');
			// f.check(',');
			exp = getExp(f);
			temp.append(exp);
			numberOfParametersForInvokingMethod++;
		}

		return "// acutals\n" + temp.toString();
	}

	// LITERAL -> INT | true | false
	String getLiteral(SamTokenizer f) {
		String result = null;
		if (Utilities.peekInteger(f)) // f.peekAtKind() == TokenType.INTEGER)
		{
			int value = getInt(f); // f.getInt();
			result = String.format("%d", value);
		} else {
			String literal = f.getString();
			if (literal.equals("true") || literal.equals("false")) {
				result = literal;
			} else {
				throw new RuntimeException("Literal is not INT|true|false");
			}
		}
		return result;
	}

	/**
	 * recursively get the Integer, notice that the negative numbers, like -5 is
	 * considered as the token "-" and 5.
	 * 
	 * @param f: SamTokenizer
	 * @return int value
	 */
	// * INT -> '-'? [1-9] [0-9]*
	//
	// I cannot use this code because of the negative number identification
	// String getInt(SamTokenizer f)
	// {
	// int value = f.getInt();
	// return String.format("%d", value);
	// }
	public int getInt(SamTokenizer f) {
		if (f.peekAtKind() == TokenType.INTEGER)
			return f.getInt();
		else if (f.peekAtKind() == TokenType.OPERATOR) {
			char op = f.getOp();
			int value = f.getInt();
			return -value;
		}
		throw new TokenizerException("There is no integer found");
		// return f.getInt(); // This is to introduce an error.
	}

	// ID -> [a-zA-Z] ( [a-zA-Z] | [0-9] | '_' )*
	String getId(SamTokenizer f) {
		// Are we sure if this is correct?
		String id = f.getWord();

		return id;
	}

	private static void showCommand() {
		System.out
				.println("Usage: java -jar assignment1.jar INPUT_BAIL_FILE_NAME OUTPUT_SAM_FILE_NAME");
	}

	public static void main(String[] args) throws IOException {
		if (args.length != 2) {
			showCommand();
			System.exit(0);
		}

		String bailFilePath = args[0];
		String samFilePath = args[1];

		BaliCompiler bali = new BaliCompiler();
		String srcBase = "/Users/smcho/Dropbox/smcho/topics/UTClass/CS380C/assignment1/mytest_sets";
		// String srcBase =
		// "/Users/smcho/Dropbox/smcho/topics/UTClass/CS380C/assignment1/mytest_sets/bad";
		// String mainName = "factorial";
		// String testName = mainName + ".bali";
		// String testNameAssembly = mainName + ".sam";
		// String path = new File(srcBase, testName).toString();
		// String assemblyPath = new File(srcBase, testNameAssembly).toString();

		String samCode = bali.compiler(bailFilePath);
		System.out.println(samCode);
		Utilities.save(samFilePath, samCode);
		System.exit(0);
	}
}
