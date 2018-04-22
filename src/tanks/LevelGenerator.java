package tanks;

// kgurazada
public class LevelGenerator {
	public static String generateLevelString() {
		double random = Math.random();
		int walls = (int) (random * 12 + 1);
		String s = "{28,18|";
		boolean[][] cells = new boolean[28][18];
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				cells[i][j] = false;
			}
		}
		for (int i = 0; i < walls; i++) {
			int l = (int) (Math.random() * 15); // max 14
			int x = (int) (Math.random() * (28 - l));
			int y = (int) (Math.random() * (18 - l));
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
		int numTanks = (int) (random * 8 + 1);
		int x = (int) (Math.random() * (28));
		int y = (int) (Math.random() * (18));
		while (cells[x][y]) {
			x = (int) (Math.random() * (28));
			y = (int) (Math.random() * (18));
		}
		for (int i = -2; i <= 2; i++)
			for (int j = -2; j <= 2; j++)
				cells[Math.max(0, Math.min(27, x+i))][Math.max(0, Math.min(17, y+j))] = true;

		s += x + "-" + y + "-" + "player,";
		for (int i = 0; i < numTanks; i++) {
			int type = (int) (Math.random() * 4);
			int angle = (int) (Math.random() * 4);
			x = (int) (Math.random() * (28));
			y = (int) (Math.random() * (18));
			while (cells[x][y]) {
				x = (int) (Math.random() * (28));
				y = (int) (Math.random() * (18));
			}
			for (int a = -1; a <= 1; a++)
				for (int j = -2; j <= 1; j++)
					cells[Math.max(0, Math.min(27, x+a))][Math.max(0, Math.min(17, y+j))] = true;
			
			s += x + "-" + y + "-";
			if (type == 0) {
				s += "brown-" + angle;
			} else if (type == 1) {
				s += "green-" + angle;
			} else if (type == 2) {
				s += "mint-" + angle;
			} else if (type == 3) {
				s += "yellow-" + angle;
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