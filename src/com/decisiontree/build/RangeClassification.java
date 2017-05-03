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

import com.decisiontree.data.RangeAttribute;
import com.decisiontree.data.RangeDataSet;
import com.decisiontree.data.RangeTuple;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * RangeClassification - Classifies for interval-valued testing data with a given decision tree.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeClassification extends Classification{

	public RangeClassification(RangeDataSet dataSet, SplitSearch splitSearch){
		super(dataSet, splitSearch);
	}
	
	@Override
	public double [] findClsDistrib(TreeNode tree, Tuple tuple){
		
		if(tree.getType() == TreeNode.LEAF){

			double [] clsDist = tree.getClsDist();
			double [] clsDistNormal = new double[getDataSet().getNoCls()];
		        for(int i = 0; i < getDataSet().getNoCls(); i++){
				clsDistNormal[i] = clsDist[i]/tree.getWeightedNoTuples();
			}
			return clsDistNormal;
		}
		
		double clsDist[] = new double[getDataSet().getNoCls()];

		for(int i = 0; i < Tree.NO_PARTITION;i++){
			double start, end;
			RangeAttribute attr = (RangeAttribute)(tuple.getAttribute(tree.getAttrNum()));

			if(i==0){
                                start = attr.getAbsStart();
                                end = tree.getSplit();
                        }else{
                                start = tree.getSplit();
                                end = attr.getAbsEnd();
                        }

			if(attr.getAbsStart() >= end) continue;
			if(attr.getAbsEnd() <= start) break;
			TreeNode child = tree.getChild(i);
			
			double frac = attr.getFrac(start,end);
			if(frac <1E-12) continue;
			RangeAttribute newP = RangeAttribute.cutCopy(attr,start,end);
			RangeTuple fracT = RangeTuple.copy((RangeTuple)tuple, tree.getAttrNum(), newP, frac * tuple.getWeight());

			double clsDistChild[] = findClsDistrib(child,fracT);

			for(int j = 0 ; j < getDataSet().getNoCls(); j++)
				clsDist[j] += frac * clsDistChild[j];

		}

		return clsDist;		

	}

	@Override
	public double crossFold( int fold, double nodeSize, double purity){
		
		List<Tuple> train = getTrainData(getDataSet().getData(), fold);
		
		RangeTree dTree = new RangeTree(getDataSet(), splitSearch, nodeSize, purity);
		TreeNode tree = dTree.buildDTree(train,0);

		List<Tuple> test = getTestData(getDataSet().getData(), fold);
		return ClassifyAll(tree, test);

	}
	
	@Override
	public RangeDataSet getDataSet() {
		return (RangeDataSet)dataSet;
	}
}
