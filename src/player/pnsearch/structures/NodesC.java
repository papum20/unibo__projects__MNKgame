package player.pnsearch.structures;

import java.util.LinkedList;



public class NodesC extends INodesC {
	
	// INSTANCE FOR PnSearch
	public static class NodeE extends Node_e<MovePair,Value,NodeE> {
		short i, j;
		
		public NodeE()								{super();}
		public NodeE(MovePair move, NodeE parent)	{super(move, parent);}
		//public NodeE(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}

		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeE(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeE>();}
	}
	
	// INSTANCE FOR PnSearchDelete
	public static class NodeED extends Node_ed<MovePair,Value,NodeED> {
		short i, j;
		
		public NodeED()																{super();}
		public NodeED(MovePair move, NodeED parent)								{super(move, parent);}
		//public NodeED(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}
		
		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeED(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeED>();}
	}
	
	public static class NodeEDS extends Node_eds<MovePair,Value,NodeEDS> {
		short i, j;

		public NodeEDS()								{super();}
		public NodeEDS(MovePair move, NodeEDS parent)	{super(move, parent);}
		//public NodeEDS(MovePair move, Value value, short proof, short disproof)	{super(move, value, proof, disproof);}

		// BOOL
		@Override protected boolean equalMoves(MovePair a, MovePair b) {return a.i == b.i && a.j == b.j;}
		// GET
		@Override public MovePair getMove() {return new MovePair(i, j);}
		@Override public MovePair getPosition() {return new MovePair(i, j);}
		@Override public short i() {return i;}
		@Override public short j() {return j;}
		@Override public Value getValue() {
			if(proof == PROOF_N_ZERO) return Value.TRUE;
			else if(disproof == PROOF_N_ZERO) return Value.FALSE;
			else return Value.UNKNOWN;
		}
		// SET
		@Override public void setValue(Value value) {
			if(value == Value.TRUE) setProofDisproof(PROOF_N_ZERO, PROOF_N_INFINITE);
			else if(value == Value.FALSE) setProofDisproof(PROOF_N_INFINITE, PROOF_N_ZERO);
			else setProofDisproof((short)1, (short)1);
		}
		public void addChild(MovePair move) {children.addLast(new NodeEDS(move, this));}
		// INIT
		@Override protected void setMove(MovePair move) {
			this.i = move.i();
			this.j = move.j();
		}
		@Override @Deprecated protected void resetValue() {}
		@Override @Deprecated protected void evalValue() {}
		@Override protected void generateChildren() {this.children = new LinkedList<NodeEDS>();}
	}
	
}
