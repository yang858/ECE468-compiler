public class IRNode {
	private String operator;
	private String firstOperand;
	private String secondOperand;
	private String answer;
	private String type;

	public IRNode() {
		super();
		// TODO Auto-generated constructor stub
	}

	public IRNode(String operator, String firstOperand, String secondOperand,
			String answer) {
		super();
		this.operator = operator;
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
		this.answer = answer;
	}

	
	public IRNode(String operator, String firstOperand, String secondOperand,
			String answer, String type) {
		super();
		this.operator = operator;
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
		this.answer = answer;
		this.type = type;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getFirstOperand() {
		return firstOperand;
	}

	public void setFirstOperand(String firstOperand) {
		this.firstOperand = firstOperand;
	}

	public String getSecondOperand() {
		return secondOperand;
	}

	public void setSecondOperand(String secondOperand) {
		this.secondOperand = secondOperand;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	@Override
	public String toString() {
		String string = ";" + operator;
		if (firstOperand != null) {
			string += " " + firstOperand;
		}

		if (secondOperand != null) {
			string += " " + secondOperand;
		}

		if (answer != null) {
			string += " " + answer;
		}
		 return string;
	}

}
