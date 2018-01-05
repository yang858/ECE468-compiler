import java.util.*;

public class Optimizer extends MicroBaseListener {
	private List<IRNode> IRNodes = new ArrayList<IRNode>();
	private List<IRNode> IRNodesT = new ArrayList<IRNode>();
	private List<TinyNode> TinyNodes = new ArrayList<TinyNode>();
	private Stack<String> stack = new Stack<String>();
	private Stack<LABEL> LABELStack = new Stack<LABEL>();
	private Hashtable<String, String> variableMapTable = null;
	private int registerNumber = 0;
	private int LABELNnumber = 1;
	private int parameterRegisterNumber = 1;
	private int localRegisterNumber = 1;
	private Hashtable<String, String> replace = null;
	private ArrayList<Hashtable<String, String>> rl = new ArrayList<>();
	private ArrayList<Hashtable<String, String>> vml = new ArrayList<>();
	private boolean isGlobal = true;
	public Hashtable<String, int[]> localT = new Hashtable<String, int[]>();
	//step7 begin
	private int registerLimit = 4;
	private RegisterAllocation allocator = new RegisterAllocation(registerLimit);
	//step end
	Stack<String> funcNameStack = new Stack<>();
	String fname = null;
	public String regstr=null;
	//step8 begin
	public Hashtable<String, String> funcType = new Hashtable<>();
	public String findValName(String key)
	{
		String ret = null;
		for(int i=rl.size()-1;i>=0;i--)
		{
			if(rl.get(i).containsKey(key))
			{
				ret = rl.get(i).get(key);
				break;
			}
		}
		return ret;
	}
	public String findValType(String key)
	{
		String ret = null;
		for(int i=vml.size()-1;i>=0;i--)
		{
			if(vml.get(i).containsKey(key))
			{
				ret = vml.get(i).get(key);
				break;
			}
		}
		return ret;	
	}
	//step8 end
	@Override
	public void exitProgram(MicroParser.ProgramContext ctx) {

//		System.out.println(variableMapTable);

//		System.out.println(numArguments);


		System.out.println(";IR code\n;LABEL main\n;LINK");
		for (int i = 0; i < IRNodes.size(); i++) {
			System.out.println(IRNodes.get(i).toString());
		}
		System.out.println(";RET\n;tiny code");
		InitTinyNodes();
		for (int i = 0; i < TinyNodes.size(); i++) {
			System.out.println(TinyNodes.get(i).toString());
		}
//		System.out.println("sys halt");

		System.out.println("end");
	}

