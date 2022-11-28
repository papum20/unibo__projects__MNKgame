package player.dbsearch;

import mnkgame.MNKCell;
import player.ArrayBoardDb;
import player.dbsearch.structures.NodeBoard;
import player.dbsearch.structures.Operator;
import player.pnsearch.structures.INodes.MovePair;

public class DbSearchBoard extends IDbSearch<NodeBoard> {


	
	@Override
	protected boolean isLastCombination(NodeBoard node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isLastDependency(NodeBoard node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isCombinationNode(NodeBoard node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean isDependencyNode(NodeBoard node) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void addDependencyStage_next(NodeBoard node) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void addCombinationStage_next(NodeBoard node, NodeBoard root) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected Operator[] getApplicableOperators(NodeBoard node) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected NodeBoard addChild(NodeBoard node, Operator f) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected boolean isTreeChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected boolean inConflict(NodeBoard A, NodeBoard B) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	protected void addDependingCombinations(NodeBoard A, NodeBoard B) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void findAllCombinationNodes_next(NodeBoard partner, NodeBoard next) {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected MNKCell getBestMove() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected NodeBoard newNode(ArrayBoardDb board) {
		// TODO Auto-generated method stub
		return null;
	}
	
}
