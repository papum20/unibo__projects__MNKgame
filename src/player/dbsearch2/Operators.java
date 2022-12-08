/*
 * un operatore è definito quando si ha una sequenza di caselle in fila con le seguenti proprietà:
 * 	-	primo e ultimo elemento
 * 	-	
 */


package player.dbsearch2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import mnkgame.MNKCell;
import mnkgame.MNKCellState;
import player.pnsearch.structures.INodes.MovePair;



public class Operators {

	//byte CODES FOR THREATS
	public static final byte LINE_0		= 0;	//xxxxx				k
	public static final byte LINE_1F	= 16;	//_xxxx_			straight k-1
	public static final byte LINE_1a	= 17;	//_xxxx				k-1
	public static final byte LINE_1b	= 18;	//xx_xx				k-1
	public static final byte LINE_2B	= 32;	//_xx_x_			broken	k-2
	public static final byte LINE_2		= 33;	//__xxx_			2	replies	k-2
	public static final byte LINE_2T	= 34;	//__xxx__			3 	replies	k-2
	public static final byte LINE_21a	= 35;	//xxx__				any k-2 in k
	public static final byte LINE_21b	= 36;	//xx_x_				any k-2 in k
	public static final byte LINE_21c	= 37;	//xx__x				any k-2 in k
	public static final byte LINE_3B	= 48;	//_x_x__			broken	k-3
	public static final byte LINE_3B2	= 49;	//_x__x_			broken	k-3 2-holes
	public static final byte LINE_3		= 50;	//__xx__			2 replies k-3
	public static final byte LINE_32	= 51;	//_xx___			any k-3 in k-1 with 2 empty boders
	public static final byte LINE_3T	= 52;	//__xx___			3 replies k-3
	public static final byte LINE_3Bb	= 53;	//__x_x___			3 replies	k-2
	/*
	public static final byte THREAT_0	= 0;	//xxx_x		xxxXx		k					PREREQUISITE: LINE_1|any(TIER 1)
	public static final byte THREAT_1F	= 16;	//_x_xx_	_xXxx_		straight k-1		PREREQUISITE: LINE_2|LINE_2B|any(TIER 2)
	public static final byte THREAT_1	= 17;	//_x_xx		XxOxx		k-1					PREREQUISITE: LINE_23|any(TIER 2)
	public static final byte THREAT_2B	= 32;	//_x__x_	_OxXOxO_	broken		k-2		PREREQUISITE: LINE_3B|LINE_3B2|LINE_32|any(TIER 3)
	public static final byte THREAT_2	= 33;	//__x_x__	_OxXxO_		2 replies	k-2			PREREQUISITE: ...
	public static final byte THREAT_2T	= 34;	//__x_x___	_OxXxOO_	3 replies	k-2		PREREQUISITE: ...
	*/
	
	private static final byte THREAT_MASK = (byte)240;		//(bin)11110000

	
	
	public static final short MAX_LINE				= 0;	//max length of alignment (K+MAX_LINE) (including marks and spaces inside)
	public static final short MAX_FREE_EXTRA		= 3;	//max length by which a sequence of aligned symbols can extend, with empty squares
	public static final short MAX_FREE_EXTRA_TOT	= 5;	//left+right
	public static final short MAX_FREE_IN			= 2;	//max number of missing symbols such that K-MAX_FREE_LINE is considered an alignment
	//MAX_LINE = K
	//min-max x such that there must be, for a threat, k-x marks aligned
	public static final short MARK_DIFF_MIN	= 3;
	public static final short MARK_DIFF_MAX	= 0;
	
	// STATIC INSTANCES OF APPLIERS
	private static ApplierNull applierNull = new ApplierNull();
	private static Applier1first applier1first = new Applier1first();
	private static Applier1in applier1in = new Applier1in();
	private static Applier1second applier1second = new Applier1second();
	private static Applier1_1first_or_in applier1_1first_or_in = new Applier1_1first_or_in();
	private static Applier1_1in_or_in applier1_1in_or_in = new Applier1_1in_or_in();
	private static Applier1_3second_or_in applier1_3second_or_in = new Applier1_3second_or_in();
	private static Applier1_3in_or_in applier1_3in_or_in = new Applier1_3in_or_in();
	private static Applier1_2third applier1_2third = new Applier1_2third();
	private static Applier1_2in applier1_2in = new Applier1_2in();

	

	
	
