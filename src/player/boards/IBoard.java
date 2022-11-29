package player.boards;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import mnkgame.MNKGameState;

public interface IBoard {
	
	public void markCell(int y, int x);
	public MNKCellState cellState(int y, int x);
	public MNKGameState gameState();
	
	public MNKCell getMarkedCell(int i);
	 public MNKCell getFreeCell(int i);
	public int MarkedCells_length();
	public int FreeCells_length();

	public int currentPlayer();

}
