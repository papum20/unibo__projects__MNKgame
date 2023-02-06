package player.dbsearch;

import java.util.Scanner;

import mnkgame.MNKCellState;
import mnkgame.MNKGameState;
import mnkgame.MNKPlayer;
import player.dbsearch.BiList.BiNode;
import player.dbsearch.Operators.AlignmentsMap;
import player.pnsearch.MovePair;



public class BoardTest {


	public static void printBoard(DbBoard board) {
		for(int i = 0; i < board.M; i++) {
			String row = "";
			for(int j = 0; j < board.N; j++) 
				row += mnk2char(board.cellState(i, j));
			System.out.println(row);
		}
	}
	
	public static char mnk2char(MNKCellState c) {
		if(c == MNKCellState.P1) return 'X';
		else if(c == MNKCellState.P2) return 'O';
		else return '.';
	}

	public static void debugBoard(DbBoard board, boolean print_mc, boolean print_fc, boolean print_cells) {
		String sep_line  = "";
		for(int i = 0; i < 20; i++) sep_line += "-";
		System.out.println(sep_line);
		System.out.println("Turn " + board.MC_n);
		printBoard(board);

		System.out.println("\n");
		String mc_string = "", fc_string = "";
		if(print_mc) for(int i = 0; i < board.MC_n; i++) mc_string += board.MC[i];
		if(print_fc) for(int i = 0; i < board.FC_n; i++) fc_string += board.FC[i];
		//System.out.println("MC: " + mc_string);
		//System.out.println("FC: " + fc_string);
		if(print_cells) {
			System.out.println("Alignments per cell - P1:");
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P1);
					if(p != null) {
						System.out.println("\tcell: " + new MovePair(i, j));
						do {
							System.out.println("\t\t" + p.item.item);
						} while((p = p.next) != null);
					}
				}
			}
			System.out.println("Alignments per cell - P2:");
			for(int i = 0; i < board.M; i++) {
				for(int j = 0; j < board.N; j++) {
					BiNode<BiNode<OperatorPosition>> p = board.cells_lines[i][j].getFirst(MNKCellState.P2);
					if(p != null) {
						System.out.println("\tcell: " + new MovePair(i, j));
						do {
							System.out.println("\t\t" + p.item.item);
						} while((p = p.next) != null);
					}
				}
			}
		}
		System.out.println("Alignments per line - P1:");
		for(int d = 0; d < board.lines_dirs.length; d++) {
			System.out.println("\tDIRECTION: " + DbBoard.DIRECTIONS[board.lines_dirs[d]]);
			for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
				BiNode<OperatorPosition> p;
				try {
					p = board.lines_per_dir[d].getFirst(MNKCellState.P1, k);
				} catch (Exception e) {
					continue;
				}
				if(p != null) {
					System.out.println("\tline: " + k);
					do {
						System.out.println("\t\t" + p.item);
					} while((p = p.next) != null);
				}
			}
		}
		System.out.println("Alignments per line - P2:");
		for(int d = 0; d < board.lines_dirs.length; d++) {
			System.out.println("\tDIRECTION: " + DbBoard.DIRECTIONS[board.lines_dirs[d]]);
			for(int k = 0; k < board.lines_per_dir[d].size(); k++) {
				BiNode<OperatorPosition> p;
				try {
					p = board.lines_per_dir[d].getFirst(MNKCellState.P2, k);
				} catch (Exception e) {
					continue;
				}
				if(p != null) {
					System.out.println("\tline: " + k);
					do {
						System.out.println("\t\t" + p.item);
					} while((p = p.next) != null);
				}
			}
		}

		System.out.println("\n\n");

	}
	
	
	
	public static void main(String[] args) {
		DbBoard board = new DbBoard(Integer.parseInt(args[0]), Integer.parseInt(args[1]), Integer.parseInt(args[2]));
		MNKCellState player = MNKCellState.P1;

		for(AlignmentsMap al : Operators.ALIGNMENTS) System.out.println(al.size());

		debugBoard(board, false, false, false);
		
		Scanner scanner = new Scanner(System.in);
		while(true) {
			int i, j;
			String action;
			System.out.println("NEXT MOVE:");
			System.out.println("action[a/r]: ");
			action = scanner.next();
			System.out.println("i,j: ");
			i = Integer.parseInt(scanner.next());
			j = Integer.parseInt(scanner.next());
			if(action.equals("a")) board.markCell(new MovePair(i, j), player);
			//else if(action.equals("r")) board.unmarkCell();
			
			debugBoard(board, false, false, false);

			if(board.gameState != MNKGameState.OPEN) break;

			player = Auxiliary.opponent(player);
		}

		scanner.close();

	}

}