	public static final byte[][] ALIGNMENTS_CODES = {
		new byte[]{LINE_0},
		new byte[]{LINE_1F, LINE_1a, LINE_1b},
		new byte[]{LINE_2B, LINE_2, LINE_2T, LINE_21a, LINE_21b, LINE_21c},
		new byte[]{LINE_3B, LINE_3B2, LINE_3, LINE_32, LINE_3T, LINE_3Bb}
	};
	public static final AlignmentsMap[] ALIGNMENTS = {
		//TIER 0
		new AlignmentsMap(
			ALIGNMENTS_CODES[0],
			new Alignment[]{
				new Alignment(0, 0, 0, 0, 0)	//xxxxx
			}
		),
		//TIER 1
		new AlignmentsMap(
			ALIGNMENTS_CODES[1],
			new Alignment[]{
				new Alignment(-1, -1, 0, 2, 1),			//_xxxx_
				new Alignment(-1, -1, 0, 1, 0),			//xxxx_
				new Alignment(0, -1, 1, 0, 0)		//xxx_x
			}
		),
		//TIER 2
		new AlignmentsMap(
			ALIGNMENTS_CODES[2],
			new Alignment[]{
				new Alignment(-1, -2, 1, 2, 1),		//_xx_x_
				new Alignment(-2, -2, 0, 3, 1),		//_xxx__
				new Alignment(-2, -2, 0, 4, 2),		//__xxx__
				new Alignment(-2, -2, 0, 2, 0),		//xxx__
				new Alignment(-1, -2, 1, 1, 0),		//xx_x_
				new Alignment(0, -2, 2, 0, 0)	//xx__x
			}
		),
		//TIER 3
		new AlignmentsMap(
				ALIGNMENTS_CODES[3],
			new Alignment[]{
				new Alignment(-2, -3, 1, 3, 1),	//_x_x__
				new Alignment(-1, -3, 2, 2, 1),	//_x__x_
				new Alignment(-3, -3, 0, 4, 2),	//__xx__
				new Alignment(-3, -3, 0, 4, 1),	//_xx___
				new Alignment(-3, -3, 0, 5, 2),	//__xx___
				new Alignment(-2, -3, 1, 4, 2)	//__x_x__->_oxxxo_
			}
		)
	};
	
	
	/*
	 * also, are defined some alignments which must respect precise patterns:
	 * 	line[]:	aligned cells pattern
	 * 	out:	tot free cells outside (before and after) aligned cells
	 * 	mnout:	min free cells per sided (min )
	 */
	
	public static final int THREAT_TIERS = ALIGNMENTS_CODES.length - 1;
	public static final int MAX_TIER = THREAT_TIERS - 1;
	
	public static final OperatorsMap[] OPERATORS = {
		//TIER 0
		new OperatorsMap(
			ALIGNMENTS_CODES[0],
			new Applier[]{
				applierNull				//xxxxx->do nothigh
			}
		),
		//TIER 1
		new OperatorsMap(
			ALIGNMENTS_CODES[1],
			new Applier[]{
				applierNull,			//_xxxx_->do nothing (implicit in [1])
				applier1first,			//xxxx_ ->xxxxX
				applier1in				//xxx_x ->xxxXx
			}
		),
		//TIER 2
		new OperatorsMap(
			ALIGNMENTS_CODES[2],
			new Applier[]{
				applier1in,				//_xx_x_->_xxXx_
				applier1second,			//_xxx__->_xxxX_
				applierNull,			//__xxx__->do nothing (implicit in [1])
				applier1_1first_or_in,	//xxx__->xxxXO/xxxOX
				applier1_1first_or_in,	//xx_x_->xxXxO/xxOxX
				applier1_1in_or_in		//xx__x->xxxOx/xxOXx
			}
		),
		//TIER 3
		new OperatorsMap(
				ALIGNMENTS_CODES[3],
			new Applier[]{
				applier1_3second_or_in,		//_x_x__ ->OxXxOO/OxOxXO
				applier1_3in_or_in,			//_x__x_ ->OxXOxO/OxOXxO
				applierNull,				//__xx__ ->do nothing (implicit in [3])
				applier1_3second_or_in,		//_xx___ ->OxxXOO/OxxOXO
				applier1_2third,			//__xx___->_OxxXO_
				applier1_2in				//__x_x__->_OxXxO_
			}
		)
	};

	
	
	
	// 0...7 (also -8...-1)
	public static byte threatTier(byte threat) {
		return (byte) (((threat & THREAT_MASK) >> (byte)4) - 1);
	}
	public static Threat applied(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
		return OPERATORS[threatTier(op.type)].get((int)(op.type)).add(board, op, attacker, defender);
	}
	public static MNKCell threat(MNKCell[] operator, MNKCellState attacker) {
		for(MNKCell cell : operator)
			if(cell.state == attacker) return cell;
		return null;
	}



