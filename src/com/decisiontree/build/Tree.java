/**
 * Decision Tree Classification With Uncertain Data (UDT)
 * Copyright (C) 2009, The Database Group,
 * Department of Computer Science, The University of Hong Kong
 *
 * This file is part of UDT.
 *
 * UDT is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * UDT is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.decisiontree.build;

import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.data.DataSet;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitData;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;

/**
 *
 * Tree (Abstract class) - Builds a decision tree.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public abstract class Tree {

	private static final Logger log = Logger.getLogger(Tree.class);

	protected static final int NO_PARTITION = GlobalParam.DEFAULT_PARTITION;
	private TreeNode tree;
	protected DataSet dataSet;

	public double nodeSize = GlobalParam.DEFAULT_NODESIZE;
	public double purityThreshold = GlobalParam.DEFAULT_PURITY_THRESHOLD;

	public SplitSearch splitSearch = null;

	/**
	 * Constructor with dataset
	 * @param dataSet the dataset
	 */
	public Tree(DataSet dataSet){
		setDataSet(dataSet);
		tree = null;
	}

	/**
	 * Constructor with dataset and pre-pruning parameters
	 * @param dataSet the dataset
	 * @param nodeSize the size of the node when the tree-building would stop
	 * @param purity the purity of the node when the tree building would stop
	 */
	public Tree(DataSet dataSet, double nodeSize, double purity){
		this(dataSet);
		this.nodeSize = nodeSize;
		this.purityThreshold = purity;
	}

	/**
	 * Constructor with dataset and the algorithm (SplitSearch) to find the best split.
	 * @param dataSet the dataset
	 * @param splitSearch the SplitSearch object
	 */
	public Tree(DataSet dataSet, SplitSearch splitSearch){
		this(dataSet);
		setSplitSearch(splitSearch);
	}

	/**
	 * Constructor with dataset, pre-pruning parameters and the algorithm (SplitSearch) to find the best split.
	 * @param dataSet the dataset
	 * @param splitSearch the SplitSearch object
	 * @param nodeSize the size of the node when the tree-building would stop
	 * @param purity the purity of the node when the tree building would stop
	 */
	public Tree(DataSet dataSet, SplitSearch splitSearch , double nodeSize, double purity){
		this(dataSet, nodeSize, purity);
		setSplitSearch(splitSearch);
	}


	/**
	 * Constructing the decision with postpruning on the dataSet stored
	 * @param print printing the tree in console if true
	 */
	public void constructFinalTree(boolean print){
		constructTree();
		if(print) printTree(getRoot(),0);
		postPruning(getRoot());
	}

	/**
	 * Contructing the decision on the dataSet stored and store it in tree object
	 */
	public void constructTree(){
		tree = buildDTree(dataSet.getData(), 0);

	}


	/**
	 * Finding the best attribute, split point pairs for a list of data tuples.
	 * @param data the list of data tuples
	 * @return the SplitData object storing the best split information
	 */
	public SplitData findBestAttr(List<Tuple> data){

		if(splitSearch == null){
			log.error("No SplitSearch initialized");
			return null;
		}

		return splitSearch.findBestAttr(data, dataSet.getNoCls(), dataSet.getNoAttr());

	}

	/**
	 * Finding pessimistic error of a given root node of the tree
	 * @param tree the root node of the tree
	 * @return the pessimistic error
	 */
	private double findPError(TreeNode tree){

		double noTuples = tree.getWeightedNoTuples();
		double error = tree.getError();
		return (pessiError(error,noTuples) + error)/noTuples;

	}

	/**
	 * Building the decision tree with the given data tuples at given height
	 * @param data the given data tuples
	 * @param height the height of the root node of the tree
	 * @return the built decision tree at the given height
	 */
	public TreeNode buildDTree(List<Tuple> data, int height){

		double [] clsDist = Tuple.computeClsDist(data, dataSet.getNoCls());
		double weightedNoTuples = Tuple.countWeightedTuples(data);
		double dispersion = splitSearch.findDispersion(clsDist,weightedNoTuples);
		log.debug("Node info: " + weightedNoTuples + " " + dispersion + " " + height);

		TreeNode treeNode = new TreeNode(clsDist, weightedNoTuples, dispersion);
//		TreeNode treeNode = new TreeNode(data, null, dataSet.getNoCls());
		GlobalParam.incrNoNode();

//		treeNode.setHeight(height);
//		log.debug("Total Tuple at level " + height + ": " + treeNode.getWeightedNoTuples());

		treeNode.setMajorityCls(TreeUtil.findMajorityCls(clsDist));
		boolean singleCls =	TreeUtil.isSingleCls(clsDist);
		if(singleCls){
			log.debug("Level " + height + ":  Same Class - "+ treeNode.getMajorityCls());
			treeNode.setType(TreeNode.LEAF);
			return treeNode;
		}

		if( treeNode.getWeightedNoTuples() <= nodeSize  || treeNode.getPurity() - purityThreshold > 1E-12){
			log.debug("Level " + height + ":  Pruned - "+ treeNode.getMajorityCls());
			treeNode.setType(TreeNode.LEAF);
			return treeNode;
		}

		SplitData splitData = findBestAttr(data);

		if(!splitData.isValidSplit() || treeNode.getDispersion() < splitData.getDispersion() || Math.abs(treeNode.getDispersion() - splitData.getDispersion()) < 1E-12){
			log.debug("Level " + height + ":  No Best Attribute - "+ treeNode.getDispersion() + " " + splitData.getDispersion());
			treeNode.setType(TreeNode.LEAF);
			return treeNode;
		}

		int attrNum = splitData.getAttrNum();
		List<List<Tuple>> partitions = genPartitions(data, attrNum, splitData.getSplitPt());

		treeNode.setType(TreeNode.INTERAL);
		treeNode.setAttrNum(attrNum);
		treeNode.setNoChildren(NO_PARTITION);
		treeNode.setSplit(splitData.getSplitPt());

		for(int i = 0 ; i < NO_PARTITION; i++){
			treeNode.addChild(buildDTree(partitions.get(i), height+1),i);
			treeNode.getChild(i).setParent(treeNode);
		}

		return treeNode;
	}

	/**
	 * Generate Partitions for a particular split points. It should be placed in SplitSearch once it is stable.
	 * @param data the data tuples
	 * @param attr the attribute used for split
	 * @param split the split point to split
	 * @return the list of partitioned tuples
	 */
	abstract public List<List<Tuple>> genPartitions(List<Tuple> data, int attr, double split);


	/**
	 * Get the dataset stored
	 * @return the dataset stored
	 */
	protected DataSet getDataSet() {
		return dataSet;
	}

	/**
	 * Get the root of the built decisiion tree
	 * @return the root of the decision tree
	 */
	public TreeNode getRoot(){
		return tree;
	}

	/**
	 * Get the SplitSearch object
	 * @return the SplitSearch object
	 */
	protected SplitSearch getSplitSearch() {
		return splitSearch;
	}


	/**
	 * Finding the pessimistic error for given error value and number of tuples
	 * @param e the error
	 * @param N the number of tuples
	 * @return the pessimistic error
	 */
	private double pessiError(double e, double N){

		double Coeff = 0.675;
		double CF = 0.25;
		double Val0 = 0, Pr =0;

//		int i =0;
		Coeff = Coeff * Coeff;

		if(e < 1E-6)
			return N* (1-Math.exp(Math.log(CF)/N));
		else if(e < 0.9999){
			Val0 = N* (1-Math.exp(Math.log(CF)/N));
		 	return Val0 + e * (pessiError(1.0,N) - Val0);
		}
		else if (e +0.5 >= N)
			return 0.67 * (N-e);
		else {
			Pr = (e+0.5 +Coeff/2 +Math.sqrt(Coeff * ((e+0.5) * (1-(e+0.5)/N) + Coeff/4)))/(N+Coeff);
			return (N * Pr -e);
		}


	}

	/**
	 * Post-pruning for the decision tree using pessimatic errors as in C4.5
	 * @param tree the root node of the decision tree
	 * @return the lowest of chi-square error and pessimistic error of the root node of the tree
	 */
	protected double postPruning(TreeNode tree){

		if(tree.getType() == TreeNode.LEAF){
			double k = findPError(tree);
			return k;
		}
		double chiE = 0.0;
		for(int i =0; i < tree.getNoChildren(); i++)
			chiE += postPruning(tree.getChild(i)) * tree.getChild(i).getWeightedNoTuples();
		chiE = chiE/tree.getWeightedNoTuples();

		double parE = findPError(tree);

		if(chiE > parE){
			//pruning by subtree replacement
			tree.setType(TreeNode.LEAF);
			tree.clearChildren();
			return parE;
		}
		return chiE;


	}
	/**
	 * Printing the decision tree in console. Currently use for DEBUGGING only.
	 * @param tree the root node of the decision tree
	 * @param level the level of the tree
	 */
	public void printTree(TreeNode tree, int level){
		if(tree.getType() == TreeNode.LEAF){
			for(int i = 0; i < level; i++)
				System.out.print("\t");
			System.out.println(" " + dataSet.getClsName(tree.getMajorityCls()) + " ( " + tree.getWeightedNoTuples() + ", " + tree.getError() + " )");
			return;
		}


		for(int  i=0 ;i < NO_PARTITION; i++){
			for(int j = 0; j < level; j++)
				System.out.print("\t");
			System.out.print(dataSet.getAttrName(tree.getAttrNum()));
			if(i == 0)
				System.out.println(" ( " + "<= " + tree.getSplit() + " )");
			else	System.out.println(" ( " + "> " + tree.getSplit() + " )");

			printTree(tree.getChild(i), level+1);
		}

	}

	/**
	 *
	 * Read decision tree from file - NOT IMPLEMENTED
	 * @param path the read path
	 */
	abstract public void readTree(String path);

	/**
	 *
	 * Save decision tree to file - NOT IMPLEMENTED
	 * @param path the save path
	 */
	abstract public void saveTree(String path);

	/**
	 * Set the dataset
	 * @param dataSet the dataset
	 */
	protected void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * Set the algorithm (SplitSearch object) to be use for finding best split point
	 * @param splitSearch the SplitSearch object
	 */
	protected void setSplitSearch(SplitSearch splitSearch) {
		this.splitSearch = splitSearch;
	}

}
