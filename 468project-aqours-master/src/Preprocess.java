import java.util.*;

public class Preprocess extends MicroBaseListener {
	public Hashtable<String, int[]> localT = new Hashtable<String, int[]>();
	public Hashtable<String, String> funcType = new Hashtable<>();
	private String func_name;
	
	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		int LP[] = new int[2];
		func_name = ctx.getChild(2).getText().trim();
		funcType.put(func_name,ctx.getChild(1).getText().trim());
		String s = ctx.param_decl_list().getText();
		int numArg = 0;
		if (!s.trim().isEmpty()) {
			numArg = 1;
			for (int i = 0; i < s.length(); i++) {
				if (s.charAt(i) == ',') {
					numArg++;
				}
			}
		}
		LP[1] = numArg;
		localT.put(func_name, LP);
	}
}

