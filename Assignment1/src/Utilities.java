package assignment1;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Set;

import edu.cornell.cs.sam.io.SamTokenizer;
import edu.cornell.cs.sam.io.Tokenizer.TokenType;

/**
 * Utilities class for SamTokenizer
 * @author Sungmin Cho, Jinruhua
 *
 */
public class Utilities {
/**
 * use SLL(1) algorithm. Peek if the next token is expected operator
 * @param f SamTokenizer
 * @param operator next operator when parsing	
 * @return whether the next token is expected operator
 */
	public static boolean peekIfTheNextTokenIsExpectedOperator(SamTokenizer f, char operator)
	{
		if (f.peekAtKind() == TokenType.OPERATOR) {
			char op = f.getOp();
			f.pushBack();
			if (op == operator) {
				return true;
			}
			else 
				return false;
		}
		else 
		    return false;
	}
	/**
	 * use SLL(1) algorithm. Peek if the next token is type
	 * @param f SamTokenizer 
	 * @return peek if the next token is type
	 */
	public static boolean peekIfTheNextTokenIsType(SamTokenizer f)
	{
		if (f.peekAtKind() == TokenType.WORD) {
			String word = f.getWord();
			f.pushBack();
			if (word.equals("int")) {
				return true;
			}
			else 
				return false;
		}
		else 
		    return false;
	}

	// The first set of "Statmements" are
	// 'return', 'if', 'while', 'break'
	// '{' <- block
	// ';' <- nothing
	// ID <- location
	/**
	 * peek if The next token is in first set of statement
	 * @param f: SamTokenizer
	 * @return  if The next token is in first set of statement
	 */
	public static boolean peekIfTheNextTokenIsInFirstSetOfStatement(
			SamTokenizer f) {
		// it returns true when the next keyword is type of WORD, and let the decide the 
		// production later.
		if (f.peekAtKind() == TokenType.WORD) {
			String word = f.getWord();
			f.pushBack();
			return true; // 
//			// if the word is one of the four keywords
//			if (word.equals("return") || word.equals("if") || word.equals("while") || word.equals("break")) {
//				return true;
//			}
//			// if the word is location
//			else {
//				return true;
//			}
		}
		else if (f.peekAtKind() == TokenType.OPERATOR)
		{
			char op = f.getOp();
			f.pushBack();
			
			if (op == '{' || op == ';') {
				return true;
			}
			else {
				return false;
			}
		}
		return false;
	}
/**
 * print a set of names
 * @param names set of names
 */
	public static void printSet(Set<String> names) {
		// TODO Auto-generated method stub
		for (String name : names) {
			System.out.println(name);
		}
		System.out.println("");
	}
/**
 * peek if the next token is in first set of literal
 * @param f SamTokenizer 
 * @return if the next token is in first set of literal
 */
	//		LITERAL    -> INT | true | false
	public static boolean peekIfTheNextTokenIsInFirstSetOfLiteral(SamTokenizer f) {
		if (peekInteger(f)) {
			return true;
		}
		if (f.peekAtKind() == TokenType.WORD) {
			String word = f.getWord();
			f.pushBack();
			if (word.equals("true") || word.equals("false")) {
				return true;
			}
			else 
				return false;
		}
		return false;
	}
/**
 * peek if the token is integer
 * @param f: SamTokenizer
 * @return if the token is integer
 */
	public static boolean peekInteger(SamTokenizer f)
	{
		if (f.peekAtKind() == TokenType.INTEGER)
		    return true;
		// -5 is recognized as '-' (operator) and 5 (integer).
		// So for this case, one should check operator and integer to return true
		if (f.peekAtKind() == TokenType.OPERATOR) {
			char op = f.getOp();
			if (op != '-') {
				f.pushBack();
				return false;
			}
			// op == '-'
			if (f.peekAtKind() == TokenType.INTEGER) {
				f.pushBack();
				return true;
			}
			return false;
		} else {
			return false;
		}
	}
/**
 * peek if the next token is id
 * @param f SamTokenizer
 * @return return true if the next token is id
 */
	public static boolean peekIfTheNextTokenIsId(SamTokenizer f) {
		// TODO Auto-generated method stub
		if (f.peekAtKind() == TokenType.WORD) {
			// we may need more checking such as the string 
			// is not keyword, but as long as it's word we return true;
			return true;
		}
		else {
			return false;
		}
	}
/**
 * save the assembly path and Sam Code
 * @param assemblyPath assembly path
 * @param samCode sam code
 * @throws IOException
 */
	public static void save(String assemblyPath, String samCode) throws IOException {
		// TODO Auto-generated method stub
		FileWriter fw = new FileWriter(assemblyPath);
		BufferedWriter bw = new BufferedWriter(fw);
		bw.write(samCode);
		bw.close();
	}
}
