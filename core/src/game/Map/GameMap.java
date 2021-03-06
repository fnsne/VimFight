/**
 *
 */
package game.Map;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import game.Object.Creature;
import game.Object.Item;
import game.Object.Player;
import game.Object.Trap;
import game.Object.Item.TYPE;


/**
 * @author 楊舜宇
 * @since 2016/6/15
 *
 */
public class GameMap {
	private int lineCount;
	private ArrayList<MapRow> rows;
	public int lineno;
	public int colno;
	public int screenStartRow;
	public int screenStartCol;
	private int trapsUpLimit = 10000;

	public GameMap(ArrayList<MapRow> mainRows, Integer startRow, Integer startCol) {
		rows = new ArrayList<MapRow>(500);
		for(int row = startRow; row < startRow + 20 && row < mainRows.size(); ++row) {
			rows.add(mainRows.get(row).getScreenRow(startCol));
		}
		rows.trimToSize();
	}

	public GameMap(BufferedReader mapReader) {
		rows = new ArrayList<MapRow>(500);
		try {
			Integer lineno = 1;
			while(mapReader.ready()) {
				String line = mapReader.readLine();
				MapRow row = new MapRow(line, lineno++);
				rows.add(row);
			}
			mapReader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			System.err.println("read text map failed");
		} catch (IOException e) {
			e.printStackTrace();
			System.err.println("read text map io problem");
		}
		rows.trimToSize();
		screenStartRow = 0;
		screenStartCol = 0;
	}

	public void spreadTraps(int amount, boolean isGlobal){
		Random random = new Random();
		int rowNum, colNum;
		for(int i = 0 ; i < amount; i++){
			if(isGlobal) {
				rowNum = random.nextInt(rows.size());
				colNum = random.nextInt(rows.get(rowNum).getLineString().length());
			}
			else {
				rowNum = random.nextInt(20) + screenStartRow;
				if(rowNum >= rows.size()) rowNum = random.nextInt(rows.size());
				int lineLength = rows.get(rowNum).getLineString().length();
				colNum = random.nextInt(20) + screenStartCol;
				if(colNum >= lineLength) colNum = random.nextInt(lineLength);
			}
			rows.get(rowNum).getSquare(colNum).addItem();
		}
	}

	public void spreadTonic(int amount, boolean isGlobal) {
		int addAmount = 0;
		int rowNum, colNum;
		Random random = new Random();
		while(addAmount != amount){
			if(isGlobal) {
				rowNum = random.nextInt(rows.size());
				colNum = random.nextInt(rows.get(rowNum).getLineString().length());
			}
			else {
				rowNum = random.nextInt(20) + screenStartRow;
				if(rowNum >= rows.size()) rowNum = random.nextInt(rows.size());
				int lineLength = rows.get(rowNum).getLineString().length();
				colNum = random.nextInt(20) + screenStartCol;
				if(colNum >= lineLength) colNum = random.nextInt(lineLength);
			}
			//if(!rows.get(rowNum).getSquare(colNum).hasItem())
			rows.get(rowNum).getSquare(colNum).addTonic();
			++addAmount;
		}
	}

	private GameMap cutScreenMap(int startRow, int startCol) {
		GameMap screenMap = new GameMap(rows, startRow, startCol);
		screenMap.lineno = startRow;
		screenMap.colno = startCol;
		return screenMap;
	}

	public Integer getLineCount() {
		return lineCount;
	}

	private ArrayList<MapRow> getMapRows() {
		return rows;
	}

	public ArrayList<MapRow> getMapScreenRows() {
		GameMap screenMap = cutScreenMap(screenStartRow, screenStartCol);
		ArrayList<MapRow> screenRows = screenMap.getMapRows();
		return screenRows;
	}

	public boolean collision(Creature creature) {
		if(creature instanceof Player) {
			Player player = (Player)creature;
			int row_index = player.pos.y;
			int col_index = player.pos.x;
			MapRow row = rows.get(row_index);
			MapSquare square = row.getSquare(col_index);
			if(square.getItemType() == Item.TYPE.NONE) return false;
			else {
				square.touch(player);
			}
		}
		return true;
	}

