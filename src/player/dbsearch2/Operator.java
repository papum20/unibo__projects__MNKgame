/*
 * un operatore è definito quando si ha una sequenza di caselle in fila con le seguenti proprietà:
 * 	-	primo e ultimo elemento
 * 	-	
 */


package player.dbsearch2;

import java.util.HashMap;



public class Operator {

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
	public static final byte THREAT_0	= 0;	//xxx_x		xxxXx		k					PREREQUISITE: LINE_1|any(TIER 1)
	public static final byte THREAT_1F	= 16;	//_x_xx_	_xXxx_		straight k-1		PREREQUISITE: LINE_2|LINE_2B|any(TIER 2)
	public static final byte THREAT_1	= 17;	//_x_xx		XxOxx		k-1					PREREQUISITE: LINE_23|any(TIER 2)
	public static final byte THREAT_2B	= 32;	//_x__x_	_OxXOxO_	broken		k-2		PREREQUISITE: LINE_3B|LINE_3B2|LINE_32|any(TIER 3)
	public static final byte THREAT_2	= 33;	//__x_x__	_OxXxO_		2 replies	k-2			PREREQUISITE: ...
	public static final byte THREAT_2T	= 34;	//__x_x___	_OxXxOO_	3 replies	k-2		PREREQUISITE: ...
	
	private static final byte THREAT_MASK = (byte)240;		//(bin)11110000
	

	public static final short MAX_LINE				= 0;	//max length of alignment (K+MAX_LINE) (including marks and spaces inside)
	public static final short MAX_FREE_EXTRA		= 3;	//max length by which a sequence of aligned symbols can extend, with empty squares
	public static final short MAX_FREE_EXTRA_TOT	= 5;	//left+right
	public static final short MAX_FREE_IN			= 2;	//max number of missing symbols such that K-MAX_FREE_LINE is considered an alignment
	//MAX_LINE = K
	//min-max x such that there must be, for a threat, k-x marks aligned
	public static final short MARK_DIFF_MIN	= 3;
	public static final short MARK_DIFF_MAX	= 0;
	



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
	public static class AlignmentMap extends HashMap<Integer, Alignment> {
		private AlignmentMap(byte[] keys, Alignment[] values) {
			super(keys.length);
			for(int i = 0; i < keys.length; i++)
				put((int)(keys[i]), values[i]);
		}
	}


	public static final byte[][] ALIGNMENTS_CODES = {
		new byte[]{LINE_0},
		new byte[]{LINE_1F, LINE_1a, LINE_1b},
		new byte[]{LINE_2B, LINE_2, LINE_2T, LINE_21a, LINE_21b, LINE_21c},
		new byte[]{LINE_3B, LINE_3B2, LINE_3, LINE_32, LINE_3T}
	};
	public static final AlignmentMap[] ALIGNMENTS = {
		//TIER 0
		new AlignmentMap(
			ALIGNMENTS_CODES[0],
			new Alignment[]{
				new Alignment(0, 0, 0, 0, 0)	//xxxxx
			}
			),
		//TIER 1
		new AlignmentMap(
			ALIGNMENTS_CODES[1],
			new Alignment[]{
				new Alignment(-1, -1, 0, 2, 1),			//_xxxx_
				new Alignment(-1, -1, 0, 1, 0),			//xxxx_
				new Alignment(0, -1, 1, 0, 0)		//xxx_x
			}
		),
		//TIER 2
		new AlignmentMap(
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
		new AlignmentMap(
				ALIGNMENTS_CODES[3],
			new Alignment[]{
				new Alignment(-2, -3, 1, 3, 1),	//_x_x__
				new Alignment(-1, -3, 2, 2, 1),	//_x__x_
				new Alignment(-3, -3, 0, 4, 2),	//__xx__
				new Alignment(-3, -3, 0, 4, 1),	//_xx___
				new Alignment(-3, -3, 0, 5, 2)	//__xx___
			}
		)
	};
	
	
	/*
	 * also, are defined some alignments which must respect precise patterns:
	 * 	line[]:	aligned cells pattern
	 * 	out:	tot free cells outside (before and after) aligned cells
	 * 	mnout:	min free cells per sided (min )
	 */

	
	
	
	
	// 0...7 (also -8...-1)
	public static byte threatTier(byte threat) {
		return (byte)((threat & THREAT_MASK) >> (byte)4);
	}

}
