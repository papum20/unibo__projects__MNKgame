package player.dbsearch_old;

import java.util.Scanner;

import mnkgame.MNKBoard;
import mnkgame.MNKGameState;



public class DbTester {
	

	public static void main(String[] args) {
		MNKBoard board = new MNKBoard(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));

		DbSearchBoard ia = new DbSearchBoard();
		ia.initPlayer(board.M, board.N, board.K, true, 10);


		Scanner scanner = new Scanner(System.in);
		while(board.gameState() == MNKGameState.OPEN) {
			ia.selectCell(board.getFreeCells(), board.getMarkedCells());
			
			int i = Integer.parseInt(scanner.next());
			int j = Integer.parseInt(scanner.next());
			board.markCell(i, j);
		}
		scanner.close();
		ia.selectCell(board.getFreeCells(), board.getMarkedCells());
	}

}
