package core;

public class Command {

	public final CommandType type;
	
	public String param0;
	public String param1;
	public String param2;

	public final int paramCount;

	public Command(CommandType type, int parameterCount) {
		this.type = type;
		this.paramCount = parameterCount;
	}
	
	public String toString() {
		String str = type.toString();
		str += " [";
		str += param0;
		if(paramCount > 1) {
			str += ", ";
			str += param1;
		}
		if(paramCount > 2) {
			str += ", ";
			str += param2;
		}
		str += "] ";
//		str += "(" + paramCount + " parameters)";
		return str;
	}

}
