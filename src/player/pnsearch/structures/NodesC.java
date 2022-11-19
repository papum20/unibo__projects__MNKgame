package player.pnsearch.structures;

import mnkgame.MNKCell;



public class NodesC extends INodesC {
	
	// INSTANCE FOR PnSearch
	public static class Node extends Node_e<Node> {
		public Node() {super();}
		public Node(Move move, Node parent) {super(move, parent);}
		public Node(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}

		public void addChild(MNKCell move) {
			children.addLast(new Node(new Move(move), this));
		}
	}

	// INSTANCE FOR PnSearchDelete
	public static class NodeD extends Node_d<NodeD> {
		public NodeD() {super();}
		public NodeD(Move move, NodeD parent) {super(move, parent);}
		public NodeD(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}
		
		// SET
		public void addChild(MNKCell move) {
			children.addLast(new NodeD(new Move(move), this));
		}
	}
	
	public static class NodeDS extends Node_ds<NodeDS> {
		public NodeDS() {super();}
		public NodeDS(Move move, NodeDS parent) {super(move, parent);}
		public NodeDS(Move move, Value value, short proof, short disproof) {super(move, value, proof, disproof);}

		// SET
		public void addChild(MNKCell move) {
			children.addLast(new NodeDS(new Move(move), this));
		}
	}
	
}
