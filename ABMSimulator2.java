import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;


public class ABMSimulator2 extends JFrame {


	// UI components.
	private JMenuBar mb = new JMenuBar();
	private JMenu menu_file = new JMenu("FILE");
	private JMenu menu_run = new JMenu("Run");
	private JMenuItem item_open = new JMenuItem("Open");
	private JMenuItem item_close = new JMenuItem("Close");
	private JMenuItem item_compile = new JMenuItem("Compile");

	private JTextArea text_area_source = new JTextArea();
	private JTextArea text_area_result = new JTextArea();
	private JTextArea text_area_source1 = new JTextArea();
	private JTextArea text_area_result1 = new JTextArea();
	private JLabel identification = new JLabel("");
	private JScrollPane text_source, text_result;
	private JScrollPane text_source1, text_result1;

	private ArrayList<String> dataSegment = new ArrayList<>();
	private ArrayList<String> textSegment = new ArrayList<>();

//	private HashMap

	// Simulator cores
	ABMSimulatorCore core1, core2;



	//
//	private HashMap<String, Object> dataSegment = new HashMap<>();

	private class ABMSimulatorCore {
		private JTextArea sourceArea;
		private JTextArea outputArea;

		private ArrayList<String> arr_list = new ArrayList<String>();
		private ArrayList<String> result = new ArrayList<String>();
		private Stack<String> stackstorage = new Stack<String>();
		private HashMap<String, Integer> hmap = new HashMap<>();
		private HashMap<String, Integer> hmap1 = new HashMap<>();

		public ABMSimulatorCore(JTextArea sourceArea, JTextArea outputArea) {
			this.sourceArea = sourceArea;
			this.outputArea = outputArea;
		}

