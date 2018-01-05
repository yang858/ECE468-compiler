import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;
public class Micro {
	public static void main(String[] args) throws Exception {
		CharStream filename = new ANTLRFileStream(args[0]);
		MicroLexer microLexer = new MicroLexer(filename);
		CommonTokenStream token = new CommonTokenStream(microLexer);
		MicroParser parser = new MicroParser(token);
		ANTLRErrorStrategy error = new CustomErrorStrategy();
		
		parser.setErrorHandler(error);
		ParseTree parseTree = parser.program();
		Preprocess preprocess = new Preprocess();
		Optimizer myMicroListener = new Optimizer();
		ParseTreeWalker parseTreeWalker = new ParseTreeWalker();
		parseTreeWalker.walk(preprocess, parseTree);
		myMicroListener.funcType = preprocess.funcType;
		myMicroListener.localT = preprocess.localT;
		parseTreeWalker.walk(myMicroListener, parseTree);
	}
}

class CustomErrorStrategy extends DefaultErrorStrategy {
	@Override
	public void reportError(Parser recognizer, RecognitionException e) {
		throw e;

	}
}
