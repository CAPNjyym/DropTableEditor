import java.awt.Button;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class DropTableEditor extends JFrame implements ActionListener{
	private static String path = System.getProperty("user.dir") + 
			"\\drop\\";
	
	private static int area, difficulty, sectionID, episode, boxmob, dropIndex[], dropRates[][];
	private static boolean changed[][][][] = new boolean[3][4][10][2];
	private static String dropData[][][][][][], enemyNames[][], itemNames[][], locNames[][];
	private static final String dropperFileName[] = {"box", "mob"},
			dropperName[] = {"Box", "Monster"},
			difficultyName[] = {"Normal", "Hard", "Very Hard", "Ultimate"},
			sectionIDName[] = {"Viridia", "Greenill", "Skyly", "Bluefull",
							"Purplenum", "Pinkal", "Redria", "Oran", "Yellowboze", "Whitill"},
			areaStrings[] = {"Forest", "Caves", "Mines", "Ruins",
							"Temple", "Spaceship", "CCA / CT", "Seabed", "Episode 4",
							"1 Boxes", "2 Boxes", "4 Boxes"};
	
	private static final long serialVersionUID = 1L;
	private JPanel background;
	private JComboBox<String> areaList, difficultyList, sectionIDList, location[], dropItemList[], dropRate[];
	private JLabel pathLabel, locLabel[], rateLabel[], itemLabel[];
	private JTextField pathField;
	private Button save;
	
	public static void main(String[] args){
		dropData = new String[3][4][10][2][][];
		
		enemyNames = getEnemyNames();
		locNames = getLocationNames();
		dropRates = getDropRates();
		itemNames = getItemNames();
		
		populateDropData();
		new DropTableEditor();
	}
	
	public DropTableEditor(){
		background = new JPanel();
		background.setBounds(800, 640, 200, 100);
		background.setLayout(null);
		add(background);
		
		setTitle("Drop Table Editor");
		setSize(800, 720);
		setBackground(Color.BLACK);
		setResizable(false);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Clicking the "X" at the top-right corner closes the window
		
		// JComboBoxes
		areaList = new JComboBox<String>(areaStrings);
		difficultyList = new JComboBox<String>(difficultyName);
		sectionIDList = new JComboBox<String>(sectionIDName);
		areaList.setBounds(5, 5, 90, 30);
		difficultyList.setBounds(100, 5, 90, 30);
		sectionIDList.setBounds(195, 5, 90, 30);
		
		// Path setting stuff
		pathLabel = new JLabel("Path:");
		pathField = new JTextField(path);
		pathLabel.setBounds(290, 5, 50, 30);
		pathField.setBounds(325, 5, 360, 30);
		
		// Save Button
		save = new Button("Save Changes");
		save.setBounds(690, 5, 100, 30);
		
		// Add ActionListeners
		areaList.addActionListener(this);
		difficultyList.addActionListener(this);
		sectionIDList.addActionListener(this);
		save.addActionListener(this);
		
		// Add Everything
		background.add(areaList);
		background.add(difficultyList);
		background.add(sectionIDList);
		background.add(pathLabel);
		background.add(pathField);
		background.add(save);
		
		placeDropObjects();
		
		setVisible(true);
	}
	
	
	@SuppressWarnings("unchecked")
	private void placeDropObjects(){
		int i, j;
		String stars = "";
		
		locLabel = new JLabel[30];
		rateLabel = new JLabel[30];
		itemLabel = new JLabel[30];
		location = new JComboBox[30];
		dropRate = new JComboBox[30];
		dropItemList = new JComboBox[30];
		
		for (i=0; i<30; i++){
			locLabel[i] = new JLabel("Location " + (i+1) + ":");
			rateLabel[i] = new JLabel("Rate:");
			itemLabel[i] = new JLabel("Item ID:");
			location[i] = new JComboBox<String>();
			dropRate[i] = new JComboBox<String>();
			dropItemList[i] = new JComboBox<String>();
			
			for (j=0; j<locNames[0].length; j++)
				location[i].addItem(locNames[0][j]);
			
			dropRate[i].addItem("0 ** (0)");
			for (j=1; j<dropRates.length; j++){
				if (dropRates[j][1] == 0)
					stars = "";
				else if (dropRates[j][1] == 1)
					stars = " *";
				else if (dropRates[j][1] == 2)
					stars = " **";
				
				if (j < 232)
					dropRate[i].addItem("1 / " + dropRates[j][0] + stars + " (" + j + ")");
				else
					dropRate[i].addItem(reduce(dropRates[j][0]) + stars + " (" + j + ")");
			}
			
			for (j=0; j<itemNames.length; j++)
				dropItemList[i].addItem(itemNames[j][1]);
			
			locLabel[i].setBounds(5, 50+21*i, 210, 20);
			location[i].setBounds(80, 50+21*i, 180, 20);
			rateLabel[i].setBounds(285, 50+21*i, 30, 20);
			dropRate[i].setBounds(320, 50+21*i, 135, 20);
			itemLabel[i].setBounds(480, 50+21*i, 50, 20);
			dropItemList[i].setBounds(530, 50+21*i, 255, 20);
			
			background.add(locLabel[i]);
			background.add(location[i]);
			background.add(rateLabel[i]);
			background.add(dropRate[i]);
			background.add(itemLabel[i]);
			background.add(dropItemList[i]);
		}
		
		updateFields();
	}
	
	private String reduce(int val){
		int num = val / 100, denom = val % 100;
		
		while (num % 2 == 0 && denom % 2 == 0){
			num /= 2;
			denom /= 2;
		}
		
		return num + " / " + denom;
	}
	
	
	private void updateFields(){
		int i, j;
		area = areaList.getSelectedIndex();
		difficulty = difficultyList.getSelectedIndex();
		sectionID = sectionIDList.getSelectedIndex();
		
		if (area < 9){ // enemies, so boxmob is 1
			boxmob = 1;
			if (area < 4) // episode 1
				episode = 0;
			else if (area < 8) // episode 2
				episode = 1;
			else
				episode = 2;
		}
		
		if (area < 9) // enemies
			dropIndex = getDropIndex(area);
		else{ // box
			episode = area - 9;
			boxmob = dropData[episode][difficulty][sectionID][0].length;
			dropIndex = new int[boxmob];
			for (i=0; i<boxmob; i++)
				dropIndex[i] = i;
			boxmob = 0;
		}
		
		for (i=0; i<30; i++){
			if (i < dropIndex.length){
				locLabel[i].setVisible(true);
				rateLabel[i].setVisible(true);
				dropRate[i].setVisible(true);
				itemLabel[i].setVisible(true);
				dropItemList[i].setVisible(true);
				
				if (area < 9){ // enemies, so disable location field
					locLabel[i].setText(enemyNames[episode/2][dropIndex[i]]);
					location[i].setVisible(false);
				}
				else{ // boxes, so enable location field
					locLabel[i].setText("Location " + (i+1) + ":");
					location[i].removeAllItems();
					for (j=0; j<locNames[episode].length; j++){
						location[i].addItem(locNames[episode][j]);
					}
					location[i].setVisible(true);
					location[i].setSelectedIndex
						(Integer.parseInt(dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][0]));
				}
				
				dropRate[i].setSelectedIndex
					(Integer.parseInt(dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][1-boxmob]));
				dropItemList[i].setSelectedIndex(findItemIndex(i));
			}
			else{
				locLabel[i].setVisible(false);
				location[i].setVisible(false);
				rateLabel[i].setVisible(false);
				dropRate[i].setVisible(false);
				itemLabel[i].setVisible(false);
				dropItemList[i].setVisible(false);
			}
		}
	}
	
	private int[] getDropIndex(int area){
		int enemies[] = null;
		
		switch (area){
		case 0: enemies = new int[12]; // forest, 12 enemies
			enemies[0] = 9; enemies[1] = 10; enemies[2] = 11; // Booma, Gobooma, Gigobooma
			enemies[3] = 5; enemies[4] = 6; // Rag Rappy, Al Rappy
			enemies[5] = 7; enemies[6] = 8; // Savage Wolf, Barbarous Wolf
			enemies[7] = 3; enemies[8] = 4; // Mothmant, Monest
			enemies[9] = 1; enemies[10] = 2; // Hildebear, Hildeblue
			enemies[11] = 44; // Dragon
			break;
		case 1: enemies = new int[13]; // caves, 13 enemies
			enemies[0] = 16; enemies[1] = 17; enemies[2] = 18; // Evil Shark, Pal Shark, Guil Shark
			enemies[3] = 13; enemies[4] = 14; // Poison Lily, Nar Lily
			enemies[5] = 12; // Grass Assassin
			enemies[6] = 15; // Nano Dragon
			enemies[7] = 19; enemies[8] = 20; // Pofuilly Slime, Pouilly Slime
			enemies[9] = 21; enemies[10] = 22; enemies[11] = 23; // Pan Arms, Migium, Hidoom
			enemies[12] = 45; // De Rol Le / Dal Ral Lie
			break;
		case 2: enemies = new int[8]; // mines, 8 enemies
			enemies[0] = 50; enemies[1] = 24; // Gilchic, Dubchic
			enemies[2] = 28; enemies[3] = 29; // Canadine, Canane
			enemies[4] = 26; enemies[5] = 27; // Sinow Beat, Sinow Gold
			enemies[6] = 25; // Garanz
			enemies[7] = 46; // Vol Opt / Vol Opt ver. 2
			break;
		case 3: enemies = new int[13]; // ruins, 13 enemies
			enemies[0] = 41; enemies[1] = 42; enemies[2] = 43; // Dimenian, La Dimenian, So Dimenian
			enemies[3] = 30; // Delsaber
			enemies[4] = 38; enemies[5] = 39; enemies[6] = 40; // Claw, Bulk, Bulclaw
			enemies[7] = 37; // Dark Belra
			enemies[8] = 31; // Chaos Sorcerer
			enemies[9] = 34; enemies[10] = 35; // Dark Gunner, Death Gunner
			enemies[11] = 36; // Chaos Bringer
			enemies[12] = 47; // Dark Falz
			break;
		case 4: enemies = new int[17]; // temple, 17 enemies
			enemies[0] = 41; enemies[1] = 42; enemies[2] = 43; // Dimenian, La Dimenian, So Dimenian
			enemies[3] = 5; enemies[4] = 51; // Rag Rappy, Love Rappy
			enemies[5] = 13; enemies[6] = 14; // Poison Lily, Nar Lily
			enemies[7] = 3; enemies[8] = 4; // Mothmant, Monest
			enemies[9] = 1; enemies[10] = 2; // Hildebear, Hildeblue
			enemies[11] = 12; // Grass Assassin
			enemies[12] = 37; // Dark Belra
			enemies[13] = 73; // Barba Ray
			enemies[14] = 79; enemies[15] = 80; enemies[16] = 81; // St. Rappy, Halo Rappy, Egg Rappy
			break;
		case 5: enemies = new int[11]; // spaceship, 11 enemies
			enemies[0] = 50; enemies[1] = 24; // Gilchic, Dubchic
			enemies[2] = 7; enemies[3] = 8; // Savage Wolf, Barbarous Wolf
			enemies[4] = 30; // Delsaber
			enemies[5] = 21; enemies[6] = 22; enemies[7] = 23; // Pan Arms, Migium, Hidoom
			enemies[8] = 25; // Garanz
			enemies[9] = 31; // Chaos Sorcerer
			enemies[10] = 76; // Gol Dragon
			break;
		case 6: enemies = new int[16]; // cca / ct, 16 enemies
			enemies[0] = 52; enemies[1] = 53; // Merillia, Meriltas
			enemies[2] = 59; enemies[3] = 60; // Ul Gibbon, Zol Gibbon
			enemies[4] = 54; // Gee
			enemies[5] = 56; enemies[6] = 57; enemies[7] = 58; // Mericarol, Merikle, Mericus
			enemies[8] = 61; // Gibbles
			enemies[9] = 55; // Gi Gue
			enemies[10] = 62; enemies[11] = 63; // Sinow Berill, Sinow Spigell
			enemies[12] = 77; // Gal Gryphon
			enemies[13] = 82; // Ill Gill
			enemies[14] = 83; // Del Lily
			enemies[15] = 84; // Epsilon
			break;
		case 7: enemies = new int[9]; // seabed, 9 enemies
			enemies[0] = 64; enemies[1] = 65; // Dolmolm, Dolmdarl
			enemies[2] = 68; // Recon
			enemies[3] = 69; enemies[4] = 70; // Sinow Zoa, Sinow Zele
			enemies[5] = 66; // Morfos
			enemies[6] = 71; // Deldepth
			enemies[7] = 72; // Delbiter
			enemies[8] = 78; // Olga Flow
			break;
		case 8: enemies = new int[21]; // episode 4, 21 enemies
			enemies[0] = 9; enemies[1] = 10; enemies[2] = 11; // Boota, Ze Boota, Ba Boota
			enemies[3] = 1; // Astark
			enemies[4] = 12; enemies[5] = 13; // Dorphon, Dorphon Eclair
			enemies[6] = 17; enemies[7] = 18; // Sand Rappy, Del Rappy
			enemies[8] = 3; enemies[9] = 2; // Satellite Lizard, Yowie
			enemies[10] = 7; enemies[11] = 8; // Zu, Pazuzu
			enemies[12] = 14; enemies[13] = 16; enemies[14] = 15; // Goran, Pyro Goran, Goran Detonator
			enemies[15] = 4; enemies[16] = 5; // Merissa A, Merissa AA
			enemies[17] = 6; // Girtablulu
			enemies[18] = 19; enemies[19] = 20; enemies[20] = 21; // Saint Million, Shambertin, Kondrieu
			break;
		}
		return enemies;
	}
	
	private int findItemIndex(int i){
		int low = 0, high = itemNames.length - 1, itemIndex = (low + high) / 2;;
		String item = itemNames[itemIndex][0], data = dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][2-boxmob];
		
		while (!item.equals(data) && low <= high){
			if (item.compareTo(data) > 0)
				high = itemIndex - 1;
			else if (item.compareTo(data) < 0)
				low = itemIndex + 1;
			
			itemIndex = (low + high) / 2;
			item = itemNames[itemIndex][0];
		}
		
		if (low <= high)
			return itemIndex;
		return itemNames.length - 1;
	}
	
	
	public void actionPerformed(ActionEvent e){
		checkForChanges();
		
		if (e.getSource() == areaList || e.getSource() == difficultyList || e.getSource() == sectionIDList)
			updateFields();
		else{ // "Save" Button Pressed
			int ep, diff, sID, dropper;
			
			for (ep=1; ep<5; ep*=2)
				for (diff=0; diff<4; diff++)
					for (sID=0; sID<10; sID++)
						for (dropper=0; dropper<2; dropper++)
							if (changed[ep/2][diff][sID][dropper]){
								writeFiles(ep, dropper, diff, sID);
								changed[ep/2][diff][sID][dropper] = false;
							}
		}
	}
	
	private void checkForChanges(){
		int i;
		boolean change = false;
		
		for (i=0; i<dropIndex.length; i++){
			if (area > 8 && !dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][0].equals
					((location[i].getSelectedIndex())+"")){
				change = true;
				dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][0] = (location[i].getSelectedIndex()) + "";
			}
			if (!dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][1-boxmob].equals
					(dropRate[i].getSelectedIndex()+"")){
				change = true;
				dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][1-boxmob] = dropRate[i].getSelectedIndex() + "";
			}
			if (!dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][2-boxmob].equals
					(itemNames[dropItemList[i].getSelectedIndex()][0])){
				change = true;
				dropData[episode][difficulty][sectionID][boxmob][dropIndex[i]][2-boxmob]
						= itemNames[dropItemList[i].getSelectedIndex()][0];
			}
		}
		
		if (change){
			changed[episode][difficulty][sectionID][boxmob] = true;
		}
	}
	
	
	private static void populateDropData(){
		int ep, dropper, diff, sID;
		
		for (ep=1; ep<5; ep*=2)
			for (dropper=0; dropper<2; dropper++)
				for (diff=0; diff<4; diff++)
					for (sID=0; sID<10; sID++)
						dropData[ep/2][diff][sID][dropper] = readFile(ep, diff, sID, dropper);
	}
	
	private static String[][] readFile(int ep, int diff, int sID, int dropper){
		Scanner fileReader;
		String fileName = path + "ep" + ep + "_" + dropperFileName[dropper]
				+ "_" + diff + "_" + sID + ".txt",
				line, lines[] = new String[170], data[][];
		int i, index = 0;
		
		try{
			fileReader = new Scanner(new File(fileName));
			
			while (fileReader.hasNextLine()){
				line = fileReader.nextLine();
				if (line.charAt(0) != '#'){
					lines[index] = line;
					index++;
				}
			}
			fileReader.close();
			
			data = new String[index/(3-dropper)][3-dropper];
			for (i=0; i<index; i++){
				data[i/(3-dropper)][i%(3-dropper)] = lines[i];
			}
			return data;
		}
		catch (FileNotFoundException e){
			System.out.println(fileName + " not found.");
			return null;
		}
	}
	
	
	private static void writeFiles(int ep, int dropper, int diff, int sID){
		String fileName = "ep" + ep + "_" + dropperFileName[dropper]
				+ "_" + diff + "_" + sID + ".txt";
		PrintWriter fileWriter;
		int i;
		
		try{
			fileWriter = new PrintWriter(path + fileName);
			fileWriter.println("# Tethealla Server " + dropperName[dropper] + " Drop File");
			fileWriter.println("# Episode: " + ep + ", Section ID: " +
					sectionIDName[sID] + ", Difficulty: " + difficultyName[diff]);
			fileWriter.println("#");
			fileWriter.println("# File layout:");
			fileWriter.println("#");
			
			switch (dropper){
				case (0): // box
					fileWriter.println("# Three lines per box.");
					fileWriter.println("# First line is area the box is found in.");
					fileWriter.println("# Second line is rare occurance rate.  Valid values from 0 to 255.");
					fileWriter.println("# Third line is hex for actual drop.  Refer to bb_items.txt for valid hex values.");
					break;
				case (1): // mob / Monster
					fileWriter.println("# Two lines per enemy.");
					fileWriter.println("# First line is rare occurance rate.  Valid values from 0 to 255.");
					fileWriter.println("# Second line is hex for actual drop.  Refer to bb_items.txt for valid hex values.");
					break;
			}
			
			// END OF HEADER, BEGIN OF DATA
			
			for (i=0; i<dropData[ep/2][diff][sID][dropper].length; i++){
				fileWriter.println("#");
				
				if (dropper == 1) // mob / Monster
					fileWriter.println("# " + enemyNames[(ep-1)/2][i]);
				fileWriter.println(dropData[ep/2][diff][sID][dropper][i][0]);
				fileWriter.println(dropData[ep/2][diff][sID][dropper][i][1]);
				if (dropper == 0) // box
					fileWriter.println(dropData[ep/2][diff][sID][dropper][i][2]);
			}
			
			fileWriter.close();
			System.out.println("Successfully wrote to " + fileName);
		}
		
		catch (FileNotFoundException e){
			System.out.println("Could not write to " + fileName);
		}
	}

	
	private static String[][] getEnemyNames(){
		String temp[][] = // Episode 1
			{{"Null", "Hildebear / Hildelt", "Hildeblue / Hildetorr", "Mothmant / Mothvert",
			"Monest / Mothvist", "Rag Rappy / El Rappy", "Al Rappy / Pal Rappy", "Savage Wolf / Gulgus",
			"Barbarous Wolf / Gulgus-gue", "Booma / Bartle", "Gobooma / Barble", "Gigobooma / Tollaw",
			"Grass Assassin / Crimson Assassin", "Poison Lily / Ob Lily", "Nar Lily / Mil Lily",
			"Nano Dragon", "Evil Shark / Vulmer", "Pal Shark / Govulmer", "Guil Shark / Melqueek",
			"Pofuilly Slime", "Pouilly Slime", "Pan Arms", "Migium", "Hidoom", "Dubchic / Dubchich",
			"Garanz / Baranz", "Sinow Beat / Sinow Blue", "Sinow Gold / Sinow Red", "Canadine / Canabin",
			"Canane / Canune", "Delsaber", "Chaos Sorcerer / Gran Sorcerer", "Null", "Null", "Dark Gunner",
			"Death Gunner","Chaos Bringer / Dark Bringer", "Dark Belra / Indi Belra", "Claw", "Bulk",
			"Bulclaw", "Dimenian / Arlan", "La Dimenian / Merlan", "So Dimenian / Del-D",
			"Dragon / Sil Dragon", "De Rol Le / Dal Ral Lie", "Vol Opt / Vol Opt ver. 2", "Dark Falz",
			"Null", "Null", "Gilchic / Gilchich",
			// Episode 2
			"Love Rappy", "Merillia", "Meriltas", "Gee", "Gi Gue", "Mericarol", "Merikle", "Mericus",
			"Ul Gibbon", "Zol Gibbon", "Gibbles", "Sinow Berill", "Sinow Spigell", "Dolmolm", "Dolmdarl",
			"Morfos", "Null", "Recon", "Sinow Zoa", "Sinow Zele", "Deldepth", "Delbiter",
			"Barba Ray", "Null", "Null", "Gol Dragon", "Gal Gryphon", "Olga Flow",
			"St. Rappy", "Halo Rappy", "Egg Rappy", "Ill Gill", "Del Lily", "Epsilon"},{"Null",
			// Episode 4
			"Astark", "Yowie", "Satellite Lizard", "Merissa A", "Merissa AA", "Girtablulu", "Zu", "Pazuzu",
			"Boota", "Ze Boota", "Ba Boota", "Dorphon", "Dorphon Eclair", "Goran", "Goran Detonator",
			"Pyro Goran", "Sand Rappy", "Del Rappy", "Saint Million", "Shambertin", "Kondrieu"}};
		
		return temp;
	}
	
	private static String[][] getLocationNames(){
		String temp[][] = // Episode 1
			{{"--- NOWHERE ---", "Forest 1", "Forest 2", "Caves 1", "Caves 2", "Caves 3",
				"Mines 1", "Mines 2", "Ruins 1", "Ruins 2", "Ruins 3",
				"UNKNOWN 1 (11)", "UNKNOWN 2 (12)", "UNKNOWN 3 (13)", "UNKONWN 4 (14)", "UNKONWN 5 (15)"},
			// Episode 2
			{"--- NOWHERE ---", "VR Temple Alpha", "VR Temple Beta", "VR Spaceship Alpha", "VR Spaceship Beta",
				"Jungle Area East", "Jungle Area North",
				"Mountain Area", "CCA / Seaside Area", "Seabed Upper Levels",
				"Seabed Lower / Tower", "UNKNOWN 1 (11)", "UNKNOWN 2 (12)", "UNKNOWN 3 (13)", "UNKONWN 4 (14)", "UNKONWN 5 (15)"},
			// Episode 3
			{"--- NOWHERE ---", "Crater East", "Crater West", "Crater South", "Crater North", "Crater Interior",
				"Subterranean Desert 1", "Subterranean Desert 2", "Subterranean Desert 3", "Boss Room ?",
				"UNKNOWN 0 (10)", "UNKNOWN 1 (11)", "UNKNOWN 2 (12)", "UNKNOWN 3 (13)", "UNKONWN 4 (14)", "UNKONWN 5 (15)"}};
		return temp;
	}
	
	private static int[][] getDropRates(){
		int i, num, denom = Integer.MAX_VALUE + 1, val, temp[][] = new int[256][2];
		
		for (i=0; i<256; i++){
			num = (i % 8) + 7;
			if (i < 232)
				val = -1 * (int)(1.0 * denom / num - 0.5);
			else
				val = 100 * num + -1 * denom;
			
			temp[i][0] = val;
			if (i == 0 || i == 123 || i == 150 || i == 151 || i == 154 || i == 155 || i == 156 || i == 158 ||
				i == 159 || i == 160 || i == 161 || i == 162 || i == 164 || i == 165 || i == 168 || i == 170 ||
				i == 175 || i == 177 || i == 180 || i == 185 || i == 187 || i == 188 || i == 189 || i == 190 ||
				i == 191 || i == 195 || i == 201 || i == 209 || i == 215 || i == 216 || i == 221 || i == 225 ||
				i == 229 || i == 238 || i == 239 || i == 240 || i == 245 || i == 249 || i == 250 || i == 255)
				temp[i][1] = 2;
			else if (i == 1 || i == 2 || i == 3 || i == 5 || i == 6 || i == 7 || i == 14 || i == 15 || i == 16 ||
				i == 17 || i == 19 || i == 33 || i == 50 || i == 59 || i == 70 || i == 172 || i == 174 || i == 179 ||
				i == 181 || i == 182 || i == 184 || i == 186 || i == 193 || i == 199 || i == 200 || i == 202 || i == 243)
				temp[i][1] = 1;
			
			if (i > 38 && num == 14)
				denom /= 2;
		}
		
		return temp;
	}
	
	private static String[][] getItemNames(){
		String[][] temp = {{"000000", "--- NONE ---"}, // Basic Weapons
			// Sabers
			{"000100", "Saber"}, {"000101", "Brand"}, {"000102", "Buster"}, {"000103", "Pallasch"}, {"000104", "Gladius"},
			{"000105", "DB's SABER"}, {"000106", "KALADGOLG"}, {"000107", "DURANDAL"}, {"000108", "GALATINE"},
			// Swords
			{"000200", "Sword"}, {"000201", "Gigush"}, {"000202", "Breaker"}, {"000203", "Claymore"}, {"000204", "Calibur"},
			{"000205", "FLOWEN'S SWORD"}, {"000206", "LAST SURVIVOR"}, {"000207", "DRAGON SLAYER"},
			// Daggers
			{"000300", "Dagger"}, {"000301", "Knife"}, {"000302", "Blade"}, {"000303", "Edge"}, {"000304", "Ripper"},
			{"000305", "BLADE DANCE"}, {"000306", "BLOODY ART"}, {"000307", "CROSS SCAR"}, {"000308", "ZERO DIVIDE"}, {"000309", "TWIN KAMUI*"},
			// Partisans
			{"000400", "Partisan"}, {"000401", "Halbert"}, {"000402", "Glaive"}, {"000403", "Berdys"}, {"000404", "Gungnir"},
			{"000405", "BRIONAC"}, {"000406", "VJAYA"}, {"000407", "GAE BOLG"}, {"000408", "ASTERON BELT"},
			// Slicers
			{"000500", "Slicer"}, {"000501", "Spinner"}, {"000502", "Cutter"}, {"000503", "Sawcer"}, {"000504", "Diska"},
			{"000505", "SLICER OF ASSASSIN"}, {"000506", "DISKA OF LIBERATOR"}, {"000507", "DISKA OF BRAVEMEN"}, {"000508", "IZMAELA"},
			// Handguns
			{"000600", "Handgun"}, {"000601", "Autogun"}, {"000602", "Lockgun"}, {"000603", "Railgun"}, {"000604", "Raygun"},
			{"000605", "VARISTA"}, {"000606", "CUSTOM RAY Ver.00"}, {"000607", "BRAVACE"}, {"000608", "TENSION BLASTER"},
			// Rifles
			{"000700", "Rifle"}, {"000701", "Sniper"}, {"000702", "Blaster"}, {"000703", "Beam"}, {"000704", "Laser"},
			{"000705", "VISK'235W"}, {"000706", "WALS'MK2"}, {"000707", "JUSTY'23ST"}, {"000708", "RIANOV 303SNR*"}, {"000709", "RIANOV 303SNR-1"},
			{"00070A", "RIANOV 303SNR-2"}, {"00070B", "RIANOV 303SNR-3"}, {"00070C", "RIANOV 303SNR-4"}, {"00070D", "RIANOV 303SNR-5"},
			// Mechguns
			{"000800", "Mechgun"}, {"000801", "Assault"}, {"000802", "Repeater"}, {"000803", "Gatling"}, {"000804", "Vulcan"},
			{"000805", "M&A60 VISE"}, {"000806", "H&S25 JUSTICE"}, {"000807", "L&K14 COMBAT"},
			// Shotguns
			{"000900", "Shot"}, {"000901", "Spread"}, {"000902", "Cannon"}, {"000903", "Launcher"}, {"000904", "Arms"},
			{"000905", "CRUSH BULLET"}, {"000906", "METEOR SMASH"}, {"000907", "FINAL IMPACT"},
			// Canes
			{"000A00", "Cane"}, {"000A01", "Stick"}, {"000A02", "Mace"}, {"000A03", "Club"},
			{"000A04", "CLUB OF LACONIUM"}, {"000A05", "MACE OF ADAMAN"}, {"000A06", "CLUB OF ZUMIURAN"}, {"000A07", "LOLIPOP*"},
			// Rods
			{"000B00", "Rod"}, {"000B01", "Pole"}, {"000B02", "Pillar"}, {"000B03", "Striker"},
			{"000B04", "BATTLE VERGE"}, {"000B05", "BRAVE HAMMER"}, {"000B06", "ALIVE AQHU"}, {"000B07", "VALKYRIE*"},
			// Wands
			{"000C00", "Wand"}, {"000C01", "Staff"}, {"000C02", "Baton"}, {"000C03", "Scepter"},
			{"000C04", "FIRE SCEPTER:AGNI"}, {"000C05", "ICE STAFF:DAGON"}, {"000C06", "STORM VAND:INDRA"}, {"000C07", "EARTH WAND BROWNIE"},
			// Claws
			{"000D00", "PHOTON CLAW"}, {"000D01", "SILENCE CLAW"}, {"000D02", "NEI'S CLAW (replica)"}, {"000D03", "PHOENIX CLAW*"},
			// Double Sabers
			{"000E00", "DOUBLE SABER"}, {"000E01", "STAG CUTLERY"}, {"000E02", "TWIN BRAND"},
			// Knuckles
			{"000F00", "BRAVE KNUCKLE"}, {"000F01", "ANGRY FIST"}, {"000F02", "GOD HAND"}, {"000F03", "SONIC KNUCKLE"},
			{"000F04", "Saber (2)"},

			// Advanced Weapons
			{"001000", "OROTIAGITO"}, {"001001", "AGITO (1975)"}, {"001002", "AGITO (1983)"}, {"001003", "AGITO (2001)"}, {"001004", "AGITO (1991)"}, {"001005", "AGITO (1977)"}, {"001006", "AGITO (1980)"}, {"001007", "RAIKIRI"},
			{"001100", "SOUL EATER"}, {"001101", "SOUL BANISH"},
			{"001200", "SPREAD NEEDLE"},
			{"001300", "HOLY RAY"},
			{"001400", "INFERNO BAZOOKA"}, {"001401", "RAMBLING MAY*"}, {"001402", "L&K38 COMBAT"},
			{"001500", "FLAME VISIT"}, {"001501", "BURNING VISIT"},
			{"001600", "AKIKO'S FRYING PAN"},
			{"001700", "SORCERER'S CANE"},
			{"001800", "S-BEAT'S BLADE"},
			{"001900", "P-ARMS'S BLADE"},
			{"001A00", "DELSABER'S BUSTER"},
			{"001B00", "BRINGER'S RIFLE"},
			{"001C00", "EGG BLASTER"},
			{"001D00", "PSYCHO WAND"},
			{"001E00", "HEAVEN PUNISHER"},
			{"001F00", "LAVIS CANNON"},
			{"002000", "VICTOR AXE"}, {"002001", "LACONIUM AXE*"},
			{"002100", "CHAIN SAWD"},
			{"002200", "CADUCEUS"}, {"002201", "MERCURIUS ROD"},
			{"002300", "STING TIP"},
			{"002400", "MAGICAL PIECE"},
			{"002500", "TECHNICAL CROZIER"},
			{"002600", "SUPPRESSED GUN"},
			{"002700", "ANCIENT SABER"},
			{"002800", "HARISEN BATTLE FAN"},
			{"002900", "YAMIGARASU"},
			{"002A00", "AKIKO'S WOK"},
			{"002B00", "TOY HAMMER"},
			{"002C00", "ELYSION"},
			{"002D00", "RED SABER"},
			{"002E00", "METEOR CUDGEL"},
			{"002F00", "MONKEY KING BAR"}, {"002F01", "BLACK KING BAR"},
			{"003000", "DOUBLE CANNON"}, {"003001", "GIRASOLE"},
			{"003100", "HUGE BATTLE FAN"},
			{"003200", "TSUMIKIRI J-SWORD"},
			{"003300", "SEALED J-SWORD"},
			{"003400", "RED SWORD"},
			{"003500", "CRAZY TUNE"},
			{"003600", "TWIN CHAKRAM"},
			{"003700", "WOK OF AKIKO'S SHOP"},
			{"003800", "LAVIS BLADE"},
			{"003900", "RED DAGGER"},
			{"003A00", "MADAM'S PARASOL"},
			{"003B00", "MADAM'S UMBRELLA"},
			{"003C00", "IMPERIAL PICK"},
			{"003D00", "BERDYSH"},
			{"003E00", "RED PARTISAN"},
			{"003F00", "FLIGHT CUTTER"},
			{"004000", "FLIGHT FAN"},
			{"004100", "RED SLICER"},
			{"004200", "HANDGUN:GULD"}, {"004201", "MASTER RAVEN"},
			{"004300", "HANDGUN:MILLA"}, {"004301", "LAST SWAN"},
			{"004400", "RED HANDGUN"},
			{"004500", "FROZEN SHOOTER"}, {"004501", "SNOW QUEEN"},
			{"004600", "ANTI ANDROID RIFLE"},
			{"004700", "ROCKET PUNCH"},
			{"004800", "SAMBA MARACAS"},
			{"004900", "TWIN PSYCHOGUN"},
			{"004A00", "DRILL LAUNCHER"},
			{"004B00", "GULD MILLA"}, {"004B01", "DUAL BIRD*"},
			{"004C00", "RED MECHGUN"},
			{"004D00", "BELRA CANNON"},
			{"004E00", "PANZER FAUST"}, {"004E01", "IRON FAUST"},
			{"004F00", "SUMMIT MOON"},
			{"005000", "WINDMILL"},
			{"005100", "EVIL CURST"},
			{"005200", "FLOWER CANE"},
			{"005300", "HILDEBEAR'S CANE"}, {"005400", "HILDEBLUE'S CANE"},
			{"005500", "RABBIT WAND"},
			{"005600", "PLANTAIN LEAF"}, {"005601", "FATSIA"},
			{"005700", "DEMONIC FORK"},
			{"005800", "STRIKER OF CHAO"}, 
			{"005900", "BROOM"},
			{"005A00", "PROPHETS OF MOTAV"},
			{"005B00", "THE SIGH OF A GOD"},
			{"005C00", "TWINKLE STAR"},
			{"005D00", "PLANTAIN FAN"},
			{"005E00", "TWIN BLAZE"},
			{"005F00", "MARINA'S BAG"},
			{"006000", "DRAGON'S CLAW"},
			{"006100", "PANTHER'S CLAW"},
			{"006200", "S-RED'S BLADE"},
			{"006300", "PLANTAIN HUGE FAN"},
			{"006400", "CHAMELEON SCYTHE"},
			{"006500", "YASMINKOV 3000R"},
			{"006600", "ANO RIFLE"},
			{"006700", "BARANZ LAUNCHER"},
			{"006800", "BRANCH OF PAKUPAKU"},
			{"006900", "HEART OF POUMN"},
			{"006A00", "YASMINKOV 2000H"},
			{"006B00", "YASMINKOV 7000V"},
			{"006C00", "YASMINKOV 9000M"},
			{"006D00", "MASER BEAM"},
			{"006D01", "POWER MASER"},
			{"006E00", "Saber (3)"}, {"006E01", "LOGiN"},
			{"006F00", "FLOWER BOUQUET"},
			
			// Special Weapons
			{"007000", "(S Rank Weapon) SABER  - FA"},
			{"007100", "(S Rank Weapon) SWORD  - FA"},
			{"007200", "(S Rank Weapon) BLADE  - C8"},
			{"007300", "(S Rank Weapon) PARTISAN - C8"},
			{"007400", "(S Rank Weapon) SLICER - 8C"},
			{"007500", "(S Rank Weapon) GUN - FA"},
			{"007600", "(S Rank Weapon) RIFLE - DC"},
			{"007700", "(S Rank Weapon) MECHGUN - 32"},
			{"007800", "(S Rank Weapon) SHOT - 7D"}, 
			{"007900", "(S Rank Weapon) CANE - 78"},
			{"007A00", "(S Rank Weapon) ROD - B4"},
			{"007B00", "(S Rank Weapon) WAND"},
			{"007C00", "(S Rank Weapon) TWIN"},
			{"007D00", "(S Rank Weapon) CLAW"},
			{"007E00", "(S Rank Weapon) BAZOOKA"},
			{"007F00", "(S Rank Weapon) NEEDLE"},
			{"008000", "(S Rank Weapon) SCYTHE"},
			{"008100", "(S Rank Weapon) HAMMER"},
			{"008200", "(S Rank Weapon) MOON"},
			{"008300", "(S Rank Weapon) PSYCHOGUN"},
			{"008400", "(S Rank Weapon) PUNCH"},
			{"008500", "(S Rank Weapon) WINDMILL"},
			{"008600", "(S Rank Weapon) HARISEN"},
			{"008700", "(S Rank Weapon) KATANA"},
			{"008800", "(S Rank Weapon) J-CUTTER"},
			{"008900", "MUSASHI"}, {"008901", "YAMATO"}, {"008902", "ASUKA"}, {"008903", "SANGE & YASHA"},
			{"008A00", "SANGE"}, {"008A01", "YASHA"}, {"008A02", "KAMUI"},
			{"008B00", "PHOTON LAUNCHER"}, {"008B01", "GUILTY LIGHT"}, {"008B02", "RED SCORPIO"}, {"008B03", "PHONON MASER*"},
			{"008C00", "TALIS"}, {"008C01", "MAHU"}, {"008C02", "HITOGATA"}, {"008C03", "DANCING HITOGATA"}, {"008C04", "KUNAI"},
			{"008D00", "NUG-2000 BAZOOKA"},
			{"008E00", "S-BERILL'S HAND #0"}, {"008E01", "S-BERILL'S HAND #1"},
			{"008F00", "FLOWEN'S SWORD AUW 3060"}, {"008F01", "FLOWEN'S SWORD AUW 3064"}, {"008F02", "FLOWEN'S SWORD AUW 3067"}, {"008F03", "FLOWEN'S SWORD AUW 3073"}, {"008F04", "FLOWEN'S SWORD AUW 3077"}, {"008F05", "FLOWEN'S SWORD AUW 3082"}, {"008F06", "FLOWEN'S SWORD AUW 3083"}, {"008F07", "FLOWEN'S SWORD AUW 3084"}, {"008F08", "FLOWEN'S SWORD AUW 3079"},
			{"009000", "DB'S SWORD AUW 3062"}, {"009001", "DB'S SWORD AUW 3067"}, {"009002", "DB'S SWORD AUW 3069 (1)"}, {"009003", "DB'S SWORD AUW 3064"}, {"009004", "DB'S SWORD AUW 3069 (2)"}, {"009005", "DB'S SWORD AUW 3073"}, {"009006", "DB'S SWORD AUW 3070"}, {"009007", "DB'S SWORD AUW 3075"}, {"009008", "DB'S SWORD AUW 3077"},
			{"009100", "GIGUE BAZOOKA"},
			{"009200", "GUARDIANNA"},
			{"009300", "VIRIDIA CARD"}, {"009301", "GREENILL CARD"}, {"009302", "SKYLY CARD"}, {"009303", "BLUEFULL CARD"}, {"009304", "PURPLENUM CARD"},
			{"009305", "PINKAL CARD"}, {"009306", "REDRIA CARD"}, {"009307", "ORAN CARD"}, {"009308", "YELLOWBOZE CARD"}, {"009309", "WHITILL CARD"},
			{"009400", "MORNING GLORY"},
			{"009500", "PARTISAN OF LIGHTING"},
			{"009600", "GAL WIND (weapon)"},
			{"009700", "ZANBA"},
			{"009800", "RIKA'S CLAW"},
			{"009900", "ANGEL HARP"},
			{"009A00", "DEMOLITION COMET"},
			{"009B00", "NEI'S CLAW"},
			{"009C00", "RAINBOW BATON"},
			{"009D00", "DARK FLOW"}, {"009E00", "DARK METEOR"}, {"009F00", "DARK BRIDGE"},
			{"00A000", "G-ASSASIN'S SABERS"}, {"00A100", "RAPPY'S FAN"},
			{"00A200", "BOOMA'S CLAW"}, {"00A201", "GOBOOMA'S CLAW"}, {"00A202", "GIGOBOOMA'S CLAW"},
			{"00A300", "RUBY BULLET"},
			{"00A400", "AMORE ROSE"},
			{"00A500", "(S Rank Weapon) SWORDS"},
			{"00A600", "(S Rank Weapon) LAUNCHER"},
			{"00A700", "(S Rank Weapon) CARD"},
			{"00A800", "(S Rank Weapon) KNUCKLE"},
			{"00A900", "(S Rank Weapon) AXE"},
			{"00AA00", "SLICER OF FANATIC"},
			{"00AB00", "LAME D'ARGENT"}, {"00AC00", "EXCALIBUR"},
			{"00AD00", "Saber (4)"}, {"00AD01", "Saber (5)"}, {"00AD02", "Saber (6)"},
			{"00AD03", "RAGE DE FEU"},
			{"00AE00", "DAISY CHAIN"},
			{"00AF00", "OPHELIE SEIZE"},
			{"00B000", "MILLE MARTEAUX"},
			{"00B100", "LE COGNEUR"},
			{"00B200", "COMMANDER BLADE"},
			{"00B300", "VIVIENNE"},
			{"00B400", "KUSANAGI"},
			{"00B500", "SACRED DUSTER"},
			{"00B600", "GUREN"}, {"00B700", "SHOUREN"}, {"00B800", "JIZAI"},
			{"00B900", "FLAMBERGE"},
			{"00BA00", "YUNCHANG"},
			{"00BB00", "SNAKE SPIRE"},
			{"00BC00", "FLAPJACK FLAPPER"},
			{"00BD00", "GETSUGASAN"},
			{"00BE00", "MAGUWA"},
			{"00BF00", "HEAVEN STRIKER"},
			{"00C000", "CANNON ROUGE"},
			{"00C100", "METEOR ROUGE"},
			{"00C200", "SOLFERINO"},
			{"00C300", "CLIO"},
			{"00C400", "SIREN GLASS HAMMER"},
			{"00C500", "GLIDE DIVINE"},
			{"00C600", "SHICHISHITO"},
			{"00C700", "MURASAME"},
			{"00C800", "DAYLIGHT SCAR"},
			{"00C900", "DECALOG"},
			{"00CA00", "5TH ANNIV. BLADE"},
			{"00CB00", "PRINCIPAL'S GIFT PARASOL"},
			{"00CC00", "AKIKO'S CLEAVER"},
			{"00CD00", "TANEGASHIMA"}, 
			{"00CE00", "TREE CLIPPERS"},
			{"00CF00", "NICE SHOT"},
			{"00D200", "ANO BAZOOKA"},
			{"00D300", "SYNTHESIZER"},
			{"00D400", "BAMBOO SPEAR"},
			{"00D500", "KAN'EI TSUHO"},
			{"00D600", "JITTE"},
			{"00D700", "BUTTERFLY NET"},
			{"00D800", "SYRINGE"},
			{"00D900", "BATTLEDORE"},
			{"00DA00", "RACKET"},
			{"00DB00", "HAMMER"},
			{"00DC00", "GREAT BOUQUET"},
			{"00DD00", "TypeSA/Saber"},
			{"00DE00", "TypeSL/Saber"}, {"00DE01", "TypeSL/Slicer"}, {"00DE02", "TypeSL/Claw"}, {"00DE03", "TypeSL/Katana"},
			{"00DF00", "TypeJS/Saber"}, {"00DF01", "TypeJS/Slicer"}, {"00DF02", "TypeJS/J-Sword"},
			{"00E000", "TypeSW/Sword"}, {"00E001", "TypeSW/Slicer"}, {"00E002", "TypeSW/J-Sword"},
			{"00E100", "TypeRO/Sword"}, {"00E101", "TypeRO/Halbert"}, {"00E102", "TypeRO/Rod"},
			{"00E200", "TypeBL/BLADE"},
			{"00E300", "TypeKN/Blade"}, {"00E301", "TypeKN/Claw"},
			{"00E400", "TypeHA/HALBERT"}, {"00E401", "TypeHA/Rod"},
			{"00E500", "TypeDS/D.SABER"}, {"00E501", "TypeDS/Rod"}, {"00E502", "TypeDS"},
			{"00E600", "TypeCL/CLAW"},
			{"00E700", "TypeSS/SW"},
			{"00E800", "TypeGU/HANDGUN"}, {"00E801", "TypeGU/MECHGUN"},
			{"00E900", "TypeRI/RIFLE"},
			{"00EA00", "TypeME/MECHGUN"},
			{"00EB00", "TypeSH/SHOT"},
			{"00EC00", "TypeWA/WAND"},
			
			// Frames / Armors
			{"010100", "Frame"}, {"010101", "Armor"}, {"010102", "Psy Armor"}, {"010103", "Giga Frame"}, {"010104", "Soul Frame"}, {"010105", "Cross Armor"}, {"010106", "Solid Frame"}, {"010107", "Brave Armor"}, {"010108", "Hyper Frame"}, {"010109", "Grand Armor"}, {"01010A", "Shock Frame"}, {"01010B", "King's Frame"}, {"01010C", "Dragon Frame"}, {"01010D", "Absorb Armor"}, {"01010E", "Protect Frame"}, {"01010F", "General Armor"}, {"010110", "Perfect Frame"}, {"010111", "Valiant Frame"}, {"010112", "Imperial Armor"}, {"010113", "Holiness Armor"}, {"010114", "Guardian Armor"}, {"010115", "Divinity Armor"}, {"010116", "Ultimate Frame"}, {"010117", "Celestial Armor"},
			{"010118", "HUNTER FIELD"}, {"010119", "RANGER FIELD"}, {"01011A", "FORCE FIELD"},
			{"01011B", "REVIVAL GARMENT"}, {"01011C", "SPIRIT GARMENT"},
			{"01011D", "STINK FRAME"},
			{"01011E", "D-PARTS Ver1.01"}, {"01011F", "D-PARTS Ver2.10"},
			{"010120", "PARASITE WEAR:De Rol"}, {"010121", "PARASITE WEAR:Nelgal"}, {"010122", "PARASITE WEAR:Vajulla"},
			{"010123", "SENSE PLATE"}, {"010124", "GRAVITON PLATE"}, {"010125", "ATTRIBUTE PLATE"},
			{"010126", "FLOWEN'S FRAME"},
			{"010127", "CUSTOM FRAME Ver.00"},
			{"010128", "DB's ARMOR"},
			{"010129", "GUARD WAVE"},
			{"01012A", "DF FIELD"},
			{"01012B", "LUMINOUS FIELD"},
			{"01012C", "CHU CHU FEVER"},
			{"01012D", "LOVE HEART"},
			{"01012E", "FLAME GARMENT"},
			{"01012F", "VIRUS ARMOR:Lafuteria"},
			{"010130", "BRIGHTNESS CIRCLE"}, {"010131", "AURA FIELD"},
			{"010132", "ELECTRO FRAME"},
			{"010133", "SACRED CLOTH"},
			{"010134", "SMOKING PLATE"},
			{"010135", "STAR CUIRASS"},
			{"010136", "BLACK HOUND CUIRASS"},
			{"010137", "MORNING PRAYER"},
			{"010138", "BLACK ODOSHI DOMARU"}, {"010139", "RED ODOSHI DOMARU"},
			{"01013A", "BLACK ODOSHI RED NIMAIDOU"}, {"01013B", "BLUE ODOSHI VIOLET NIMAIDOU"},
			{"01013C", "DIRTY LIFE JACKET"},
			{"01013D", "Frame (2)"},
			{"01013E", "WEDDING DRESS"},
			{"01013F", "Frame (3)"},
			{"010140", "RED COAT"},
			{"010141", "THIRTEEN "},
			{"010142", "MOTHER GARB"}, {"010143", "MOTHER GARB+"},
			{"010144", "DRESS PLATE "},
			{"010145", "SWEETHEART"},
			{"010146", "IGNITION CLOAK "}, {"010147", "CONGEAL CLOAK "}, {"010148", "TEMPEST CLOAK "}, {"010149", "CURSED CLOAK "}, {"01014A", "SELECT CLOAK "},
			{"01014B", "SPIRIT CUIRASS "}, {"01014C", "REVIVAL CUIRASS "},
			{"01014D", "ALLIANCE UNIFORM "}, {"01014E", "OFFICER UNIFORM "}, {"01014F", "COMMANDER UNIFORM "},
			{"010150", "CRIMSON COAT"},
			{"010151", "INFANTRY GEAR"}, {"010152", "LIEUTENANT GEAR"}, {"010153", "INFANTRY MANTLE "}, {"010154", "LIEUTENANT MANTLE "},
			{"010155", "UNION FIELD"},
			{"010156", "SAMURAI ARMOR*"},
			{"010157", "STEALTH SUIT*"},
			{"010158", "???? [Requires GOE]"}, {"010159", "Knight/Power [Requires GOE] (1)"}, {"01015A", "Knight/Power [Requires GOE] (2)"},
			
			// Barriers / Shields
			{"010200", "Barrier"}, {"010201", "Shield"}, {"010202", "Core Shield"}, {"010203", "Giga Shield"}, {"010204", "Soul Barrier"}, {"010205", "Hard Shield"}, {"010206", "Brave Barrier"}, {"010207", "Solid Shield"}, {"010208", "Flame Barrier"}, {"010209", "Plasma Barrier"}, {"01020A", "Freeze Barrier"}, {"01020B", "Psychic Barrier"}, {"01020C", "General Shield"}, {"01020D", "Protect Barrier"}, {"01020E", "Glorious Shield"}, {"01020F", "Imperial Barrier"}, {"010210", "Guardian Shield"}, {"010211", "Divinity Barrier"}, {"010212", "Ultimate Shield"}, {"010213", "Spiritual Shield"}, {"010214", "Celestial Shield"},
			{"010215", "INVISIBLE GUARD"}, {"010216", "SACRED GUARD"},
			{"010217", "S-PARTS Ver1.16"}, {"010218", "S-PARTS Ver2.01"},
			{"010219", "LIGHT RELIEF"},
			{"01021A", "SHIELD OF DELSABER"},
			{"01021B", "FORCE WALL"}, {"01021C", "RANGER WALL"}, {"01021D", "HUNTER WALL"},
			{"01021E", "ATTRIBUTE WALL"},
			{"01021F", "SECRET GEAR"}, {"010220", "COMBAT GEAR"},
			{"010221", "PROTO REGENE GEAR"}, {"010222", "REGENERATE GEAR"}, {"010223", "REGENE GEAR ADV."},
			{"010224", "FLOWEN'S SHIELD"},
			{"010225", "CUSTOM BARRIER Ver.00"},
			{"010226", "DB'S SHIELD"},
			{"010227", "RED RING"},
			{"010228", "TRIPOLIC SHIELD"},
			{"010229", "STANDSTILL SHIELD"},
			{"01022A", "SAFETY HEART"},
			{"01022B", "KASAMI BRACER"},
			{"01022C", "GODS SHIELD SUZAKU"}, {"01022D", "GODS SHIELD GENBU"}, {"01022E", "GODS SHIELD BYAKKO"}, {"01022F", "GODS SHIELD SEIRYU"},
			{"010230", "HUNTER'S SHELL"},
			{"010231", "RIKO'S GLASSES*"}, {"010232", "RIKO'S EARRING*"},
			{"010235", "SECURE FEET"},
			{"010238", "Barrier (2)"}, {"010239", "Barrier (3)"},
			{"01023A", "RESTA MERGE"}, {"01023B", "ANTI MERGE"}, {"01023C", "SHIFTA MERGE"}, {"01023D", "DEBAND MERGE"}, {"01023E", "FOIE MERGE"}, {"01023F", "GIFOIE MERGE"}, {"010240", "RAFOIE MERGE"}, {"010241", "RED MERGE"}, {"010242", "BARTA MERGE"}, {"010243", "GIBARTA MERGE"}, {"010244", "RABARTA MERGE"}, {"010245", "BLUE MERGE"}, {"010246", "ZONDE MERGE"}, {"010247", "GIZONDE MERGE"}, {"010248", "RAZONDE MERGE"}, {"010249", "YELLOW MERGE"},
			{"01024A", "RECOVERY BARRIER"}, {"01024B", "ASSIST BARRIER"}, {"01024C", "RED BARRIER"}, {"01024D", "BLUE BARRIER"}, {"01024E", "YELLOW BARRIER"},
			{"01024F", "WEAPONS GOLD SHIELD"},
			{"010250", "BLACK GEAR"},
			{"010251", "WORKS GUARD"},
			{"010252", "RAGOL RING"},
			{"010253", "BLUE RING (7 Colors)"},
			{"010259", "BLUE RING"}, {"01025F", "GREEN RING"}, {"010266", "YELLOW RING"}, {"01026C", "PURPLE RING"}, {"010275", "WHITE RING"}, {"010280", "BLACK RING"},
			{"010283", "WEAPONS SILVER SHIELD"}, {"010284", "WEAPONS COPPER SHIELD"},
			{"010285", "GRATIA"},
			{"010286", "TRIPOLIC REFLECTOR "},
			{"010287", "STRIKER PLUS"},
			{"010288", "REGENERATE GEAR B.P. "},
			{"010289", "RUPIKA "},
			{"01028A", "YATA MIRROR"},
			{"01028B", "BUNNY EARS"},
			{"01028C", "CAT EARS"},
			{"01028D", "THREE SEALS"},
			{"01028F", "DF SHIELD*"},
			{"010290", "FROM THE DEPTHS"},
			{"010291", "DE ROL LE SHIELD*"},
			{"010292", "HONEYCOMB REFLECTOR*"},
			{"010293", "EPSIGUARD"},
			{"010294", "ANGEL RING*"},
			{"010295", "UNION GUARD*"},
			{"010296", "[Blank]*"},
			{"010297", "UNION*"},
			{"010298", "BLACK SHIELD UNION GUARD*"},
			{"010299", "STINK SHIELD*"},
			{"01029A", "BLACK*"},
			//{"01029B00 *Japanese Text* [GENPEI, Heightened, Your ID]"}, {"01029C00 *Japanese Text* [GENPEI, Greenil]"}, {"01029D00 *Japanese Text* [GENPEI, Skyly]"}, {"01029E00 *Japanese Text* [GENPEI, Blueful]"}, {"01029F00 *Japanese Text* [GENPEI, Purplenum]"}, {"0102A000 *Japanese Text* [GENPEI, Pinkal]"}, {"0102A100 *Japanese Text* [GENPEI, Redria]"}, {"0102A200 *Japanese Text* [GENPEI, Oran]"}, {"0102A300 *Japanese Text* [GENPEI, Yellowboze]"}, {"0102A400 *Japanese Text* [GENPEI, Whitill]"},
			//{"0102A500 Frame"}, {"0102A600 Frame"}, {"0102FF00 Frame"},
			
			// Units
			{"010300", "Knight/Power"}, {"010301", "General/Power"}, {"010302", "Ogre/Power"}, {"010303", "God/Power"},
			{"010304", "Priest/Mind"}, {"010305", "General/Mind"}, {"010306", "Angel/Mind"}, {"010307", "God/Mind"},
			{"010308", "Marksman/Arm"}, {"010309", "General/Arm"}, {"01030A", "Elf/Arm"}, {"01030B", "God/Arm"},
			{"01030C", "Thief/Legs"}, {"01030D", "General/Legs"}, {"01030E", "Elf/Legs"}, {"01030F", "God/Legs"},
			{"010310", "Digger/HP"}, {"010311", "General/HP"}, {"010312", "Dragon/HP"}, {"010313", "God/HP"},
			{"010314", "Magician/TP"}, {"010315", "General/TP"}, {"010316", "Angel/TP"}, {"010317", "God/TP"},
			{"010318", "Warrior/Body"}, {"010319", "General/Body"}, {"01031A", "Metal/Body"}, {"01031B", "God/Body"},
			{"01031C", "Angel/Luck"}, {"01031D", "God/Luck"}, {"01031E", "Master/Ability"}, {"01031F", "Hero/Ability"}, {"010320", "God/Ability"},
			{"010321", "Resist/Fire"}, {"010322", "Resist/Flame"}, {"010323", "Resist/Burning"},
			{"010324", "Resist/Cold"}, {"010325", "Resist/Freeze"}, {"010326", "Resist/Blizzard"},
			{"010327", "Resist/Shock"}, {"010328", "Resist/Thunder"}, {"010329", "Resist/Storm"},
			{"01032A", "Resist/Light"}, {"01032B", "Resist/Saint"}, {"01032C", "Resist/Holy"},
			{"01032D", "Resist/Dark"}, {"01032E", "Resist/Evil"}, {"01032F", "Resist/Devil"},
			{"010330", "All/Resist"}, {"010331", "Super/Resist"}, {"010332", "Perfect/Resist"},
			{"010333", "HP/Restorate"}, {"010334", "HP/Generate"}, {"010335", "HP/Revival"},
			{"010336", "TP/Restorate"}, {"010337", "TP/Generate"}, {"010338", "TP/Revival"},
			{"010339", "PB/Amplifier"}, {"01033A", "PB/Generate"}, {"01033B", "PB/Create"},
			{"01033C", "Wizard/Technique"}, {"01033D", "Devil/Technique"}, {"01033E", "God/Technique"},
			{"01033F", "General/Battle"}, {"010340", "Devil/Battle"}, {"010341", "God/Battle"},
			{"010342", "Cure/Poison"}, {"010343", "Cure/Paralysis"}, {"010344", "Cure/Slow"}, {"010345", "Cure/Confuse"}, {"010346", "Cure/Freeze"}, {"010347", "Cure/Shock"},
			{"010348", "Yasakani Magatama"},
			{"010349", "V101"}, {"01034A", "V501"}, {"01034B", "V502"}, {"01034C", "V801"},
			{"01034D", "LIMITER"}, {"01034E", "ADEPT"},
			{"01034F", "SWORDSMAN LORE"}, {"010350", "PROOF OF SWORD-SAINT"},
			{"010351", "SMARTLINK"},
			{"010352", "DIVINE PROTECTION"},
			{"010353", "Heavenly/Battle"}, {"010354", "Heavenly/Power"}, {"010355", "Heavenly/Mind"}, {"010356", "Heavenly/Arms*"}, {"010357", "Heavenly/Legs"}, {"010358", "Heavenly/Body"}, {"010359", "Heavenly/Luck"},
			{"01035A", "Heavenly/Ability"}, {"01035B", "Centurion/Ability"},
			{"01035C", "Friend Ring"},
			{"01035D", "Heavenly/HP"}, {"01035E", "Heavenly/TP"}, {"01035F", "Heavenly/Resist"}, {"010360", "Heavenly/Technique"},
			{"010361", "HP/Resurrection"}, {"010362", "TP/Resurrection"}, {"010363", "PB/Increase"},
			{"010364", "???? (1)"},
			{"010365", "Mag (Pal Rappy) (1)"}, {"010366", "Mag (Pal Rappy) (2)"}, {"010367", "Mag (Pal Rappy) (3)"}, {"010368", "Mag (Pal Rappy) (4)"},
			
			// THAT ???? item in VIRIDIA NORMAL BOX ITEM 1 and BOX ITEM 2
			{"020000", "???? (2)"},
			
			// Items (Green Boxes)
			{"030000", "Monomate"}, {"030001", "Dimate"}, {"030002", "Trimate"},
			{"030100", "Monofluid"}, {"030101", "Difluid"}, {"030102", "Trifluid"},
			{"030200", "Disk:Foie Lv.1"},
			{"030300", "Sol Atomizer"}, {"030400", "Moon Atomizer"}, {"030500", "Star Atomizer"},
			{"030600", "Antidote"}, {"030601", "Antiparalysis"},
			{"030602", "???? (x1-x255)"},
			{"030700", "Telepipe"},
			{"030800", "Trap Vision"},
			{"030900", "Scape Doll"},
			{"030A00", "Monogrinder"}, {"030A01", "Digrinder"}, {"030A02", "Trigrinder"},
			{"030A03", "???? (3)"},
			{"030B00", "Power Material"}, {"030B01", "Mind Material"}, {"030B02", "Evade Material"}, {"030B03", "HP Material"}, {"030B04", "TP Material"}, {"030B05", "Def Material"}, {"030B06", "Luck Material"}, {"030B07", "???? (4)"},
			{"030C00", "Cell Of MAG 502"}, {"030C01", "Cell Of MAG 213"}, {"030C02", "Parts Of RoboChao"}, {"030C03", "Heart Of Opa Opa"}, {"030C04", "Heart Of Pian"}, {"030C05", "Heart Of Chao"}, {"030C06", "???? (5)"},
			{"030D00", "Sorcerer's Right Arm"}, {"030D01", "S-beat's Arms"}, {"030D02", "P-arm's Arms"}, {"030D03", "Delsaber's Right Arm"}, {"030D04", "C-bringer's Right Arm"}, {"030D05", "Delsaber's Left Arm"}, {"030D06", "S-red's Arms"}, {"030D07", "Dragon's Claw"}, {"030D08", "Hildebear's Head"}, {"030D09", "Hildeblue's Head"}, {"030D0A", "Parts of Baranz"}, {"030D0B", "Belra's Right Arm"}, {"030D0C", "GIGUE'S ARMS"}, {"030D0D", "S-BERILL'S ARMS"}, {"030D0E", "G-ASSASIN'S ARMS"}, {"030D0F", "BOOMA'S RIGHT ARMS"}, {"030D10", "GOBOOMA'S RIGHT ARMS"}, {"030D11", "GIGOBOOMA'S RIGHT ARMS"}, {"030D12", "GAL WIND (enemy part)"}, {"030D13", "RAPPY'S WING"}, {"030D14", "Cladding of Epsilon"}, {"030D15", "De Rol Le Shell"}, {"030D16", "???? (6)"},
			{"030E00", "BERILL PHOTON"}, {"030E01", "PARASITIC GENE FLOW"}, {"030E02", "MAGICSTONE IRITISTA"}, {"030E03", "BLUE BLACK STONE"}, {"030E04", "SYNCESTA"}, {"030E05", "MAGIC WATER"}, {"030E06", "PARASITIC CELL TYPE D"}, {"030E07", "MAGIC ROCK HEART KEY"}, {"030E08", "MAGIC ROCK MOOLA"}, {"030E09", "STAR AMPLIFIER"}, {"030E0A", "BOOK OF HITOGATA"}, {"030E0B", "HEART OF CHU CHU"}, {"030E0C", "PARTS OF EGG BLASTER"},
			{"030E0D", "HEART OF ANGEL"}, {"030E0E", "HEART OF DEVIL"}, {"030E0F", "KIT OF HAMBERGER"}, {"030E10", "PANTHER'S SPIRIT"}, {"030E11", "KIT OF MARK3"}, {"030E12", "KIT OF MASTER SYSTEM"}, {"030E13", "KIT OF GENESIS"}, {"030E14", "KIT OF SEGA SATURN"}, {"030E15", "KIT OF DREAMCAST"},
			{"030E16", "AMP. RESTA"}, {"030E17", "AMP. ANTI"}, {"030E18", "AMP. SHIFTA"}, {"030E19", "AMP. DEBAND"}, {"030E1A", "Amplifier of Foie"}, {"030E1B", "AMP. (1)"}, {"030E1C", "AMP. (2)"}, {"030E1D", "AMP. (3)"}, {"030E1E", "AMP. (4)"}, {"030E1F", "AMP. (5)"}, {"030E20", "AMP. (6)"}, {"030E21", "AMP. (7)"}, {"030E22", "AMP. (8)"}, {"030E23", "AMP. (9)"}, {"030E24", "AMP. (10)"}, {"030E25", "AMP. YELLOW"},
			{"030E26", "HEART OF KAPUKAPU"},
			{"030E27", "PHOTON BOOSTER"},
			{"030E28", "???? (7)"},
			{"030F00", "Addslot"},
			{"030F01", "???? (8)"},
			{"031000", "PHOTON DROP"}, {"031001", "PHOTON SPHERE"}, {"031002", "PHOTON CRYSTAL"},
			{"031003", "Secret Lottery Ticket"},
			{"031004", "(Blank Rare) x1"}, {"031005", "???? x1"},
			{"031100", "BOOK OF KATANA 1"}, {"031101", "BOOK OF KATANA 2"}, {"031102", "BOOK OF KATANA 3"}, {"031103", "???? (9)"},
			{"031200", "WEAPONS BRONZE BADGE"}, {"031201", "WEAPONS SILVER BADGE"}, {"031202", "WEAPONS GOLD BADGE"}, {"031203", "WEAPONS CRYSTAL BADGE"}, {"031204", "WEAPONS STEEL BADGE"}, {"031205", "WEAPONS ALUMINUM BADGE"}, {"031206", "WEAPONS LEATHER BADGE"}, {"031207", "WEAPONS BONE BADGE"},
			{"031208", "FoieLV.1Disk: (1)"}, {"031209", "(blank) [Unknown item]"},
			{"03120A", "VALENTINE'S CHOCOLATE"},
			{"03120B", "FoieLV.1Disk: (2)"}, {"03120C", "FoieLV.1Disk: (3)"}, {"03120D", "FoieLV.1Disk: (4)"}, {"03120E", "FoieLV.1Disk: (5)"}, {"03120F", "FoieLV.1Disk: (6)"},
			{"031210", "Flower Bouquet"}, {"031211", "Cake"}, {"031212", "Accessories"}, {"031213", "Mr.Naka's Business Card"},
			{"031214", "???? (10)"},
			{"031300", "PRESENT"}, {"031400", "CHOCOLATE"}, {"031401", "CANDY"}, {"031402", "CAKE"},
			{"031403", "SILVER BADGE"}, {"031404", "GOLD BADGE"}, {"031405", "CRYSTAL BADGE"}, {"031406", "IRON BADGE"}, {"031407", "ALUMINUM BADGE"}, {"031408", "LEATHER BADGE"}, {"031409", "BONE BADGE"},
			{"03140A", "FoieLV.1Disk: (7)"}, {"03140B", "FoieLV.1Disk: (8)"},
			{"031500", "Christmas Present"}, {"031501", "Easter Egg"}, {"031502", "Jack-O-Laturn"}, {"031503", "???? (11)"},
			{"031600", "DISK VOL.1"}, {"031601", "DISK VOL.2"}, {"031602", "DISK VOL.3"}, {"031603", "Disk: Foie Lv.1 (1)"}, {"031604", "Disk: Foie LV.1 (2)"}, {"031605", "DISK VOL.6"}, {"031606", "DISK VOL.7"}, {"031607", "DISK VOL.8"}, {"031608", "DISK VOL.9"}, {"031609", "DISK VOL.10"}, {"03160A", "DISK VOL.11"}, {"03160B", "DISK VOL.12"}, {"03160C", "???? (12)"},
			{"031700", "HUNTERS REPORT"}, {"031701", "HUNTERS REPORT RANK A"}, {"031702", "HUNTERS REPORT RANK B"}, {"031703", "HUNTERS REPORT RANK C"}, {"031704", "HUNTERS REPORT RANK F"}, {"031705", "???? (13)"},
			{"031800", "Tablet"}, {"031801", "Disk: Foie Lv.1"}, {"031802", "Dragon Scale"}, {"031803", "Heaven Striker Coat"}, {"031804", "Pioneer Parts"}, {"031805", "Amitie's Memo"}, {"031806", "Heart of Morolian"}, {"031807", "Rappy's Beak"}, {"031808", "FoieLV.1Disk:"}, {"031809", "D-Photon Core"}, {"03180A", "Liberta Kit"}, {"03180B", "Cell of MAG 0503"}, {"03180C", "Cell of MAG 0504"}, {"03180D", "Cell of MAG 0505"}, {"03180F", "Cell of MAG 0507"},
			{"031810", "???? (14)"}, {"031811", "???? (15)"}, {"031812", "???? (16)"},
			{"031900", "Team Points 500"}, {"031901", "Team Points 1000"}, {"031902", "Team Points 5000"}, {"031903", "Team Points 10000"}, {"031904", "???? (17)"},
			{"032000", "???? (18)"}};
		
		return temp;
	}
}
