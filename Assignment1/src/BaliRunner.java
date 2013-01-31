package assignment1;

import java.io.File;
import java.io.IOException;

import assignment1.BaliCompiler;
/**
 * The entrance of the program
 * @author Sungmin Cho, Jinruhua 
 *
 */
public class BaliRunner {
	public static void main(String[] args) throws IOException
	{
		BaliCompiler bali = new BaliCompiler();
		String srcBase = "/Users/smcho/Dropbox/smcho/topics/UTClass/CS380C/assignment1/mytest_sets/bad";
		//String srcBase = "/Users/smcho/Dropbox/smcho/topics/UTClass/CS380C/assignment1/mytest_sets/bad";
		String mainName = "bad.if-else";
		String testName = mainName + ".bali";
		String testNameAssembly = mainName + ".sam";
		String path = new File(srcBase, testName).toString();
		String assemblyPath = new File(srcBase, testNameAssembly).toString();
		String samCode = bali.compiler(path);
		System.out.println(samCode);
		Utilities.save(assemblyPath, samCode);
//		System.out.println("The name of methods");
//		Utilities.printSet(BaliCompiler.methodSymbolTable);
//		System.out.println("The name of variables");
//		Utilities.printSet(BaliCompiler.variableSymbolTable);
	}
}