	private void InitTinyNodes() {
		for (int i = 0; i < IRNodes.size(); i++) {
			IRNode ir = IRNodes.get(i);
			int previousIRNode = i - 1;
			if (i == 0) {
				previousIRNode = 0;
			}
			IRNode ir_previous = IRNodes.get(previousIRNode);
			String operator = ir.getOperator();
			String firstOperand = ir.getFirstOperand();
			String ft = firstOperand;
			if (firstOperand != null&&firstOperand.startsWith("$T")) {
				firstOperand = allocator.ensure(firstOperand);
			}
			String secondOperand = ir.getSecondOperand();
			String st = secondOperand;
			if (secondOperand != null&&secondOperand.startsWith("$T")) {
				secondOperand = allocator.ensure(secondOperand);
			}
			String answer = ir.getAnswer();
			//System.out.println(String.format("%s,%s,%s",ft,st,answer));
			if (answer != null) {
				//answer = answer.replace("$T", "r");
				if(answer.startsWith("$T"))
				{
					answer = allocator.ensure(answer);
					if(answer==null)
					{
						TinyNodes.add(new TinyNode("push", null, "r0"));
						regstr = allocator.registers[0];
						allocator.registers[0] = ir.getAnswer();
						answer = "r0";
					}
				}
				if (answer.equals("$R")) {
					

//					System.out.println("parameterRegisterNumber: " + parameterRegisterNumber);
					answer="$"+ (Integer.parseInt(ir.getSecondOperand()) + 4 + 2);

// 					System.out.println("--------------$R-----------");

// 					System.out.println(ir);

// 					System.out.println(answer);
// //					parameterRegisterNumber++;

// 					System.out.println(ir);
// 					System.out.println("-------------");
// 					System.out.println(parameterRegisterNumber);
				}
			}
			if (operator.equalsIgnoreCase("ADDI")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("addi", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("ADDF")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("addr", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("SUBI")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("subi", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("SUBF")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("subr", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("MULTI")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("muli", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("MULTF")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("mulr", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("DIVI")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("divi", secondOperand, answer));
			} else if (operator.equalsIgnoreCase("DIVF")) {
				TinyNodes.add(new TinyNode("move", firstOperand, answer));
				TinyNodes.add(new TinyNode("divr", secondOperand, answer));
			}
			else if (operator.equalsIgnoreCase("STOREI")) {
				if (firstOperand.charAt(0) != 'r' && answer.charAt(0) != 'r') {
//					TinyNodes.add(new TinyNode("move", firstOperand,
//							'r' + String.valueOf(registerNumber)));
//					TinyNodes.add(new TinyNode("move", 'r' + String
//							.valueOf(registerNumber), answer));
//					registerNumber++;
					String r=null;
					String tmp=String.format("$T%d",registerNumber++);
					r = allocator.ensure(tmp);
					TinyNodes.add(new TinyNode("move", firstOperand,r));
					//System.out.println(firstOperand+","+r);
					allocator.free(tmp);
					TinyNodes.add(new TinyNode("move", r,answer));
					if(regstr!=null)
					{
						String tmpr = allocator.ensure(regstr);
						regstr = null;
						TinyNodes.add(new TinyNode("pop", null, tmpr));
					}
					//System.out.println(r+","+answer);
				} else {
					TinyNodes.add(new TinyNode("move", firstOperand, answer));
				}

			} else if (operator.equalsIgnoreCase("STOREF")) {
				if (firstOperand.charAt(0) != 'r' && answer.charAt(0) != 'r') {
//					TinyNodes.add(new TinyNode("move", firstOperand,
//							'r' + String.valueOf(registerNumber)));
//					TinyNodes.add(new TinyNode("move", 'r' + String
//							.valueOf(registerNumber), answer));
//					registerNumber++;
					String r=null;
					String tmp=String.format("$T%d",registerNumber++);
					r = allocator.ensure(tmp);
					TinyNodes.add(new TinyNode("move", firstOperand,r));
					allocator.free(tmp);
					TinyNodes.add(new TinyNode("move", r,answer));
					if(regstr!=null)
					{
						String tmpr = allocator.ensure(regstr);
						regstr = null;
						TinyNodes.add(new TinyNode("pop", null, tmpr));
					}
				} else {
					TinyNodes.add(new TinyNode("move", firstOperand, answer));
				}
			} else if (operator.equalsIgnoreCase("READI")) {
				TinyNodes.add(new TinyNode("sys readi", answer, null));
			} else if (operator.equalsIgnoreCase("READF")) {
				TinyNodes.add(new TinyNode("sys readr", answer, null));
			} else if (operator.equalsIgnoreCase("WRITEI")) {
				TinyNodes.add(new TinyNode("sys writei", answer, null));
			} else if (operator.equalsIgnoreCase("WRITEF")) {
				TinyNodes.add(new TinyNode("sys writer", answer, null));
			} else if (operator.equalsIgnoreCase("WRITES")) {
				TinyNodes.add(new TinyNode("sys writes", answer, null));
			} else if (operator.equalsIgnoreCase("LABEL")) {
				TinyNodes.add(new TinyNode("label", answer, null));
			} else if (operator.equalsIgnoreCase("EQ")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jeq", answer, null));
			} else if (operator.equalsIgnoreCase("NE")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jne", answer, null));
			} else if (operator.equalsIgnoreCase("GT")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jgt", answer, null));
			} else if (operator.equalsIgnoreCase("GE")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jge", answer, null));
			} else if (operator.equalsIgnoreCase("LT")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jlt", answer, null));
			} else if (operator.equalsIgnoreCase("LE")) {
				cmp(firstOperand, secondOperand,ir.getType());
				TinyNodes.add(new TinyNode("jle", answer, null));
			} else if (operator.equalsIgnoreCase("JUMP")) {
				TinyNodes.add(new TinyNode("jmp", answer, null));
			} else if (ir.getOperator().equals("RET")) {
				TinyNodes.add(new TinyNode("unlnk", null, null));
				TinyNodes.add(new TinyNode("ret", null, null));
			} else if (ir.getOperator().equals("PUSH")) {
				TinyNodes.add(new TinyNode("push", answer, null));
				if(answer!=null&&answer.charAt(0)=='r')
				{
					//System.out.println("rrrrrrrrrrrrrrrrrrrr"+answer);
					allocator.free(Integer.parseInt(answer.substring(1)));
					if(regstr!=null)
					{
						String tmpr = allocator.ensure(regstr);
						regstr = null;
						TinyNodes.add(new TinyNode("pop", null, tmpr));
					}
				}
			} else if (ir.getOperator().equals("POP")) {
				TinyNodes.add(new TinyNode("pop", answer, null));
			} else if (ir.getOperator().equals("JSR")) {
				TinyNodes.add(new TinyNode("push", "r0", null));
				TinyNodes.add(new TinyNode("push", "r1", null));
				TinyNodes.add(new TinyNode("push", "r2", null));
				TinyNodes.add(new TinyNode("push", "r3", null));
				TinyNodes.add(new TinyNode("jsr", answer, null));
				TinyNodes.add(new TinyNode("pop", "r3", null));
				TinyNodes.add(new TinyNode("pop", "r2", null));
				TinyNodes.add(new TinyNode("pop", "r1", null));
				TinyNodes.add(new TinyNode("pop", "r0", null));
			} else if (ir.getOperator().equals("LINK")) {
				localRegisterNumber = localT.get(ir_previous.getAnswer())[0];
//				parameterRegisterNumber = localT.get(ir_previous.getAnswer())[1];
				TinyNodes.add(new TinyNode("link", Integer
						.toString(localRegisterNumber - 1), null));
			} else if (ir.getOperator().equals("EOGD")) {
				TinyNodes.add(new TinyNode("push", null, null));
				TinyNodes.add(new TinyNode("push", "r0", null));
				TinyNodes.add(new TinyNode("push", "r1", null));
				TinyNodes.add(new TinyNode("push", "r2", null));
				TinyNodes.add(new TinyNode("push", "r3", null));
				TinyNodes.add(new TinyNode("jsr", "main", null));
				TinyNodes.add(new TinyNode("sys halt", null, null));
			} else if(ir.getOperator().equals("FUNCEXIT"))
			{
				//init register allocation
				allocator.init();
			}
			if(ft!=null&&ft.startsWith("$T"))
			{
				allocator.free(ft);
				if(regstr!=null)
				{
					String tmpr = allocator.ensure(regstr);
					regstr = null;
					TinyNodes.add(new TinyNode("pop", null, tmpr));
				}
			}
			if(st!=null&&st.startsWith("$T"))
			{
				allocator.free(st);
				if(regstr!=null)
				{
					String tmpr = allocator.ensure(regstr);
					regstr = null;
					TinyNodes.add(new TinyNode("pop", null, tmpr));
				}
			}
		}

	}

	private void cmp(String firstOperand, String secondOperand,String type) {
		//System.out.println("cmp:"+firstOperand+","+secondOperand);
		if (secondOperand.charAt(0) != 'r') {
			String tmp=String.format("$T%d",registerNumber++);
			String r = allocator.ensure(tmp);
			TinyNodes.add(new TinyNode("move", secondOperand,r));
			secondOperand = r;
			allocator.free(tmp);
			if(regstr!=null)
			{
				String tmpr = allocator.ensure(regstr);
				regstr = null;
				TinyNodes.add(new TinyNode("pop", null, tmpr));
			}
			/*TinyNodes.add(new TinyNode("move", secondOperand, 'r' + String
					.valueOf(registerNumber)));
			secondOperand = 'r' + String.valueOf(registerNumber);
			registerNumber++;*/
		}
		if (type.equals("FLOAT")) {
			TinyNodes.add(new TinyNode("cmpr", firstOperand, secondOperand));
		} else {
			TinyNodes.add(new TinyNode("cmpi", firstOperand, secondOperand));
		}
	}

	@Override
	public void exitId(MicroParser.IdContext ctx) {
		stack.push(ctx.getText().trim());
	}

	// ////////////1
	// @Override
	// public void exitAssign_stmt(MicroParser.Assign_stmtContext ctx) {
	// String operand = stack.pop();
	// String dest = stack.pop();
	// if (replace.containsKey(dest)) {
	// dest = replace.get(dest);
	// }
	// if (replace.containsKey(operand)) {
	// operand = replace.get(operand);
	// }
	// if (variableMapTable.get(dest).contains("INT")) {
	// IRNodes.add(new IRNode("STOREI", operand, null, dest, "INT"));
	// } else if (variableMapTable.get(dest).contains("FLOAT")) {
	// IRNodes.add(new IRNode("STOREF", operand, null, dest, "FLOAT"));
	// }
	// }

	@Override
	public void enterString_decl(MicroParser.String_declContext ctx) {
		TinyNodes.add(new TinyNode("str", ctx.getChild(1).getText(), ctx
				.getChild(3).getText()));
	}

	@Override
	public void exitString_decl(MicroParser.String_declContext ctx) {
		// stack.push(ctx.getChild(0).getText().trim());
		// variableMapTable.put(ctx.getChild(1).getText().trim(),
		// ctx.getChild(0)
		// .getText().trim());
		String name = ctx.getChild(1).getText().trim();
		String stringContent = ctx.getChild(3).getText().trim();
		if(rl.size()==0)
		{
			//global decl
			rl.add(new Hashtable<String, String>());
		}
		if(vml.size()==0)
		{
			//global decl
			vml.add(new Hashtable<String, String>());
		}
		variableMapTable = vml.get(vml.size()-1);
		variableMapTable.put(name, "STRING");
		IRNodes.add(new IRNode("STRING", name, null, stringContent, "STRING"));
	}

	@Override
	public void exitVar_decl(MicroParser.Var_declContext ctx) {
		String type = ctx.getChild(0).getText();
		while (!stack.isEmpty()) {
			stack.pop();
		}
		String[] info = ctx.getChild(1).getText().trim().split(",");
		if(info.length>0&&rl.size()==0)
		{
			//global decl
			rl.add(new Hashtable<String, String>());
		}
		if(info.length>0&&vml.size()==0)
		{
			//global decl
			vml.add(new Hashtable<String, String>());
		}
		//step8 begin
		int depth = rl.size()-1;
		replace = rl.get(depth);
		variableMapTable = vml.get(depth);
		//step8 end
		for (int i = 0; i < info.length; i++) {
			String key = info[i];
			variableMapTable.put(key, type);
			if(isGlobal)
			{
				TinyNodes.add(new TinyNode("var", key, null));
				replace.put(key, key);
				variableMapTable.put(key, type);					
			}else
			{
				replace.put(key, "$-" + localRegisterNumber);
				variableMapTable.put("$-" + localRegisterNumber, type);	
				localRegisterNumber++;
			}
			// TinyNodes.add(new TinyNode("var", key, null));
			// if (isGlobal) {
			// IRNodes.add(new IRNode("VAR", key, null, null, type.trim()));
			// }
		}
	}

	@Override
	public void exitAssign_expr(MicroParser.Assign_exprContext ctx) {
		// String type = variableMapTable.get(ctx.getChild(0).getText());
		// // Node expr = getNode(ctx.getChild(2));
		// System.err.println(ctx.getChild(2));
		// String opName = "";
		// if(type == null) {
		// return;
		// } else if(type.equals("INT")) {
		// opName = "STOREI";
		// } else if(type.equals("FLOAT")) {
		// opName = "STOREF";
		// } else {
		// return;
		// }
		// // IRNode irNode = new IRNode(opName,expr.getValue(), null,
		// ctx.getChild(0).getText());
		// // IRList.add(irNode);
		String operand = stack.pop();
		String answer = stack.pop();
		//System.out.println("test:"+operand+","+answer);
//		answer = replace.get(answer);
		answer = findValName(answer);
		//System.out.println(answer);
		if (findValName(operand)!=null/*replace.containsKey(operand)*/){
			operand = findValName(operand);
		}
		/*if (findValName(answer)!=null){
			operand = findValName(answer);
		}*/
		//System.out.println(operand);
		if (LABELStack.size() != 0
				&& LABELStack.peek().getName().equals("for_incr")) {
			if (findValType(answer).contains("INT")) {
				IRNodesT.add(new IRNode("STOREI", operand, null, answer, "INT"));
			} else if (findValType(answer).contains("FLOAT")) {
				IRNodesT.add(new IRNode("STOREF", operand, null, answer,
						"FLOAT"));
			}
		} else {
			if (findValType(answer).contains("INT")) {
				IRNodes.add(new IRNode("STOREI", operand, null, answer, "INT"));
			} else if (findValType(answer).contains("FLOAT")) {
				IRNodes.add(new IRNode("STOREF", operand, null, answer, "FLOAT"));
			}
		}
	}

	@Override
	public void exitRead_stmt(MicroParser.Read_stmtContext ctx) {

		String[] info = ctx.getChild(2).getText().trim().split(",");
		for (int i = 0; i < info.length; i++) {
			stack.pop();
			String answer = info[i];
			if (findValName(answer)!=null) {
				answer = findValName(answer);
			}
			if (findValType(answer).contains("INT")) {
				IRNodes.add(new IRNode("READI", null, null, answer, "INT"));
			} else if (findValType(answer).contains("FLOAT")) {
				IRNodes.add(new IRNode("READF", null, null, answer, "FLOAT"));
			} else if (findValType(answer).contains("STRING")) {
				IRNodes.add(new IRNode("READS", null, null, answer, "STRING"));
			}
		}
	}

	@Override
	public void exitWrite_stmt(MicroParser.Write_stmtContext ctx) {
		String[] info = ctx.getChild(2).getText().trim().split(",");
		for (int i = 0; i < info.length; i++) {
			stack.pop();
			String answer = info[i];
			if (findValName(answer)!=null) {
				answer = findValName(answer);
			}
			if (findValType(answer).contains("INT")) {
				IRNodes.add(new IRNode("WRITEI", null, null, answer, "INT"));
			} else if (findValType(answer).contains("FLOAT")) {
				IRNodes.add(new IRNode("WRITEF", null, null, answer, "FLOAT"));
			} else if (findValType(answer).contains("STRING")) {
				IRNodes.add(new IRNode("WRITES", null, null, answer, "STRING"));
			}
		}
	}

	@Override
	public void exitExpr_prefix(MicroParser.Expr_prefixContext ctx) {
		if (ctx.getChildCount() == 0) {
			return;
		}

		if (ctx.getChild(0).getChildCount() <= 1) {
			return;
		}
		String extra_op = stack.pop();
		// stack: first in but last out
		String secondOperand = stack.pop();
		String operator = stack.pop();
		String firstOperand = stack.pop();
		String answer = "$T" + String.valueOf(registerNumber++);
		stack.push(answer);
		if (findValName(firstOperand)!=null) {
			firstOperand = findValName(firstOperand);
		}
		if (findValName(secondOperand)!=null) {
			secondOperand = findValName(secondOperand);
		}
		if (operator.contains("+")) {
			operator = "ADD";
		} else if (operator.contains("-")) {
			operator = "SUB";
		} else if (operator.contains("*")) {
			operator = "MULT";
		} else if (operator.contains("/")) {
			operator = "DIV";
		}

		String type = "";
		variableMapTable = vml.get(vml.size()-1);
		if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("INT")) {
			variableMapTable.put(answer, "INT");
			operator += "I";
			type = "INT";
		} else if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("FLOAT")) {
			variableMapTable.put(answer, "FLOAT");
			operator += "F";
			type = "FLOAT";
		}
		IRNodes.add(new IRNode(operator, firstOperand, secondOperand, answer,
				type));
		stack.push(extra_op);
	}

	@Override
	public void exitFactor(MicroParser.FactorContext ctx) {
		if (ctx.getText().matches("[0-9]+")) {
			return;
		} else if (ctx.getText().matches("[0-9]*\\.[0-9]+")) {
			return;
		}
		if (ctx.getChild(0).getChildCount() <= 1) {
			return;
		}
		// stack: first in but last out
		String secondOperand = stack.pop();
		String operator = stack.pop();
		String firstOperand = stack.pop();


		String before = firstOperand;
		String answer = "$T" + String.valueOf(registerNumber++);
		stack.push(answer);
		if (findValName(firstOperand)!=null) {
			firstOperand = findValName(firstOperand);
		}



		before = secondOperand;
		if (findValName(secondOperand)!=null) {
			secondOperand = findValName(secondOperand);
		}



		if (operator.contains("+")) {
			operator = "ADD";
		} else if (operator.contains("-")) {
			operator = "SUB";
		} else if (operator.contains("*")) {
			operator = "MULT";
		} else if (operator.contains("/")) {
			operator = "DIV";
		}
		if (findValType(firstOperand) != null) {
			variableMapTable = vml.get(vml.size()-1);

			String type = "";
			if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("INT")) {
				variableMapTable.put(answer, "INT");
				operator += "I";
				type = "INT";
			} else if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("FLOAT")) {
				variableMapTable.put(answer, "FLOAT");
				operator += "F";
				type = "FLOAT";
			}
			IRNodes.add(new IRNode(operator, firstOperand, secondOperand,
					answer, type));
		}
	}

	@Override
	public void exitFactor_prefix(MicroParser.Factor_prefixContext ctx) {
		if (ctx.getChildCount() == 0) {
			return;
		}
		if (ctx.getChild(0).getChildCount() <= 1) {
			return;
		}
		String extra_op = stack.pop();
		// stack: first in but last out
		String secondOperand = stack.pop();
		String operator = stack.pop();
		String firstOperand = stack.pop();
		String answer = "$T" + String.valueOf(registerNumber++);
		stack.push(answer);
		if (findValName(firstOperand)!=null) {
			firstOperand = findValName(firstOperand);
		}
		if (findValName(secondOperand)!=null) {
			secondOperand = findValName(secondOperand);
		}
		if (operator.contains("+")) {
			operator = "ADD";
		} else if (operator.contains("-")) {
			operator = "SUB";
		} else if (operator.contains("*")) {
			operator = "MULT";
		} else if (operator.contains("/")) {
			operator = "DIV";
		}
		
		String type = "";
		variableMapTable = vml.get(vml.size()-1);
		if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("INT")) {
			variableMapTable.put(answer, "INT");
			operator += "I";
			type = "INT";
		} else if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("FLOAT")) {
			variableMapTable.put(answer, "FLOAT");
			operator += "F";
			type = "FLOAT";
		}
		IRNodes.add(new IRNode(operator, firstOperand, secondOperand, answer,
				type));
		stack.push(extra_op);
	}

	@Override
	public void exitExpr(MicroParser.ExprContext ctx) {
		if (ctx.getText().matches("[0-9]+")) {
			return;
		} else if (ctx.getText().matches("[0-9]*\\.[0-9]+")) {
			return;
		}
		if (ctx.getChild(0).getChildCount() <= 1) {
			return;
		}

		// stack: first in but last out
		String secondOperand = stack.pop();
		String operator = stack.pop();
		String firstOperand = stack.pop();
		String answer = "$T" + String.valueOf(registerNumber++);
		stack.push(answer);
		if (findValName(firstOperand)!=null) {
			firstOperand = findValName(firstOperand);
		}
		if (findValName(secondOperand)!=null) {
			secondOperand = findValName(secondOperand);
		}

		if (operator.contains("+")) {
			operator = "ADD";
		} else if (operator.contains("-")) {
			operator = "SUB";
		} else if (operator.contains("*")) {
			operator = "MULT";
		} else if (operator.contains("/")) {
			operator = "DIV";
		}

		if (findValType(firstOperand) != null) {
			String type = "";
			variableMapTable = vml.get(vml.size()-1);
			if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("INT")) {
				variableMapTable.put(answer, "INT");
				operator += "I";
				type = "INT";
			} else if (findValType(firstOperand)!=null&&findValType(firstOperand).contains("FLOAT")) {
				variableMapTable.put(answer, "FLOAT");
				operator += "F";
				type = "FLOAT";
			}
			if (LABELStack.size() != 0
					&& LABELStack.peek().getName().equals("for_incr")) {
				IRNodesT.add(new IRNode(operator, firstOperand, secondOperand,
						answer, type));
			} else {
				IRNodes.add(new IRNode(operator, firstOperand, secondOperand,
						answer, type));
			}
		}
	}

	@Override
	public void exitPrimary(MicroParser.PrimaryContext ctx) {
		// private List<IRNode> IRNodes = new ArrayList<IRNode>();
		variableMapTable = vml.get(vml.size()-1);
		if (LABELStack.size() != 0
				&& LABELStack.peek().getName().equals("for_incr")) {
			if (ctx.getText().matches("[0-9]+")) {
				String reg = "$T" + String.valueOf(registerNumber++);
				IRNodesT.add(new IRNode("STOREI", ctx.getText(), null, reg,
						"INT"));
				stack.push(reg);
				variableMapTable.put(reg, "INT");
			} else if (ctx.getText().matches("[0-9]*\\.[0-9]+")) {
				String reg = "$T" + String.valueOf(registerNumber++);
				IRNodesT.add(new IRNode("STOREF", ctx.getText(), null, reg,
						"FLOAT"));
				stack.push(reg);
				variableMapTable.put(reg, "FLOAT");
			}
		} else {
			if (ctx.getText().matches("[0-9]+")) {
				String reg = "$T" + String.valueOf(registerNumber++);
				IRNodes.add(new IRNode("STOREI", ctx.getText(), null, reg,
						"INT"));
				stack.push(reg);
				variableMapTable.put(reg, "INT");
			} else if (ctx.getText().matches("[0-9]*\\.[0-9]+")) {
				String reg = "$T" + String.valueOf(registerNumber++);
				IRNodes.add(new IRNode("STOREF", ctx.getText(), null, reg,
						"FLOAT"));
				stack.push(reg);
				variableMapTable.put(reg, "FLOAT");
			}
		}
	}

	@Override
	public void exitAddop(MicroParser.AddopContext ctx) {
		stack.push(ctx.getText());

	}

	@Override
	public void exitMulop(MicroParser.MulopContext ctx) {

		stack.push(ctx.getText());
	}

	@Override
	public void enterIf_stmt(MicroParser.If_stmtContext ctx) {
		//step8 begin
		rl.add(new Hashtable<String, String>());
		vml.add(new Hashtable<String, String>());
		//step8 end
		LABELStack.push(new LABEL("if", "label" + LABELNnumber++, "label"
				+ LABELNnumber++));
		// LABELNnumber = LABELNnumber + 2;
	}

	@Override
	public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
		//step8 begin
		rl.remove(rl.size()-1);
		vml.remove(vml.size()-1);
		//step8 end
		while (true) {
			if (LABELStack.peek().getName().equals("if")) {
				IRNodes.add(new IRNode("LABEL", null, null, LABELStack.pop()
						.getOp2(), "NA"));
				break;
			} else if (LABELStack.peek().getName().equals("else_if")) {
				// IRNodes.add(new IRNode("JUMP", null, null, LABELStack.peek()
				// .getOp2()));
				// IRNodes.add(new IRNode("LABEL", null, null, LABELStack.pop()
				// .getOp2()));
				// break;
				IRNodes.add(new IRNode("JUMP", null, null, LABELStack.peek()
						.getOp2(), "NA"));
				IRNodes.add(new IRNode("LABEL", null, null, LABELStack.pop()
						.getOp2(), "NA"));
				if (LABELStack.size() == 0
						|| (!LABELStack.peek().getName().equals("else_if") && !LABELStack
								.peek().getName().equals("if"))) {
					break;
				}
			} else {
				LABELStack.pop();
			}
		}
	}

	@Override
	public void enterElse_part(MicroParser.Else_partContext ctx) {
		//step8 begin
		rl.add(new Hashtable<String, String>());
		vml.add(new Hashtable<String, String>());
		//step8 end
		IRNodes.add(new IRNode("JUMP", null, null, LABELStack.peek().getOp2(),
				"NA"));
		IRNodes.add(new IRNode("LABEL", null, null, LABELStack.peek().getOp1(),
				"NA"));

		if (ctx.getChildCount() > 1) {
			LABELStack.push(new LABEL("else_if", null, LABELStack.peek()
					.getOp2()));
		}
	}
	@Override
	public void exitElse_part(MicroParser.Else_partContext ctx) {
		//step8 begin
		rl.remove(rl.size()-1);
		vml.remove(vml.size()-1);
		//step8 end
	}
	@Override
	public void exitCond(MicroParser.CondContext ctx) {
		if (ctx.getChildCount() == 1) {
			String register1 = "$T" + registerNumber++;
			String register2 = "$T" + registerNumber++;
			IRNodes.add(new IRNode("STOREI", "1", null, register1, "INT"));
			IRNodes.add(new IRNode("STOREI", "1", null, register2, "INT"));

			if (ctx.getChild(0).getText().equals("TRUE")) {

				IRNodes.add(new IRNode("NE", register1, register2, LABELStack
						.peek().getOp1(), "INT"));
			} else if (ctx.getChild(0).getText().equals("FALSE")) {
				IRNodes.add(new IRNode("EQ", register1, register2, LABELStack
						.peek().getOp1(), "INT"));

			}
		} else {
			// String secondOperand = stack.pop();
			// String firstOperand = stack.pop();

			String secondOperand = stack.pop();
			String firstOperand = stack.pop();
			String key = ctx.getChild(1).getText();

			if (findValName(firstOperand)!=null) {
				firstOperand = findValName(firstOperand);
			}
			if (findValName(secondOperand)!=null) {
				secondOperand = findValName(secondOperand);
			}

			String type = findValType(firstOperand).trim();
			String type2 = findValType(secondOperand).trim();
			if(type.equals("FLOAT")||type2.equals("FLOAT"))
				type = "FLOAT";

			switch (key) {
			case ">":
				IRNodes.add(new IRNode("LE", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;
			case ">=":
				IRNodes.add(new IRNode("LT", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;
			case "<":
				IRNodes.add(new IRNode("GE", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;
			case "<=":
				IRNodes.add(new IRNode("GT", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;
			case "=":
				IRNodes.add(new IRNode("NE", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;
			case "!=":
				IRNodes.add(new IRNode("EQ", firstOperand, secondOperand,
						LABELStack.peek().getOp1(), type));
				break;

			default:
				break;
			}
		}
	}

	@Override
	public void exitInit_stmt(MicroParser.Init_stmtContext ctx) {
		// System.out.println(ctx.getChild(0));
		/*
		 * LABELStack.push(new LABEL("for", "label" + LABELNnumber++, "label" +
		 * LABELNnumber++)); IRNodes.add(new IRNode("LABEL", null, null,
		 * LABELStack.peek().getOp1()));
		 */
		LABELStack.push(new LABEL("for_start", "label" + LABELNnumber++, null));
		IRNodes.add(new IRNode("LABEL", null, null, LABELStack.peek().getOp1(),
				"NA"));
	}

	@Override
	public void enterCond(MicroParser.CondContext ctx) {
		// System.out.println(LABELStack.peek().getOp1());
		if (LABELStack.peek().getName().equals("for_start")) {
			LABELStack
					.push(new LABEL("for_out", "label" + LABELNnumber++, null));
		}
	}

	@Override
	public void enterIncr_stmt(MicroParser.Incr_stmtContext ctx) {
		// LABELStack.push(new LABEL("Label", "label" + LABELNnumber++, "label"
		// + LABELNnumber++));
		// IRNodes.add(new IRNode("LABEL", null, null,
		// LABELStack.peek().getOp1()));
		LABELStack.push(new LABEL("for_incr", "label" + LABELNnumber++, null));
		IRNodesT.add(new IRNode("LABEL", null, null,
				LABELStack.peek().getOp1(), "NA"));
	}

	@Override
	public void exitIncr_stmt(MicroParser.Incr_stmtContext ctx) {
		// IRNodes.add(new IRNode("JUMP", null, null,
		// LABELStack.peek().getOp1()));
		if (LABELStack.peek().getName() != "for_start") {
			LABELStack.pop();
		} else {
			IRNodes.add(new IRNode("JUMP", null, null, LABELStack.peek()
					.getOp1(), "JUMP"));
		}
	}
	@Override
	public void enterFor_stmt(MicroParser.For_stmtContext ctx) {
		//step8 begin
		rl.add(new Hashtable<String, String>());
		vml.add(new Hashtable<String, String>());
		//step8 end		
	}
	@Override
	public void exitFor_stmt(MicroParser.For_stmtContext ctx) {
		//step8 begin
		rl.remove(rl.size()-1);
		vml.remove(vml.size()-1);
		//step8 end
		IRNodes.addAll(IRNodesT);
		IRNodesT.clear();
		LABEL label = null;

		while (true) {
			if (LABELStack.size() != 0) {
				if (LABELStack.peek().getName().equals("for_start")) {
					break;
				} else {
					label = LABELStack.pop();
				}
			} else {
				break;
			}
		}
		IRNodes.add(new IRNode("JUMP", null, null, LABELStack.pop().getOp1(),
				"NA"));
		IRNodes.add(new IRNode("LABEL", null, null, label.getOp1(), "NA"));
	}

	@Override
	public void exitParam_decl(MicroParser.Param_declContext ctx) {
		// System.err.println();
		String var = stack.pop();
		String type = ctx.getChild(0).getText();
		// String newPreg = newParamReg();

//		System.out.println("&&&&&&&&&&&");
//		System.out.println(var );
//
//		System.out.println(parameterRegisterNumber);

		String funName = funcNameStack.peek();

//		System.out.println(funName);

		String rep = "$" + ( numArguments.get(funName) + 4 + 2 - parameterRegisterNumber);

//		System.out.println(rep);
		//step8 begin
		int depth = rl.size()-1;
		replace = rl.get(depth);
		variableMapTable = vml.get(depth);
		//step8 end
		replace.put(var,  rep);
		variableMapTable.put(rep,
				type);
		parameterRegisterNumber++;

//		System.out.println("-----------");
//		System.out.println(parameterRegisterNumber);
	}

	Map<String, Integer> numArguments = new HashMap<>();



	@Override
	public void enterFunc_decl(MicroParser.Func_declContext ctx) {
		if (isGlobal) {
			isGlobal = false;
			IRNodes.add(new IRNode("EOGD", null, null, null, "NA"));
		}
		String func_name = ctx.getChild(2).getText().trim();
		fname = func_name;
		//step8 begin
		//funcType.put(func_name,ctx.getChild(1).getText().trim());
		//step8 end
		IRNodes.add(new IRNode("LABEL", null, null, func_name, "NA"));
		IRNodes.add(new IRNode("LINK", null, null, null, "NA"));
		
		// System.out.println(func_name);
		// System.out.println("--------para list--------");
		// System.out.println(ctx.param_decl_list().getChildCount());
		//step8 begin
		rl.add(new Hashtable<String, String>());
		vml.add(new Hashtable<String, String>());
		//step8 end
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

		numArguments.put(func_name, numArg);


//		System.out.println(numArg);
//		System.out.println(ctx.param_decl_list().getText() + "*");
//
//
//
//		System.out.println(ctx.param_decl_list().getChildCount());



		funcNameStack.push(func_name);

//		System.out.println(funcNameStack);
	}


	@Override public void exitFunc_body(MicroParser.Func_bodyContext ctx) {

		funcNameStack.pop();
	}

	@Override
	public void exitReturn_stmt(MicroParser.Return_stmtContext ctx) {

		String returnReg = stack.pop();

		// System.out.println(ctx.getParent().toString());


		// System.out.println("------return reg------");
		// System.out.println(returnReg);
		if (findValName(returnReg.trim())!=null) {
			returnReg = findValName(returnReg.trim());
		}

		String numArg = "0";

		if(!funcNameStack.isEmpty())
		{
			String funName = funcNameStack.peek();

			 numArg = "" + numArguments.get(funName);

//			System.out.println(funcNameStack);

//			System.out.println(funName + ":" + numArg);


		}else{

//			System.out.println("error");
		}


		// System.out.println("----exitReturn----" );

		// System.out.println(funName + ":" + numArg);

		if (findValType(returnReg).contains("INT")) {
			IRNodes.add(new IRNode("STOREI", returnReg, numArg, "$R", "INT"));
		} else if (findValType(returnReg).contains("FLOAT")) {
			IRNodes.add(new IRNode("STOREF", returnReg, numArg, "$R", "FLOAT"));
		} else if (findValType(returnReg).contains("STRING")) {
			IRNodes.add(new IRNode("STORES", returnReg, numArg, "$R", "STRING"));
		}
		//step8 begin
		int depth = rl.size()-1;
		replace = rl.get(depth);
		variableMapTable = vml.get(depth);
		//step8 end
		replace.put("$R", returnReg);
		IRNodes.add(new IRNode("RET", null, null, null, "NA"));
	}
	@Override
	public void exitFunc_decl(MicroParser.Func_declContext ctx) {
		String function_name = ctx.getChild(2).getText().trim();
		//int LP[] = new int[2];
		int LP[] = localT.get(function_name);
//		LP[0] = -(localRegisterNumber - 1);

		LP[0] = localRegisterNumber;
		//LP[1] = parameterRegisterNumber - 1;
		localT.put(function_name, LP);
		parameterRegisterNumber = 1;
		localRegisterNumber = 1;
		registerNumber = 1;
		//step8 begin
//		replace.clear();
		rl.remove(rl.size()-1);
		vml.remove(vml.size()-1);
		//step8 end
		if (IRNodes.get(IRNodes.size() - 1).getOperator() != "RET") {
			IRNodes.add(new IRNode("RET", null, null, null, "NA"));
		}
		IRNodes.add(new IRNode("NEWLINE", null, null, null, "NA"));
		IRNodes.add(new IRNode("FUNCEXIT", null, null, null, "NA"));
	}

	@Override
	public void exitCall_expr(MicroParser.Call_exprContext ctx) {
		String[] chop_chop = ctx.getChild(2).getText().trim().split(",");
		int count = localT.get(ctx.getChild(0).getText().trim())[1];
		/*boolean freeze = false;
		for (String timber : chop_chop) {
			if (timber.contains("(")) {
				freeze = true;
			} else if (timber.contains(")")) {
				freeze = false;
			}

			if (!freeze) {
				count++;
			}
		}*/
		Stack<String> lumberjack = new Stack<String>();
		if (LABELStack.size() != 0
				&& LABELStack.peek().getName().equals("for_incr")) {
			IRNodesT.add(new IRNode("PUSH", null, null, null, "NA"));
		}else
		{
			IRNodes.add(new IRNode("PUSH", null, null, null, "NA"));
		}
		for (int i = 0; i < count; i++) {
			lumberjack.push(stack.pop());
		}
		String func_name = stack.pop();
		//func_name = ctx.getChild(0).getText().trim();
		//System.out.println(func_name+","+count+","+localT.get(func_name)[1]);
		//System.out.println(func_name+","+ctx.getChild(0).getText().trim());
		List<String> the_log = new ArrayList<String>();
		while (!lumberjack.isEmpty()) {
			the_log.add(lumberjack.pop());
		}
		if (LABELStack.size() != 0
				&& LABELStack.peek().getName().equals("for_incr")) {
			for (String rage : the_log) {
				if (findValName(rage.trim())!=null) {
					IRNodesT.add(new IRNode("PUSH", null, null, findValName(rage
							.trim()), "NA"));
				} else {
					IRNodesT.add(new IRNode("PUSH", null, null, rage.trim(), "NA"));
				}
			}
			IRNodesT.add(new IRNode("JSR", null, null, func_name.trim(), "NA"));
			for (String rage : the_log) {
				IRNodesT.add(new IRNode("POP", null, null, null, "NA"));
			}
			IRNodesT.add(new IRNode("POP", null, null, "$T" + String
					.valueOf(registerNumber), "NA"));			
		}else
		{
			for (String rage : the_log) {
				if (findValName(rage.trim())!=null) {
					IRNodes.add(new IRNode("PUSH", null, null, findValName(rage
							.trim()), "NA"));
				} else {
					IRNodes.add(new IRNode("PUSH", null, null, rage.trim(), "NA"));
				}
			}
			IRNodes.add(new IRNode("JSR", null, null, func_name.trim(), "NA"));
			for (String rage : the_log) {
				IRNodes.add(new IRNode("POP", null, null, null, "NA"));
			}
			IRNodes.add(new IRNode("POP", null, null, "$T" + String
					.valueOf(registerNumber), "NA"));			
		}
		stack.push("$T" + String.valueOf(registerNumber));
		variableMapTable = vml.get(vml.size()-1);
		//System.out.println(func_name);
		variableMapTable.put("$T" + String.valueOf(registerNumber),funcType.get(func_name)==null?"FLOAT":funcType.get(func_name));
		registerNumber++;
	}

	private class LABEL {
		private String name;
		private String op1;
		private String op2;

		public LABEL(String name, String op1, String op2) {
			super();
			this.name = name;
			this.op1 = op1;
			this.op2 = op2;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getOp1() {
			return op1;
		}

		public void setOp1(String op1) {
			this.op1 = op1;
		}

		public String getOp2() {
			return op2;
		}

		public void setOp2(String op2) {
			this.op2 = op2;
		}

	}
}