	public int isCollision(Player player) {
		int row_index = player.pos.y;
		int col_index = player.pos.x;
		MapRow row = rows.get(row_index);
		MapSquare square = row.getSquare(col_index);
		if(square.getItemType() == Item.TYPE.NONE) return 0;
		else if(square.getItemType() == TYPE.HP || square.getItemType() == TYPE.MP)
			return 1;
		else return 2;
	}

	public void updateScreenMap(Position pos) {
  int nextScreenCol = screenStartCol;
  int nextScreenRow = screenStartRow;
  if(pos.x > screenStartCol + 18) {
     nextScreenCol = pos.x - 1;
  		}
   else if(pos.x < screenStartCol) {
       nextScreenCol = pos.x - 19;
        }
   if(pos.y > screenStartRow + 19) {
       nextScreenRow = pos.y - 1;
        }
   else if(pos.y < screenStartRow) {
       nextScreenRow = pos.y - 19;
        }
   if(nextScreenCol < 0) nextScreenCol = 0;
   if(nextScreenRow < 0) nextScreenRow = 0;
   screenStartCol = nextScreenCol;
   screenStartRow = nextScreenRow;
    }

	public void moveRight(Creature creature) {
		int x = creature.getCol();
		int y = creature.getRow();
		MapRow row = rows.get(y);
		String line = row.getLineString();
		if(x + 1 < line.length()) {
			if(creature instanceof Player) {
				if(x + 1 >= screenStartCol + 19) {
					screenStartCol++;
				}
			}
			creature.setCol(x + 1);
		}
	}

	public void moveLeft(Creature creature) {
		int x = creature.getCol();
		if(x - 1 >= 0) {
			if(creature instanceof Player) {
				if(x - 1 <= screenStartCol) {
					screenStartCol--;
				}
			}
			creature.setCol(x - 1);
		}
	}

	public void moveUp(Creature creature) {
		int y = creature.getRow();
		int x = creature.getCol();
		if(y - 1 >= 0) {
			if(creature instanceof Player) {
				if(y - 1 <= screenStartRow) {
					screenStartRow--;
				}
			}
			creature.setRow(y - 1);
			MapRow row = rows.get(y - 1);
			if(x >= row.getLineString().length()) {
				x = row.getLineString().length() - 1;
				x = (x < 0)? 0: x;
				creature.setCol(x);
			}
		}
	}


	public void moveDown(Creature creature) {
		int y = creature.getRow();
		int x = creature.getCol();
		if(y + 1 < rows.size()) {
			if(y + 1 >= screenStartRow + 19) {
				screenStartRow++;
			}
			creature.setRow(y + 1);
			MapRow row = rows.get(y + 1);
			if(x >= row.getLineString().length()) {
				x = row.getLineString().length() - 1;
				x = (x < 0)? 0: x;
				creature.setCol(x);
			}
		}
	}


	public void moveNextWord(Creature creature) {
		int x = creature.getCol();
		int y = creature.getRow();
   int startLineCol = x;
   int nextPosX = x;
   int nextPosY = y;
   Character curChar = rows.get(y).getLineString().charAt(x);
   boolean isBreakPoint = false;

   int curType;
   if(curChar.toString().matches("\\s")) curType = 0;
   else if(curChar.toString().matches("\\w")) curType = 1;
   else curType = 2;

   for(int rowNum = y; rowNum < rows.size(); ++rowNum) {
       MapRow row = rows.get(rowNum);
       String line = row.getLineString();
       boolean isFound = false;
       for (int i = startLineCol; i < line.length(); ++i) {
           Character c = line.charAt(i);
           if (c.toString().matches("\\s")) {
               isBreakPoint = true;
               continue;
           } else if (c.toString().matches("\\w") && c != '_' && curType != 1 || isBreakPoint) {
               isFound = true;
               nextPosX = i;
               nextPosY = rowNum;
               break;
           } else if (c.toString().matches("\\p{Punct}") && curChar != c && curType != 2 || isBreakPoint) {
               isFound = true;
               nextPosX = i;
               nextPosY = rowNum;
               break;
                			}
            	}
      if(isFound) {
          break;
            	}
      startLineCol = 0;
        }
   creature.setCol(nextPosX);
   creature.setRow(nextPosY);
	}


