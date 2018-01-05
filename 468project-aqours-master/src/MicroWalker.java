import java.util.*;

public class MicroWalker extends MicroBaseListener {
	private List<IRNode> IRNodes = new ArrayList<IRNode>();
	private List<IRNode> IRNodesT = new ArrayList<IRNode>();
	private List<TinyNode> TinyNodes = new ArrayList<TinyNode>();
	private Stack<String> stack = new Stack<String>();
	private Stack<LABEL> LABELStack = new Stack<LABEL>();
	private Hashtable<String, String> variableMapTable = new Hashtable<String, String>();
	private int registerNumber = 0;
	private int LABELNnumber = 1;
	private int parameterRegisterNumber = 1;
	private int localRegisterNumber = 1;
	private Hashtable<String, String> replace = new Hashtable<String, String>();
	private boolean isGlobal = true;
	private Hashtable<String, int[]> localT = new Hashtable<String, int[]>();

	Stack<String> funcNameStack = new Stack<>();

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
			if (firstOperand != null) {
				firstOperand = firstOperand.replace("$T", "r");
			}
			String secondOperand = ir.getSecondOperand();
			if (secondOperand != null) {
				secondOperand = secondOperand.replace("$T", "r");
			}
			String answer = ir.getAnswer();
			if (answer != null) {
				answer = answer.replace("$T", "r");
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
			} else if (operator.equalsIgnoreCase("STOREI")) {
				if (firstOperand.charAt(0) != 'r' && answer.charAt(0) != 'r') {
					TinyNodes.add(new TinyNode("move", firstOperand,
							'r' + String.valueOf(registerNumber)));
					TinyNodes.add(new TinyNode("move", 'r' + String
							.valueOf(registerNumber), answer));
					registerNumber++;
				} else {
					TinyNodes.add(new TinyNode("move", firstOperand, answer));
				}

			} else if (operator.equalsIgnoreCase("STOREF")) {
				if (firstOperand.charAt(0) != 'r' && answer.charAt(0) != 'r') {
					TinyNodes.add(new TinyNode("move", firstOperand,
							'r' + String.valueOf(registerNumber)));
					TinyNodes.add(new TinyNode("move", 'r' + String
							.valueOf(registerNumber), answer));
					registerNumber++;
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
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jeq", answer, null));
			} else if (operator.equalsIgnoreCase("NE")) {
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jne", answer, null));
			} else if (operator.equalsIgnoreCase("GT")) {
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jgt", answer, null));
			} else if (operator.equalsIgnoreCase("GE")) {
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jge", answer, null));
			} else if (operator.equalsIgnoreCase("LT")) {
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jlt", answer, null));
			} else if (operator.equalsIgnoreCase("LE")) {
				cmp(firstOperand, secondOperand);
				TinyNodes.add(new TinyNode("jle", answer, null));
			} else if (operator.equalsIgnoreCase("JUMP")) {
				TinyNodes.add(new TinyNode("jmp", answer, null));
			} else if (ir.getOperator().equals("RET")) {
				TinyNodes.add(new TinyNode("unlnk", null, null));
				TinyNodes.add(new TinyNode("ret", null, null));
			} else if (ir.getOperator().equals("PUSH")) {
				TinyNodes.add(new TinyNode("push", answer, null));
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
			}

		}

	}

	private void cmp(String firstOperand, String secondOperand) {

		if (secondOperand.charAt(0) != 'r') {
			if (firstOperand.charAt(0) == 'r') {
				String temp = secondOperand;
				secondOperand = firstOperand;
				firstOperand = temp;
			} else {
				TinyNodes.add(new TinyNode("move", secondOperand, 'r' + String
						.valueOf(registerNumber)));
				secondOperand = 'r' + String.valueOf(registerNumber);
				registerNumber++;
			}
		}
		if ((variableMapTable.get(firstOperand) != null && variableMapTable
				.get(firstOperand).equals("FLOAT"))
				|| (variableMapTable.get(secondOperand) != null && variableMapTable
						.get(secondOperand).equals("FLOAT"))) {
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
		for (int i = 0; i < info.length; i++) {
			String key = info[i];
			variableMapTable.put(key, type);

			replace.put(key, "$-" + localRegisterNumber);
			variableMapTable.put("$-" + localRegisterNumber, type);
			localRegisterNumber++;
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
		answer = replace.get(answer);
		if (replace.containsKey(operand)){
			operand = replace.get(operand);
		}
		if (replace.containsKey(answer)){
			operand = replace.get(answer);
		}
		
		if (LABELStack.size() != 0
				&& LABELStack.peek().getName().equals("for_incr")) {
			if (variableMapTable.get(answer).contains("INT")) {
				IRNodesT.add(new IRNode("STOREI", operand, null, answer, "INT"));
			} else if (variableMapTable.get(answer).contains("FLOAT")) {
				IRNodesT.add(new IRNode("STOREF", operand, null, answer,
						"FLOAT"));
			}
		} else {
			if (variableMapTable.get(answer).contains("INT")) {
				IRNodes.add(new IRNode("STOREI", operand, null, answer, "INT"));
			} else if (variableMapTable.get(answer).contains("FLOAT")) {
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
			if (replace.containsKey(answer)) {
				answer = replace.get(answer);
			}
			if (variableMapTable.get(answer).contains("INT")) {
				IRNodes.add(new IRNode("READI", null, null, answer, "INT"));
			} else if (variableMapTable.get(answer).contains("FLOAT")) {
				IRNodes.add(new IRNode("READF", null, null, answer, "FLOAT"));
			} else if (variableMapTable.get(answer).contains("STRING")) {
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
			if (replace.containsKey(answer)) {
				answer = replace.get(answer);
			}
			if (variableMapTable.get(answer).contains("INT")) {
				IRNodes.add(new IRNode("WRITEI", null, null, answer, "INT"));
			} else if (variableMapTable.get(answer).contains("FLOAT")) {
				IRNodes.add(new IRNode("WRITEF", null, null, answer, "FLOAT"));
			} else if (variableMapTable.get(answer).contains("STRING")) {
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
		if (replace.containsKey(firstOperand)) {
			firstOperand = replace.get(firstOperand);
		}
		if (replace.containsKey(secondOperand)) {
			secondOperand = replace.get(secondOperand);
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
		if (variableMapTable.get(firstOperand).contains("INT")) {
			variableMapTable.put(answer, "INT");
			operator += "I";
			type = "INT";
		} else if (variableMapTable.get(firstOperand).contains("FLOAT")) {
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
		if (replace.containsKey(firstOperand)) {
			firstOperand = replace.get(firstOperand);
		}



		before = secondOperand;
		if (replace.containsKey(secondOperand)) {
			secondOperand = replace.get(secondOperand);
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
		if (variableMapTable.get(firstOperand) != null) {


			String type = "";
			if (variableMapTable.get(firstOperand).contains("INT")) {
				variableMapTable.put(answer, "INT");
				operator += "I";
				type = "INT";
			} else if (variableMapTable.get(firstOperand).contains("FLOAT")) {
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
		if (replace.containsKey(firstOperand)) {
			firstOperand = replace.get(firstOperand);
		}
		if (replace.containsKey(secondOperand)) {
			secondOperand = replace.get(secondOperand);
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
		if (variableMapTable.get(firstOperand).contains("INT")) {
			variableMapTable.put(answer, "INT");
			operator += "I";
			type = "INT";
		} else if (variableMapTable.get(firstOperand).contains("FLOAT")) {
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
		if (replace.containsKey(firstOperand)) {
			firstOperand = replace.get(firstOperand);
		}
		if (replace.containsKey(secondOperand)) {
			secondOperand = replace.get(secondOperand);
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

		if (variableMapTable.get(firstOperand) != null) {
			String type = "";
			if (variableMapTable.get(firstOperand).contains("INT")) {
				variableMapTable.put(answer, "INT");
				operator += "I";
				type = "INT";
			} else if (variableMapTable.get(firstOperand).contains("FLOAT")) {
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
		LABELStack.push(new LABEL("if", "label" + LABELNnumber++, "label"
				+ LABELNnumber++));
		// LABELNnumber = LABELNnumber + 2;
	}

	@Override
	public void exitIf_stmt(MicroParser.If_stmtContext ctx) {
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

			if (replace.containsKey(firstOperand)) {
				firstOperand = replace.get(firstOperand);
			}
			if (replace.containsKey(secondOperand)) {
				secondOperand = replace.get(secondOperand);
			}

			String type = variableMapTable.get(firstOperand).trim();

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
	public void exitFor_stmt(MicroParser.For_stmtContext ctx) {
		IRNodes.addAll(IRNodesT);
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
		IRNodes.add(new IRNode("LABEL", null, null, func_name, "NA"));
		IRNodes.add(new IRNode("LINK", null, null, null, "NA"));

		// System.out.println(func_name);
		// System.out.println("--------para list--------");
		// System.out.println(ctx.param_decl_list().getChildCount());

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
		if (replace.containsKey(returnReg.trim())) {
			returnReg = replace.get(returnReg.trim());
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

		if (variableMapTable.get(returnReg).contains("INT")) {
			IRNodes.add(new IRNode("STOREI", returnReg, numArg, "$R", "INT"));
		} else if (variableMapTable.get(returnReg).contains("FLOAT")) {
			IRNodes.add(new IRNode("STOREF", returnReg, numArg, "$R", "FLOAT"));
		} else if (variableMapTable.get(returnReg).contains("STRING")) {
			IRNodes.add(new IRNode("STORES", returnReg, numArg, "$R", "STRING"));
		}



		replace.put("$R", returnReg);
		IRNodes.add(new IRNode("RET", null, null, null, "NA"));
	}

	@Override
	public void exitFunc_decl(MicroParser.Func_declContext ctx) {
		String function_name = ctx.getChild(2).getText().trim();
		int LP[] = new int[2];
//		LP[0] = -(localRegisterNumber - 1);

		LP[0] = localRegisterNumber;
		LP[1] = parameterRegisterNumber - 1;
		localT.put(function_name, LP);
		parameterRegisterNumber = 1;
		localRegisterNumber = 1;
		registerNumber = 1;
		replace.clear();
		if (IRNodes.get(IRNodes.size() - 1).getOperator() != "RET") {
			IRNodes.add(new IRNode("RET", null, null, null, "NA"));
		}
		IRNodes.add(new IRNode("NEWLINE", null, null, null, "NA"));
	}

	@Override
	public void exitCall_expr(MicroParser.Call_exprContext ctx) {
		String[] chop_chop = ctx.getChild(2).getText().trim().split(",");
		int count = 0;
		boolean freeze = false;
		for (String timber : chop_chop) {
			if (timber.contains("(")) {
				freeze = true;
			} else if (timber.contains(")")) {
				freeze = false;
			}

			if (!freeze) {
				count++;
			}
		}
		Stack<String> lumberjack = new Stack<String>();
		IRNodes.add(new IRNode("PUSH", null, null, null, "NA"));
		for (int i = 0; i < count; i++) {
			lumberjack.push(stack.pop());
		}
		String func_name = stack.pop();
		List<String> the_log = new ArrayList<String>();
		while (!lumberjack.isEmpty()) {
			the_log.add(lumberjack.pop());
		}
		for (String rage : the_log) {
			if (replace.containsKey(rage.trim())) {
				IRNodes.add(new IRNode("PUSH", null, null, replace.get(rage
						.trim()), "NA"));
			} else {
				IRNodes.add(new IRNode("PUSH", null, null, rage.trim(), "NA"));
			}
		}
		IRNodes.add(new IRNode("JSR", null, null, func_name.trim(), "NA"));
		for (String rage : the_log) {
			IRNodes.add(new IRNode("POP", null, null, null, "NA"));
		}
		IRNodes.add(new IRNode("POP", null, null, 'r' + String
				.valueOf(registerNumber), "NA"));
		stack.push('r' + String.valueOf(registerNumber));
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
