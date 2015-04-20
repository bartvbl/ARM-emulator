package core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

public class Main {

	public static void main(String[] args) {
		try {
			setSwingSettings();
			BufferedReader reader = new BufferedReader(new FileReader("res/code.asm"));
			ArrayList<String> lines = new ArrayList<String>();
			while(reader.ready()) {
				lines.add(reader.readLine());
			}
			reader.close();
			parse(lines);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void parse(ArrayList<String> lines) {
		ArrayList<String> filtered = filter(lines);
		parseCommands(filtered);
	}

	private static void parseCommands(ArrayList<String> filtered) {
		HashMap<String, Integer> labels = new HashMap<String, Integer>();
		ArrayList<Command> commands = new ArrayList<Command>();
		HashMap<String, String> stringDefinitions = new HashMap<String, String>();
		
		for(int i = 0; i < filtered.size(); i++) {
			String line = filtered.get(i);
			if(line.startsWith(".STRING")) {
				String[] lineParts = line.split(":");
				String asciiSection = lineParts[1].trim();
				String stringDefinition = asciiSection.substring(8, asciiSection.length()-1);
				stringDefinitions.put(lineParts[0], stringDefinition);
				filtered.remove(i);
				i--; // ensure the string definition isn't shown on the code page. Bit of a hack, but then again this whole thing is.
			} else if(line.endsWith(":")) {
				labels.put(line.substring(0, line.length()-1), commands.size());
			} else {
				line = line.replace('\t', ' ');
				line = line.replace("  ", " ").trim();
				CommandType type = CommandType.valueOf(line.split(" ")[0]);
				Command command = type.parse(line);
				commands.add(command);
			}
		}
		new VirtualMachine().execute(labels, commands, stringDefinitions);
	}

	private static ArrayList<String> filter(ArrayList<String> lines) {
		ArrayList<String> filtered = new ArrayList<String>();
		for(String line : lines) {
			if((!line.startsWith(".") || line.startsWith(".STRING")) && !line.startsWith("#")) {
				filtered.add(line);				
			}
		}
		return filtered;
	}
	
	private static void setSwingSettings() {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (UnsupportedLookAndFeelException e) {
			e.printStackTrace();
		}
	}

}
