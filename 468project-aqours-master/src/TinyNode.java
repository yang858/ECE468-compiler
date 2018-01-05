public class TinyNode {
	private String operator;
	private String firstOperand;
	private String secondOperand;

	public TinyNode() {
		super();
		// TODO Auto-generated constructor stub
	}

	public TinyNode(String operator, String firstOperand, String secondOperand) {
		super();
		this.operator = operator;
		this.firstOperand = firstOperand;
		this.secondOperand = secondOperand;
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

	@Override
	public String toString() {

		String string = "" + operator;
		if (firstOperand != null) {
			string += " " + firstOperand;
		}

		if (secondOperand != null) {
			string += " " + secondOperand;
		}
		return string;
	}

}
