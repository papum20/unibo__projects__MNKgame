package player.pnsearch.structures;

import mnkgame.MNKCell;



public class NodesA extends INodesA {
	
	//INSTANCE : Node with arrays
	public static class NodeA extends Node_ae<NodeA> {
		public NodeA(int children_max) {super(children_max);}
		public NodeA(Move move, NodeA parent, int children_max) {super(move, parent, children_max);}
		public NodeA(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		@Override
		protected void init(Move move, Value value, short proof, short disproof, NodeA parent, int children_max) {
			super.init(move, value, proof, disproof, parent, children_max);
			children = new NodeA[children_max];
		}

		public void addChild(MNKCell move) {
			children[children_n++] = new NodeA(new Move(move), this, children.length);
		}
	}
	
	public static class NodeAD extends Node_ad<NodeAD> {
		public NodeAD() {super();}
		//public NodeAD(int children_max) {super(children_max);}
		public NodeAD(Move move, NodeAD parent) {super(move, parent);}
		//public NodeAD(Move move, NodeAD parent, int children_max) {super(move, parent, children_max);}
		//public NodeAD(Move move, Value value, short proof, short disproof, int children_max) {super(move, value, proof, disproof, children_max);}
		
		// SET
		public void addChild(MNKCell move) {
			children[children_n++] = new NodeAD(new Move(move), this);
		}		
		public void expand(int children_max) {
			expanded = true;
			children = new NodeAD[children_max];
		}
	}

	public static class NodeADS extends Node_ads<NodeADS> {
		public NodeADS() {super();}
		public NodeADS(Move move, NodeADS parent) {super(move, parent);}

		// SET
		public void addChild(MNKCell move) {
			children[children_n++] = new NodeADS(new Move(move), this);
		}		
		public void expand(int children_max) {
			expanded = true;
			children = new NodeADS[children_max];
		}	
	}

}
