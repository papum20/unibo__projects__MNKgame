package player.boards;

import mnkgame.MNKCellState;
import player.dbsearch_old.structures.Operator;
import player.pnsearch.structures.INodes.MovePair;



public interface IBoardDB extends IBoard {


	public static final MovePair DIRECTIONS[] = {
		new MovePair(-1, 0),
		new MovePair(-1, 1),
		new MovePair(0, 1),
		new MovePair(1, 1),
		new MovePair(1, 0),
		new MovePair(1, -1),
		new MovePair(0, -1),
		new MovePair(-1, -1)
	};

	public boolean isOperatorInCell(int i, int j, short dir, Operator f, MNKCellState player);
	//marks all involved cells (-delete +add) for current player, without changing current player
	public void applyOperator(int i, int j, short dir, Operator f, MNKCellState attacker);
	public void undoOperator(int i, int j, short dir, Operator f, MNKCellState attacker);

}
