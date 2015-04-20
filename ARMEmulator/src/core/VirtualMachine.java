package core;

import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JOptionPane;

public class VirtualMachine {
	
	private String r0 = "r0", r1 = "r1", r2 = "r2", r3 = "r3", r4 = "r4", r5 = "r5", r6 = "r6", r7 = "r7", r8 = "r8", r9 = "r9", r10 = "r10", r11 = "r11";
	private String sp = "sp", fp = "fp", pc = "pc", lr = "lr";
	
	private HashMap<String, String> registerMap = new HashMap<String, String>();
	
	private boolean cmp_le, cmp_lt, cmp_eq, cmp_ne, cmp_ge, cmp_gt;
	
	private int malloc_pointer = 20; // Not zero to avoid errors with null pointers

	private String[] stack = new String[300];
	
	private boolean ended = false;
	
	private String printfString = "";
	private DebuggerWindow window;
	
	public VirtualMachine() {
		for(int i = 0; i < 12; i++) {
			registerMap.put("r"+i, "0");	
			//registerMap.put("s"+i, "0");	
		}
		registerMap.put("pc", "0");
		registerMap.put("fp", "0");
		registerMap.put("lr", "0");
		registerMap.put("sp", "1200");
		registerMap.put(r0, "1");
		for(int i = 0; i < stack.length; i++) {
			stack[i] = "";
		}
	}

	public void execute(HashMap<String, Integer> labels, ArrayList<Command> commands, HashMap<String, String> stringDefinitions) {
		
		System.out.println("\n--- Execution start --- \n");
		
		this.window = new DebuggerWindow();
		
		jump("main", labels, stringDefinitions);
		setReg(fp, getReg(sp));
		while(!ended) {
			boolean jumped = false;
			Command command = commands.get(getInt(pc));
			System.out.println("Executing command: " + command);
			switch(command.type) {
			case add:
				setInt(command.param0, getInt(command.param1) + getInt(command.param2));
				break;
			case b:
				jump(command.param0, labels, stringDefinitions);
				jumped = true;
				break;
			case beq:
				if(cmp_eq) {
					jump(command.param0, labels, stringDefinitions);
					jumped = true;
				}
				break;
			case bl:
				setInt(lr, getInt(pc) + 1);
				jump(command.param0, labels, stringDefinitions);
				jumped = true;
				break;
			case bne:
				if(cmp_ne) {
					jump(command.param0, labels, stringDefinitions);
					jumped = true;
				}
				break;
			case cmp:
				cmp_lt = getInt(command.param0) < getInt(command.param1);
				cmp_le = getInt(command.param0) <= getInt(command.param1);
				cmp_eq = getInt(command.param0) == getInt(command.param1);
				cmp_ne = getInt(command.param0) != getInt(command.param1);
				cmp_ge = getInt(command.param0) >= getInt(command.param1);
				cmp_gt = getInt(command.param0) > getInt(command.param1);
				break;
			case div:
				setInt(command.param0, getInt(command.param1) / getInt(command.param2));
				break;
			case ldr:
				if(command.paramCount == 2) {
					setReg(command.param0, getStack(getInt(command.param1)));
				} else {
					setReg(command.param0, getStack(getInt(command.param1) + getInt(command.param2)));
				}
				break;
			case str:
				if(command.paramCount == 2) {
					setStack(getReg(command.param0), getInt(command.param1));
				} else {
					setStack(getReg(command.param0), getInt(command.param1) + getInt(command.param2));
				}
				break;
			case movne:
				if(cmp_ne) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case movge:
				if(cmp_ge) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case movgt:
				if(cmp_gt) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case movlt:
				if(cmp_lt) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case movle:
				if(cmp_le) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case moveq:
				if(cmp_eq) {
					setReg(command.param0, getReg(command.param1));
				}
				break;
			case mov:
			case movt:
			case movw:
				setReg(command.param0, getReg(command.param1));
				break;
			case mul:
				setInt(command.param0, getInt(command.param1) * getInt(command.param2));
				break;
			case pop:
				setReg(command.param0, getStack(getInt(sp)));
				setInt(sp, getInt(sp)+4);
				jumped = command.param0.equals(pc);
				break;
			case push:
				setInt(sp, getInt(sp)-4);
				setStack(getReg(command.param0), getInt(sp));
				break;
			case sub:
				setInt(command.param0, getInt(command.param1) - getInt(command.param2));
				break;
			case neg:
				setInt(command.param0, getInt(command.param1) * -1);
				break;
			default:
				JOptionPane.showMessageDialog(null, "Unsupported instruction: " + command.type);
				break;
				
			}
			if(!jumped) {
				setInt(pc, getInt(pc)+1); // Move the pc to the next instruction				
			}
			window.updateContents(commands, registerMap, labels, stack, new boolean[]{cmp_le, cmp_lt, cmp_eq, cmp_ne, cmp_ge, cmp_gt});
		}
	}

	private void jump(String label, HashMap<String, Integer> labels, HashMap<String, String> stringDefinitions) {
		if(labels.containsKey(label)) {
			setInt(pc, labels.get(label));
		} else {
			if(label.equals("printf")) {
				String r0Contents = getReg(r0);
				String registerContents = getReg(r1);
				if(r0Contents.startsWith(".STRING")) {
					printfString += stringDefinitions.get(r0Contents);
				} else {
					printfString += registerContents + " ";					
				}
			} else if(label.equals("putchar")) {
				JOptionPane.showMessageDialog(null, "Printf: " + printfString);
				printfString = "";
			} else if(label.equals("malloc")) {
				int allocationSize = getInt(r0);
				setInt(r0, malloc_pointer);
				malloc_pointer+= allocationSize;
			} else if(label.equals("exit")) {
				JOptionPane.showMessageDialog(null, "Execution complete.");
				setInt(pc, getInt(pc)-1);
				window.stepButton.setEnabled(false);
				ended = true;
			} else {
				JOptionPane.showMessageDialog(null, "Cannot jump to label " + label);				
			}
			setInt(pc, getInt(pc)+1); // Emulated function: simply continue at the next instruction
		}
	}

	private String getStack(int address) {
		address /= 4;
		return stack[address];
	}

	private void setStack(String value, int address) {
		address /= 4;
		stack[address] = value;
	}

	private int getInt(String register) {
		String registerValue = getReg(register);
		if(registerValue == null) {
			return asInt(register);
		} else {
			return asInt(registerValue);			
		}
	}
	
	private void setInt(String register, int value) {
		setReg(register, ""+value);
	}
	
	private String getReg(String register) {
		String registerValue = registerMap.get(register);
		if(registerValue == null) {
			return register;
		} else {
			return registerValue;
		}
	}
	
	private void setReg(String register, String value) {
		registerMap.put(register, value);
	}
	
	private int asInt(String string) {
		return Integer.parseInt(string);
	}

}
