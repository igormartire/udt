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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.decisiontree.data.DataSet;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;
import com.decisiontree.param.GlobalParam;

/**
 * 
 * Classification (Abstract class) - Classifies a set of testing tuples with a given decision tree.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public abstract class Classification {
	
	protected static Logger log = Logger.getLogger(Classification.class);
	
	protected DataSet dataSet;
	
	protected SplitSearch splitSearch;
	
	
	/**
	 * Constructor with training dataset and 
	 * the algorithm to find best split point 
	 * @param dataSet the dataset
	 * @param splitSearch the SplitSearch
	 */
	protected Classification(DataSet dataSet, SplitSearch splitSearch){
		setDataSet(dataSet);
		setSplitSearch(splitSearch);
	}
	
	/**
	 * Finding the classifying class distribution after classify a tuple 
	 * with a given decision tree.
	 * @param tree the root node of the tree
	 * @param tuple the tuple to be classified
	 * @return the classified class distribution of the tuple
	 */
	protected abstract double [] findClsDistrib(TreeNode tree, Tuple tuple);

	/**
	 * Classify a tuple by a given decision tree
	 * @param tree the root node of the tree
	 * @param tuple the tuple to be classified
	 * @return the classified class of the tuple
	 */
	private int Classify(TreeNode tree, Tuple tuple){
		double [] classDist = findClsDistrib(tree, tuple);
	
		int max = 0;
		for(int i = 1;  i < dataSet.getNoCls(); i++)
			if(classDist[max] < classDist[i])
				max =i;

		return max;
			
	}
	/**
	 * Find the number of tuples that are correctly classified
	 * @param tree the root node of the tree
	 * @param test the testing set of tuples
	 * @return the number of tuples that are correctly classified
	 */
	private int ClassifyCount(TreeNode tree, List<Tuple> test){

			int count = 0;
			Iterator<Tuple> iter = test.iterator();
			while(iter.hasNext()){
				Tuple t = iter.next();
				if(Classify(tree, t) == t.getCls())
					count++;
			}
			
			return count;
	}

	
	/**
	 * Getting the percentage of tuples that are correctly classified.
	 * @param tree the root node of the tree
	 * @param test the testing set of tuples
	 * @return the percentage of tuples that are correctly classified
	 */
	public double ClassifyAll(TreeNode tree, List<Tuple> test){

		double result = 1.0 *ClassifyCount(tree, test)/test.size();
		log.info("Percentage: " + result );
		return result;

	}

	/**
	 * Get the training data for cross-fold validation
	 * @param data the training data for cross-fold
	 * @param fold the fold number
	 * @return the set of training data represented by the fold number
	 */
	protected List<Tuple> getTrainData(List<Tuple> data, int fold){
		ArrayList<Tuple> tList = new ArrayList<Tuple>();
		
		for(int i =0; i < data.size(); i++){
			if(i < data.size() * fold/GlobalParam.NOFOLD || i >= data.size() * (fold+1) /GlobalParam.NOFOLD)
				tList.add(data.get(i));
		}
		return tList;

	}
	/**
	 * Get the testing data for cross-fold validation
	 * @param data the testing data for cross-fold
	 * @param fold the fold number
	 * @return the set of testing data represented by the fold number
	 */
	protected List<Tuple> getTestData(List<Tuple> data, int fold){
		ArrayList<Tuple> tList = new ArrayList<Tuple>();
		
		for(int i =0; i < data.size(); i++){
			if(i >= data.size() * fold/GlobalParam.NOFOLD && i < data.size() * (fold+1) /GlobalParam.NOFOLD)
				tList.add(data.get(i));
		}

		return tList;
	}

	/**
	 * Doing cross-fold validation for the fold given by fold number
	 * @param fold the fold number
	 * @param nodeSize the min tree node size
	 * @param purityThreshold the purity of the node for the tree to stop
	 * @return percentage of tuples that are correctly classified
	 */
	protected abstract double crossFold(int fold, double nodeSize, double purityThreshold);
	
	/**
	 * Doing cross-fold validation for all the folds
	 * @param nodeSize
	 * @param purityThreshold
	 * @return the overall classifcation accuracy
	 */
	public double crossAllFold(double nodeSize, double purityThreshold){
		
		double totalPercent = 0.0;
		for(int i = 0; i < GlobalParam.NOFOLD; i++){
			totalPercent +=  crossFold(i, nodeSize, purityThreshold);	
		}
		
		return totalPercent/GlobalParam.NOFOLD;
	}


	/**
	 * Get the training dataset
	 * @return the training dataset
	 */
	protected DataSet getDataSet() {
		return dataSet;
	}

	/**
	 * Set the training dataset
	 * @param dataSet the training dataset
	 */
	protected void setDataSet(DataSet dataSet) {
		this.dataSet = dataSet;
	}

	/**
	 * Get the algorithm (SplitSearch) for best split point search
	 * @return the SplitSearch object
	 */
	protected SplitSearch getSplitSearch() {
		return splitSearch;
	}

	/**
	 * Set the algorithm (SplitSearch) for best split point search
	 * @param splitSearch the SplitSearch object
	 */
	protected void setSplitSearch(SplitSearch splitSearch) {
		this.splitSearch = splitSearch;
	}
}