	public void movePreWord(Creature creature) {
		int x = creature.getCol();
		int y = creature.getRow();
   int startLineCol = x;
   int nextPosX = x;
   int nextPosY = y;
   Character curChar = rows.get(y).getLineString().charAt(x);
   boolean isBreakPoint = false;

   int curType;
   if(curChar.toString().matches("\\s")) curType = 0;
   else if(curChar.toString().matches("\\w") && curChar != '_') curType = 1;
   else curType = 2;

   for(int rowNum = y; rowNum >= 0; --rowNum) {
       MapRow row = rows.get(rowNum);
       String line = row.getLineString();
       boolean isFound = false;
       for (int i = startLineCol; i >= 0; --i) {
           Character c = line.charAt(i);
           if (c.toString().matches("\\s")) {
               isBreakPoint = true;
               continue;
           } else if (c.toString().matches("\\w") && c != '_' && (curType != 1 || isBreakPoint)) {
               isFound = true;
               nextPosX = i;
               nextPosY = rowNum;
               curType = 1;
               break;
           } else if (c.toString().matches("\\p{Punct}") && (curChar != c || curType != 2 || isBreakPoint)) {
               isFound = true;
               nextPosX = i;
               nextPosY = rowNum;
               curType = 2;
               break;
                			}
            		}
       if(isFound) {
           int startWordIndex = nextPosX;
           for(int i = nextPosX; i >= 0; --i) {
               Character c = line.charAt(i);
               if (c.toString().matches("\\s")) {
                   startWordIndex = i + 1;
                   break;
               } else if (c.toString().matches("\\w") && c != '_' && curType != 1) {
                   startWordIndex = i + 1;
                   break;
               } else if (c.toString().matches("\\p{Punct}")  && curType != 2) {
                   startWordIndex = i + 1;
                   break;
                    					}
               if(i == 0) nextPosX = 0;
                			}
           nextPosX = startWordIndex;
           break;
            	}
      startLineCol = (rowNum != 0)? rows.get(rowNum - 1).getLineString().length() - 1: rows.get(0).getLineString().length();
        }
   creature.setCol(nextPosX);
   creature.setRow(nextPosY);
    }


	public void moveLineBegin(Position pos) {
		pos.x = 0;
	}


	public void moveLineEnd(Position pos) {
		String line = rows.get(pos.y).getLineString();
		pos.x = (line.length() > 0)? line.length() - 1: 0;
	}


	public boolean moveFindChar(Position pos, Character c) {
		String line = rows.get(pos.y).getLineString();
		int nextX = -1;
		if(pos.x != line.length() - 1) {
			nextX = line.indexOf(c, pos.x + 1);
		}
		if(nextX >= 0) {
			pos.x = nextX;
			return true;
		}
		return false;
	}


	public boolean moveFindPreChar(Position pos, Character c) {
		String line = rows.get(pos.y).getLineString();
		int nextX = -1;
		if(pos.x != 0) {
			nextX = line.lastIndexOf(c, pos.x - 1);
		}
		if(nextX >= 0) {
			pos.x = nextX;
			return true;
		}
		return false;
	}

	public boolean moveLine(Creature creature, int lineno) {
		int y = creature.getRow();
		int x = creature.getCol();
		if(lineno != y && lineno < rows.size()) {
			creature.setRow(lineno);
			MapRow row = rows.get(lineno);
			if(x >= row.getLineString().length()) {
				x = row.getLineString().length() - 1;
				x = (x < 0)? 0: x;
				creature.setCol(x);
			}
			return true;
		}
		return false;
	}

	public void deleteLineTrap(Player player) {
		MapRow row = rows.get(player.pos.y);
		for(MapSquare square: row) {
			if(square.item instanceof Trap) {
				square.item = null;
			}
		}
	}
}