		private void loadAbmFile(File file){
			try {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String st;
				while ((st = br.readLine()) != null) {
					this.sourceArea.append(st + "\n");
					this.arr_list.add(st);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		public void runCommands() {
			outputArea.setText("");
			result.clear();
			boolean par_pass = false;
			boolean par_ret = false;
			boolean use_hmap1 = false;

			int curr = 0;
			while (true) {
				if (curr == arr_list.size())
					break;
				String instruction = arr_list.get(curr).trim();
				if (instruction.length() > 4 && instruction.substring(0, 4).equals("push")) {
					stackstorage.push(instruction.substring(5, instruction.length()));
				}
				if (instruction.length() > 6 && instruction.substring(0, 6).equals("lvalue")) {
					stackstorage.push(instruction.substring(7, instruction.length()));
				}
				if (instruction.length() > 6 && instruction.substring(0, 6).equals("rvalue")) {
					if (use_hmap1 == false && par_ret == false) {
						if(hmap.containsKey(instruction.substring(7, instruction.length())))
							stackstorage.push(Integer.toString(hmap.get(instruction.substring(7, instruction.length()))));
						else
							stackstorage.push("0");
					}
					else if (use_hmap1 == false && par_ret == true) {
						if(hmap1.containsKey(instruction.substring(7, instruction.length())))
							stackstorage.push(Integer.toString(hmap1.get(instruction.substring(7, instruction.length()))));
						else
							stackstorage.push("0");
					}
					else {
						if(hmap1.containsKey(instruction.substring(7, instruction.length())))
							stackstorage.push(Integer.toString(hmap1.get(instruction.substring(7, instruction.length()))));
						else
							stackstorage.push("0");
					}
				}
				if (instruction.equals("pop")) {
					stackstorage.pop();
				}
				if (instruction.equals("copy")) {
					stackstorage.push(stackstorage.peek());
				}
				if (instruction.equals(":=")) {
					int value = Integer.parseInt(stackstorage.pop());
					String key = stackstorage.pop();
					if (par_pass == true || use_hmap1 == true) {
						if(hmap1.containsKey(key)) {
							hmap1.remove(key);
							hmap1.put(key, value);
						}
						else
							hmap1.put(key, value);
					}
					else {
						if(hmap.containsKey(key)) {
							hmap.remove(key);
							hmap.put(key, value);
						}
						else
							hmap.put(key, value);
					}
				}
				if (instruction.length() > 4 && instruction.substring(0, 4).equals("goto")) {
					// Identifier after goto
					String identification = instruction.substring(5, instruction.length());

					// Search label identifier
					for (int j = 0; j < arr_list.size(); j++) {
						String inst = arr_list.get(j).trim();

						if(!inst.contains("label")) continue;

						if(inst.split(" ").length >= 1 && inst.split(" ")[1].equals(identification)){
							curr = j + 1;
							break;
						}

//							Old Code
//							if (inst.length() > 5 && inst.substring(0, 5).equals(identification)) {
//								if (inst.substring(6, inst.length()).equals(identification)) {
//									curr = j + 1;
//									break;
//								}
//							}
					}

					curr ++;
					continue;
				}

				if (instruction.length() > 7 && instruction.substring(0, 7).equals("gofalse")) {
					int value = Integer.parseInt(stackstorage.pop());
					if (value == 0) {
						String identification = instruction.substring(8, instruction.length());
						for (int j = 0; j < arr_list.size(); j++) {
							String inst = arr_list.get(j).trim();

							if(!inst.contains("label")) continue;

							if(inst.split(" ").length >= 1 && inst.split(" ")[1].equals(identification)){
								curr = j + 1;
								break;
							}
						}
					}

					curr ++;
					continue;
				}
				if (instruction.length() > 6 && instruction.substring(0, 6).equals("gotrue")) {
					int value = Integer.parseInt(stackstorage.pop());
					if (value != 0) {
						String identification = instruction.substring(7, instruction.length());
						for (int j = 0; j < arr_list.size(); j++) {
							String inst = arr_list.get(j).trim();

							if(!inst.contains("label")) continue;

							if(inst.split(" ").length >= 1 && inst.split(" ")[1].equals(identification)){
								curr = j + 1;
								break;
							}
						}
					}

					curr ++;
					continue;
				}
				if (instruction.equals("halt")) {
					break;
				}
				if (instruction.equals("show")) {
					result.add("\n");
				}
				if (instruction.length() > 4 && instruction.substring(0, 4).equals("show")) {
					result.add(instruction.substring(5, instruction.length()) + "\n");
				}
				if (instruction.equals("print")) {
					result.add(stackstorage.peek() + "\n");
				}
				if (instruction.length() > 4 && instruction.substring(0, 4).equals("call")) {
					stackstorage.push(Integer.toString(curr));
					String identification = instruction.substring(5, instruction.length());
					for (int j = 0; j < arr_list.size(); j++) {
						String inst = arr_list.get(j);

						if(!inst.contains("label")) continue;

						if(inst.split(" ").length >= 1 && inst.split(" ")[1].equals(identification)){
							curr = j + 1;
							break;
						}
					}

					use_hmap1 = true;
					par_pass = false;
					continue;
				}
				//performs the operation and returns the value
				if (instruction.equals("return")) {
					use_hmap1 = false;
					par_ret = true;
					curr = Integer.parseInt(stackstorage.pop());
				}
				if (instruction.equals("&")) {
					int v1 = Integer.parseInt(stackstorage.pop());
					int v2 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(booleanPrimitiveToInt(v1 != 0 && v2 != 0)));
				}
				if (instruction.equals("|")) {
					int v1 = Integer.parseInt(stackstorage.pop());
					int v2 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(booleanPrimitiveToInt(v1 != 0 || v2 != 0)));
				}
				if (instruction.equals("!")) {
					int value = Integer.parseInt(stackstorage.pop());
					if (value == 0)
						stackstorage.push("0");
					else
						stackstorage.push("1");
				}
				if (instruction.equals("<>")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 == v2)
						stackstorage.push("0");
					else
						stackstorage.push("1");
				}
				if (instruction.equals(">=")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 >= v2)
						stackstorage.push("1");
					else
						stackstorage.push("0");
				}
				if (instruction.equals("<=")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 <= v2)
						stackstorage.push("1");
					else
						stackstorage.push("0");
				}

