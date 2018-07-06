package tanks;

// kgurazada
public class LevelGenerator {
	public static String generateLevelString() {
		
		//int type = (int) (Math.random() * 13);
		//test ^
		double size = Game.levelSize;
		
		int height = (int)(18 * size);
		int width = (int)(28 * size);
		double amountWalls = 12 * size * size;
		double amountTanks = 8 * size * size;

		double random = Math.random();
		int walls = (int) (random * amountWalls + 1);
		
		if (Game.insanity)
			walls += amountWalls / 2;
		
		int r = (int)(Math.random() * 50) + 185;
		int g = (int)(Math.random() * 50) + 185;
		int b = (int)(Math.random() * 50) + 185;

		String s = "{" + width + "," + height + "," + r + "," + g + "," + b + ",20,20,20|";
		boolean[][] cells = new boolean[width][height];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = false;
			}
		}
		for (int i = 0; i < walls; i++) {
			int l = (int) (Math.random() * Math.min(height, width) - 3); // max 14
			int x = (int) (Math.random() * (width - l));
			int y = (int) (Math.random() * (height - l));
			boolean o = false;
			double z = Math.random();
			if (z > 0.5) {
				o = true;
			}
			if (o) {
				s += x + "..." + (x + l) + "-" + y;
				for (int j = x; j <= x + l; j++) {
					cells[j][y] = true;
				}
			} else {
				for (int j = y; j <= y + l; j++) {
					cells[x][j] = true;
				}
				s += x + "-" + y + "..." + (y + l);
			}
			if (i == walls - 1) {
				s += "|";
			} else {
				s += ",";
			}
		}
		int numTanks = (int) (random * amountTanks + 1);
		
		if (Game.insanity)
			numTanks = (int) (size * size * (random * 20 + 1) + 40);
		
		int x = (int) (Math.random() * (width));
		int y = (int) (Math.random() * (height));
		while (cells[x][y]) {
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));
		}
		for (int i = -2; i <= 2; i++)
			for (int j = -2; j <= 2; j++)
				cells[Math.max(0, Math.min(width - 1, x+i))][Math.max(0, Math.min(height - 1, y+j))] = true;

		s += x + "-" + y + "-" + "player,";
		for (int i = 0; i < numTanks; i++) {
			int type = (int) (Math.random() * 13);
			int angle = (int) (Math.random() * 4);
			x = (int) (Math.random() * (width));
			y = (int) (Math.random() * (height));
			while (cells[x][y]) {
				x = (int) (Math.random() * (width));
				y = (int) (Math.random() * (height));
			}
			for (int a = -1; a <= 1; a++)
				for (int j = -2; j <= 1; j++)
					cells[Math.max(0, Math.min(width - 1, x+a))][Math.max(0, Math.min(height - 1, y+j))] = true;
			
			s += x + "-" + y + "-";
			if (type == 0) {
				s += "brown-" + angle;
			} else if (type == 1) {
				s += "green-" + angle;
			} else if (type == 2) {
				s += "mint-" + angle;
			} else if (type == 3) {
				s += "yellow-" + angle;
			} else if (type == 4) {
				s += "purple-" + angle;
			} else if (type == 5) {
				s += "magenta-" + angle;
			} else if (type == 6) {
				s += "white-" + angle;
			} else if (type == 7) {
				s += "gray-" + angle;
			} else if (type == 8) {
				s += "black-" + angle;
			} else if (type == 9) {
				s += "red-" + angle;
			} else if (type == 10) {
				s += "orange-" + angle;
			} else if (type == 11) {
				s += "pink-" + angle;
			} else if (type == 12) {
				s += "darkgray-" + angle;
			}
			if (i == numTanks - 1) {
				s += "}";
			} else {
				s += ",";
			}
		}
		return s;
	}
}