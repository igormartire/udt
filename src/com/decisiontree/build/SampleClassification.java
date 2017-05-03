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

import com.decisiontree.data.SampleAttribute;
import com.decisiontree.data.SampleDataSet;
import com.decisiontree.data.SampleTuple;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * SampleClassification - Classifies for interval-valued sample-distributed testing data with a given decision tree.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class SampleClassification extends Classification {
	
	public static Logger log = Logger.getLogger(SampleClassification.class);
	
	public SampleClassification(SampleDataSet dataSet, SplitSearch splitSearch){
		super(dataSet, splitSearch);
	}
	
	@Override
	public double [] findClsDistrib(TreeNode N, Tuple t){
		
		if(N.getType() == TreeNode.LEAF){

			double [] clsDist = N.getClsDist();
			double [] clsDistNormal = new double[getDataSet().getNoCls()];
		        for(int i = 0; i < getDataSet().getNoCls(); i++){
				clsDistNormal[i] = clsDist[i]/N.getWeightedNoTuples();
			}
			return clsDistNormal;
		}
		
		double clsDist[] = new double[getDataSet().getNoCls()];

		for(int i = 0; i < Tree.NO_PARTITION ;i++){
			double start, end;
			SampleAttribute attr = (SampleAttribute)(t.getAttribute(N.getAttrNum()));

			if(i==0){
                    start = attr.getAbsStart();
                    end = N.getSplit();
            }else{
                    start = N.getSplit();
                    end = attr.getAbsEnd();
            }

			if(attr.getAbsStart() >= end) continue;
			if(attr.getAbsEnd() <= start) break;
			TreeNode child = N.getChild(i);
			
			double frac = attr.getFrac(start,end);
			if(frac <1E-12) continue;
			SampleAttribute newP = SampleAttribute.cutCopy(attr,start,end);
			SampleTuple fracT = SampleTuple.copy((SampleTuple)t, N.getAttrNum(), newP, frac * t.getWeight());

			double clsDistChild[] = findClsDistrib(child,fracT);

			for(int j = 0 ; j < getDataSet().getNoCls(); j++)
				clsDist[j] += frac * clsDistChild[j];

		}

		return clsDist;		

	}

	@Override
	public double crossFold( int fold, double nodeSize, double purityThreshold){
		
		List<Tuple> train = getTrainData(getDataSet().getData(), fold);

		SampleTree dTree = new SampleTree(getDataSet(), splitSearch, nodeSize, purityThreshold);
		TreeNode tree = dTree.buildDTree(train,0);
		
		List<Tuple> test = getTestData(getDataSet().getData(), fold);
		return ClassifyAll(tree, test);

	}
	
	@Override
	public SampleDataSet getDataSet() {
		return (SampleDataSet) dataSet;
	}

}