	//#region CLASSES
		//#region MAIN
			/*
			* an operator ìs defined by the following parameters:
			* 	line:	K+line aligned cells, i.e. longest allowed distance between two marks
			* 	mark:	K+mark marked
			* 	in:		free cells needed inside aligned cells (i.e. at least one mark before, one after)
			* 	out:	tot free cells outside (before and after) aligned cells
			* 	mnout:	min free cells per sided (min )
			*/
			public static class Alignment {
				public final short line, mark, in, out, mnout;
				private Alignment(short line, short mark, short in, short out, short mnout) {
					this.line = line;
					this.mark = mark;
					this.in = in;
					this.out = out;
					this.mnout = mnout;
				}
				private Alignment(int line, int mark, int in, int out, int mnout) {
					this.line = (byte)line;
					this.mark = (byte) mark;
					this.in = (byte)in;
					this.out = (byte)out;
					this.mnout = (byte)mnout;
				}
				@Override public String toString() {
					return line + "," + mark + "," + in + "," + out + "," + mnout;
				}
			}
			public static class AlignmentsMap extends HashMap<Integer, Alignment> {
				private AlignmentsMap(byte[] keys, Alignment[] values) {
					super(keys.length);
					for(int i = 0; i < keys.length; i++)
						put((int)(keys[i]), values[i]);
				}
			}
			private static interface Applier {
				//given a board and and an alignment relative to it,
				//returns an array of arrays, each containing the cells to mark to apply an operator
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender);
			}
			public static class OperatorsMap extends HashMap<Integer, Applier> {
				private OperatorsMap(byte[] keys, Applier[] values) {
					super(keys.length);
					for(int i = 0; i < keys.length; i++)
						put((int)(keys[i]), values[i]);
				}
			}
			public static class Threat {
				private MovePair[] related;
				private USE[] uses;			//0=attacker, 1=defender, 2=both
				private static enum USE {ATK, DEF, BTH}
				private Threat(int related) {
					this.related = new MovePair[related];
					uses = new USE[related];
				}
				private void set(MovePair cell, int index, USE use) {
					related[index] = cell;
					uses[index] = use;
				}
				public int nextAtk(int index) {
					while(index < related.length) {
						if(uses[index] != USE.DEF) return index;
						else index++;
					}
					return -1;
				}
				public int nextDef(int index) {
					while(index < related.length) {
						if(uses[index] != USE.ATK) return index;
						else index++;
					}
					return -1;
				}
			}
			public static class RankedThreats extends ArrayList<LinkedList<Threat>> {
				public RankedThreats() {
					super(THREAT_TIERS);
					for(int i = 0; i < THREAT_TIERS; i++) set(i, null);
				}
				public void add(Threat threat, int tier) {
					get(tier).add(threat);
				}
			}
		//#endregion MAIN
		//#region APPLIERS
			private static class ApplierNull implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					return null;
				}
			}
			private static class Applier1first implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(1);
					if(board.cellState(op.start) == MNKCellState.FREE)	res.set(op.start, 0, Threat.USE.ATK);
					else												res.set(op.end, 0, Threat.USE.ATK);
					return res;
				}
			}
			private static class Applier1in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(1);
					MovePair dir = op.start.getDirection(op.end);
					MovePair it = op.start.getSum(dir);
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(board.cellState(it) != MNKCellState.FREE) it.sum(dir);
					//if(it.equals(op.end))	return null;
					res.set(it, 0, Threat.USE.ATK);
					return res;
				}
			}
			private static class Applier1second implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(1);
					MovePair dir = op.start.getDirection(op.end);
					MovePair cell = op.start.getSum(dir);
					if(board.cellState(cell) != MNKCellState.FREE) cell = op.end.getDiff(dir);
					res.set(cell, 0, Threat.USE.ATK);
					return res;
				}
			}
			//like 1kc, but starts from the free border
			private static class Applier1_1first_or_in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(2);
					MovePair dir;
					MovePair it;
					if (board.cellState(op.start) == MNKCellState.FREE) {
						it = new MovePair(op.start);
						dir = op.start.getDirection(op.end);
					} else {
						it = new MovePair(op.end);
						dir = op.end.getDirection(op.start);
					}
					int len = 0;
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(len < 2) {
						if(board.cellState(it) == MNKCellState.FREE) res.set(it, len++, Threat.USE.BTH);
						//if(it.equals(op.end)) len = 2;	//exit while
						it.sum(dir);
					}
					return res;
				}
			}
			private static class Applier1_1in_or_in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(2);
					MovePair dir = op.start.getDirection(op.end);
					MovePair it = new MovePair(op.start);
					int len = 0;
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(len < 2) {
						if(board.cellState(it) == MNKCellState.FREE) res.set(it, len++, Threat.USE.BTH);
						//if(it.equals(op.end)) len = 2;	//exit while
						it.sum(dir);
					}
					return res;
				}
			}
			private static class Applier1_3second_or_in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(4);
					MovePair dir = op.start.getDirection(op.end);
					MovePair it = op.start.getSum(dir);
					if (board.cellState(it) != MNKCellState.FREE) {
						it.reset(op.end.i() - dir.i(), op.end.j() - dir.j());
						dir.negate();
					}
					res.set(op.start, 0, Threat.USE.DEF); 
					res.set(it, 1, Threat.USE.BTH); 
					int ind = 1;
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(ind < 3) {
						if(board.cellState(it) == MNKCellState.FREE) ind++;
						//if(it.equals(op.end)) len = 2;	//exit while
						if(ind < 3) it.sum(dir);
					}
					res.set(it, 2, Threat.USE.BTH); 
					res.set(op.end, 3, Threat.USE.DEF); 
					return res;
				}
			}
			private static class Applier1_3in_or_in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(4);
					MovePair dir = op.start.getDirection(op.end);
					MovePair it = new MovePair(op.start);
					res.set(op.start, 0, Threat.USE.DEF);
					int ind = 1;
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(ind < 3) {
						if(board.cellState(it) == MNKCellState.FREE) res.set(it, ind++, Threat.USE.BTH);
						//if(it.equals(op.end)) len = 2;	//exit while
						it.sum(dir);
					}
					res.set(op.end, 3, Threat.USE.DEF);
					return res;
				}
			}
			private static class Applier1_2third implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(3);
					MovePair dir = op.start.getDirection(op.end);
					res.set(op.start.getSum(dir), 0, Threat.USE.DEF);
					if (board.cellState(op.start.i() + 2*dir.i(), op.start.j() + 2*dir.j()) == MNKCellState.FREE) {
						res.set(new MovePair(op.start.i() + 2*dir.i(), op.start.j() + 2*dir.j()), 1, Threat.USE.ATK);
					} else {
						res.set(new MovePair(op.end.i() - 2*dir.i(), op.end.j() - 2*dir.j()), 1, Threat.USE.ATK);
					}
					res.set(op.end.getDiff(dir), 2, Threat.USE.DEF);
					return res;
				}
			}
			private static class Applier1_2in implements Applier {
				public Threat add(DbBoard board, OperatorPosition op, MNKCellState attacker, MNKCellState defender) {
					Threat res = new Threat(3);
					MovePair dir = op.start.getDirection(op.end);
					MovePair it = op.start.getSum(dir);
					res.set(it, 0, Threat.USE.DEF);
					int ind = 0;
					//doesn't check termination condition ( && !it.equals(op.end)): assumes the operator is appliable
					while(ind < 2) {
						if(board.cellState(it) == MNKCellState.FREE) ind++;
						//if(it.equals(op.end)) len = 2;	//exit while
						if(ind < 2) it.sum(dir);
					}
					res.set(it, 1, Threat.USE.ATK);
					res.set(op.end.getDiff(dir), 2, Threat.USE.DEF);
					return res;
				}
			}
			
		//#endregion APPLIERS
	//#endregion CLASSES

}
