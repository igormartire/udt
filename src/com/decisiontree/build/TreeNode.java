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

import org.apache.log4j.Logger;

import com.decisiontree.param.GlobalParam;

/**
 *
 * DreeNode - Represents a decision tree nodes. It could be either internal node or leaf node.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class TreeNode{

	public static Logger log = Logger.getLogger(TreeNode.class);

	public static final int INTERAL = 0;
	public static final int LEAF = 1;

//	private List<Tuple> data;
//	private int noTuples;
	private double weightedNoTuples;


	private TreeNode parent;
	private TreeNode [] children;
	private int noChildren;

	/**
	 * Node Type - internal node or leaf node
	 */
	private int type;
	private int attrNum;
	private double split;

	private double dispersion;
	private double [] clsDist;
	private int height;
//	private int noCls;

//	private boolean sameCls;

	private int majorityCls; //only for leaf node

	public TreeNode(double [] clsDist, double weightedNoTuples, double dispersion){
		this(null,clsDist,weightedNoTuples, dispersion, 0);
//		data = null;
	}

	public TreeNode(TreeNode parent,double[] clsDist, double weightedNoTuples, double dispersion, int height){
		setParent(parent);
		noChildren = -1;
		setHeight(height);
		setClsDist(clsDist);
		setWeightedNoTuples(weightedNoTuples);
		setEntropy(dispersion);
//		if(parent == null) this.height = 0;
	}



//	public TreeNode(List<Tuple> data, TreeNode parent, int noCls){
//		setNoCls(noCls);
//		setTuple(data);
//		setClsDist();
//		countTuples();
//		setEntropy();
//		setParent(parent);
//		noChildren = -1;
//		if(parent == null) height = 0;
//	}

//	public void setNoCls(int noCls){
//		this.noCls = noCls;
//	}

	public void setType(int type){
		this.type = type;
	}

	public int getType(){
		return type;
	}

//	public void setTuple(List<Tuple> data){
//		this.data = data;
//		noTuples = data.size();
//	}

//	public void countTuples(){
//		weightedNoTuples = 0;
//		Iterator<Tuple> iter = data.iterator();
//		while(iter.hasNext()){
//			weightedNoTuples += iter.next().getWeight();
//		}
//	}

	public void setAttrNum(int i){
		this.attrNum = i;
	}

	public void setParent(TreeNode parent){
		this.parent = parent;
	}

	public void setNoChildren(int noChild){
		if(noChild < 0){
			log.error("Invalid number of children,");
			return;
		}
		this.noChildren = noChild;
		children = new TreeNode[noChild];
	}

	public void addChild(TreeNode child, int pos){
		if(pos < 0 && pos >= noChildren) return;
		children[pos] = child;
	}
	public void setMajorityCls(int majorityCls){
		this.majorityCls = majorityCls;
	}

//	public List<Tuple> getData(){
//		return data;
//	}

	public int getAttrNum(){
		return attrNum;
	}

	public int getNoChildren(){
		return noChildren;
	}

	public int getMajorityCls(){
		return majorityCls;
	}

	public TreeNode getChild(int pos){
		if(pos < 0 && pos >= noChildren) return null;
		return children[pos];
	}

	public TreeNode getParent(){
		return parent;
	}

	public double getSplit(){
		return split;
	}
        public void setSplit(double split){
		this.split = split;
	}

    private void setWeightedNoTuples(double weightedNoTuples) {
    	this.weightedNoTuples = weightedNoTuples;

    }

	public double getWeightedNoTuples(){
		return weightedNoTuples;
	}

//	public int getNoTuples(){
//		return noTuples;
//	}


	public boolean isEmptyNode(){
		return weightedNoTuples < GlobalParam.DOUBLE_PRECISION;
	}

	public double getPurity(){

		return clsDist[majorityCls]/weightedNoTuples;

	}


//	public int majorityCls(){
//
//		return majorityCls;
//	}

	public void setClsDist(double [] clsDist){
		this.clsDist = clsDist;
	}

//	public void setClsDist(){
//
//		clsDist = new double[noCls];
//		for(int i =0; i< noCls; i++){
//			clsDist[i] = 0;
//		}
//
//		Tuple tuple = null;
//		Iterator<Tuple> iter = data.iterator();
//		while(iter.hasNext()){
//			tuple = iter.next();
//			clsDist[tuple.getCls()] += tuple.getWeight();
//		}
//
//		int max = 0,count =0;
//		for(int i = 0 ; i < noCls ; i++){
//			if(clsDist[max] < clsDist[i])
//				max = i;
//			if(clsDist[i] > 0)
//				count++;
//		}
//
//		this.cls = max;
//
//		sameCls = (count <= 1);
//	}

//	public void setEntropy(){
//
//		double ent = 0;
//
//		for(int i =0; i< noCls; i++)
//			ent += clsDist[i]
//					* Math.log(clsDist[i]/weightedNoTuples);
//
//		entropy = -1.0 * ent / Math.log(2.0)/ weightedNoTuples;
//	}

//	public boolean isSingleCls(){
//		return sameCls;
//	}

	public void setEntropy(double dispersion){
		this.dispersion = dispersion;
	}

	public double getDispersion(){
		return dispersion;
	}

//	public double getError(){
//
//		double count =0.0;
//		for(int i = 0 ; i < clsDist.length ; i++)
//			if(i != cls)
//				count += clsDist[i];
//
//		return count;
//
//	}

	public double getError(){
		return TreeUtil.findError(clsDist, majorityCls);
	}

	public void setHeight(int height){
		this.height = height;
	}

	public int getHeight(){
		return height;
	}
	public double [] getClsDist(){
		return clsDist;
	}

	public void clearChildren(){
		children = null;
		noChildren = -1;
	}
}