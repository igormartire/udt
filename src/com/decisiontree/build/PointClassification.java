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

import com.decisiontree.data.PointAttribute;
import com.decisiontree.data.PointDataSet;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * PointClassification - Classifies point-valued testing data with a given decision tree.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointClassification extends Classification{

	public PointClassification(PointDataSet dataSet, SplitSearch splitSearch){
		super(dataSet, splitSearch);
	}
	

	@Override
	protected double [] findClsDistrib(TreeNode tree, Tuple tuple){

		PointDataSet db = getDataSet();
		if(tree.getType() == TreeNode.LEAF){

			double [] clsDist = tree.getClsDist();
			double [] clsDistNormal = new double[db.getNoCls()];
			for(int i =0 ; i < db.getNoCls();i++){
				clsDistNormal[i] = clsDist[i]/tree.getWeightedNoTuples();
			}
			return clsDistNormal;
		}

		double clsDist[] = new double[db.getNoCls()];
		

		PointAttribute attr = (PointAttribute)(tuple.getAttribute(tree.getAttrNum()));
		int i = 0;
		if(attr.getValue() > tree.getSplit()) i= 1; 

		TreeNode child = tree.getChild(i);
		double clsDistChild[] = findClsDistrib(child,tuple);

		for(int j = 0 ; j < db.getNoCls(); j++)
			clsDist[j] = clsDistChild[j];				


		return clsDist;		


	}

	@Override
	protected double crossFold(int fold, double nodeSize, double purityThreshold){
		
		List<Tuple> train = getTrainData(getDataSet().getData(), fold);
		
		PointTree dTree = new PointTree(getDataSet(), getSplitSearch(), nodeSize, purityThreshold);

		TreeNode tree = dTree.buildDTree(train,0);
		
		List<Tuple> test = getTestData(getDataSet().getData(), fold);
		
		return	ClassifyAll(tree, test);

	}

	@Override
	public PointDataSet getDataSet() {
		return (PointDataSet)dataSet;
	}


}