				if (instruction.equals("<")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 < v2)
						stackstorage.push("1");
					else
						stackstorage.push("0");
				}
				if (instruction.equals(">")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 > v2)
						stackstorage.push("1");
					else
						stackstorage.push("0");
				}
				if (instruction.equals("=")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					if (v1 == v2)
						stackstorage.push("1");
					else
						stackstorage.push("0");
				}
				if (instruction.equals("+")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(v1 + v2));
				}
				if (instruction.equals("-")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(v1 - v2));
				}
				if (instruction.equals("*")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(v1 * v2));
				}
				if (instruction.equals("/")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(v1 / v2));
				}
				if (instruction.equals("div")) {
					int v2 = Integer.parseInt(stackstorage.pop());
					int v1 = Integer.parseInt(stackstorage.pop());
					stackstorage.push(Integer.toString(v1 % v2));
				}
				if (instruction.equals("begin")) {
					par_pass = true;
				}
				if (instruction.equals("end")) {
					par_ret = false;
					hmap1.clear();
				}

				// Version 2
				if(instruction.startsWith(".data")){
					for (int j = 0; j < arr_list.size(); j++) {
						dataSegment.add(arr_list.get(j));
					}
				}

				if(instruction.startsWith(".text")){
					for (int j = 0; j < arr_list.size(); j++) {
						textSegment.add(arr_list.get(j));
					}
				}

				if (instruction.equals(":&")) {
					int value = Integer.parseInt(stackstorage.pop());
					String key = stackstorage.pop();
					if (par_pass == true || use_hmap1 == true) {
						if(hmap1.containsKey(key)) {
							hmap1.remove(key);
							hmap1.put(key, value);
						}
						else
							hmap1.put(key, value);
					}
					else {
						if(hmap.containsKey(key)) {
							hmap.remove(key);
							hmap.put(key, value);
						}
						else
							hmap.put(key, value);
					}
				}

				if(instruction.startsWith(".int")){
					String content = instruction.substring(4);
					String[] contents = content.split(",");

					for(String item : contents){
						stackstorage.push(String.valueOf(Integer.parseInt(item.trim())));
					}
				}

				curr++;
			}
			for (int j = 0; j < result.size(); j++) {
				outputArea.append(result.get(j) + "\n");
			}
		}
	}

	public ABMSimulator2() {
		super("ABM Simulator");

		// Creating the MenuBar and adding components
		mb.add(menu_file);
		mb.add(menu_run);
		menu_file.add(item_open);
		menu_file.add(item_close);
		menu_run.add(item_compile);
		text_source = new JScrollPane(text_area_source, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		text_result = new JScrollPane(text_area_result, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mb.setBounds(0, 0, 600, 30);
		text_source.setBounds(10, 40, 270, 500);
		text_result.setBounds(310, 40, 270, 500);

		text_source1 = new JScrollPane(text_area_source1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		text_result1= new JScrollPane(text_area_result1, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		mb.setBounds(0, 0, 600, 30);
		text_source1.setBounds(610, 40, 270, 500);
		text_result1.setBounds(910, 40, 270, 500);

		// Create two ABMSimulator cores.
		core1 = new ABMSimulatorCore(text_area_source, text_area_result);
		core2 = new ABMSimulatorCore(text_area_source1, text_area_result1);

		item_open.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
				fileChooser.addChoosableFileFilter(new FileNameExtensionFilter("ABM Files", "abm"));

				// Core 1
				int result = fileChooser.showOpenDialog(ABMSimulator2.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();

					core1.loadAbmFile(selectedFile);
				}

				// Core 2
				result = fileChooser.showOpenDialog(ABMSimulator2.this);
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					core2.loadAbmFile(selectedFile);
				}
			}
		});

		item_close.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
			}
		});

		item_compile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				new Thread(() -> {core1.runCommands();}).run();
				new Thread(() -> {core2.runCommands();}).run();
			}
		});
		this.setSize(1200, 600);
		this.getContentPane().add(mb);
		this.getContentPane().add(text_source);
		this.getContentPane().add(text_result);
		this.getContentPane().add(text_source1);
		this.getContentPane().add(text_result1);
		this.getContentPane().add(identification);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	public int booleanPrimitiveToInt(boolean foo) {
		int bar = 0;
		if (foo) {
			bar = 1;
		}
		return bar;
	}

	public static void main(String args[]) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				new ABMSimulator2().setVisible(true);
			}
		});
	}
}