package core;

import java.util.Arrays;

public enum CommandType {
	push, pop, 
	mov, movw, movt, movne, movge, movgt, movlt, movle, moveq,
	sub, add, div, mul, neg,
	str, ldr,
	cmp, beq, bl, b, bne,
	
	;

	public Command parse(String line) {
		switch(this) {
		case movt:
		case movw:
			Command command = parseArgumentLine(this, line);
			String parameter = command.param1.split(":")[2];
			command.param1 = parameter.startsWith("#") ? parameter.substring(1, parameter.length()) : parameter;
			return command;
		default:
			return parseArgumentLine(this, line);
		}
	}

	private Command parseArgumentLine(CommandType type, String line) {
		String noOPCode = line.substring(type.toString().length()+1, line.length());
		String noExtraChars = noOPCode.replace("[", "").replace("]", "").replace("{", "").replace("}", "");
		String[] parameters = noExtraChars.split(",");
		Command command = new Command(type, parameters.length);
		
		String parameter = parameters[0].trim();
		command.param0 = parameter.startsWith("#") ? parameter.substring(1, parameter.length()) : parameter;
		
		if(parameters.length > 1) {
			parameter = parameters[1].trim();
			command.param1 = parameter.startsWith("#") ? parameter.substring(1, parameter.length()) : parameter;			
		}
		
		if(parameters.length > 2) {
			parameter = parameters[2].trim();
			command.param2 = parameter.startsWith("#") ? parameter.substring(1, parameter.length()) : parameter;			
		}
		System.out.println(line + " -> " + Arrays.toString(parameters) + " -> " + command);
		
		return command;
	}

}
