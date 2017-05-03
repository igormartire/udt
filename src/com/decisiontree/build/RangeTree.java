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

import com.decisiontree.data.RangeAttribute;
import com.decisiontree.data.RangeDataSet;
import com.decisiontree.data.RangeTuple;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * RangeTree - Builds a decision tree by interval-valued data.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class RangeTree extends Tree{
	
	private static final Logger log = Logger.getLogger(RangeTree.class);
	
	public RangeTree(RangeDataSet dataSet){
		super(dataSet);
	}

	public RangeTree(RangeDataSet dataSet, SplitSearch splitSearch){
		this(dataSet);
		setSplitSearch(splitSearch);
	}
	
	public RangeTree(RangeDataSet dataSet, SplitSearch segGenerator , double nodeSize, double purity){
		this(dataSet, nodeSize, purity);
		setSplitSearch(segGenerator);
	}
	
	public RangeTree(RangeDataSet dataSet, double nodeSize, double purity){
		super(dataSet,nodeSize, purity);
	}

	@Override
	public RangeDataSet getDataSet() {
		return (RangeDataSet)dataSet;
	}

	@Override
	public List<List<Tuple>>  genPartitions(List<Tuple> data, int attrNum, double split){
		
		List<List<Tuple>> partitions = new ArrayList<List<Tuple>>();
		for(int i = 0; i < NO_PARTITION; i++){
			partitions.add(new ArrayList<Tuple>(data.size()));
		}

		Iterator<Tuple> iter = data.iterator();
		Tuple tuple = null;
		while(iter.hasNext()){
			tuple = iter.next();
			
			RangeAttribute b = ((RangeAttribute)(tuple.getAttribute(attrNum)));
			for(int j = 0; j < NO_PARTITION; j++){
				
				if(j == 0){	
					double start = Double.NEGATIVE_INFINITY;
					double end = split;

					if(b.getStart() > end) continue;
					
					if(b.getEnd() <= end){
						partitions.get(j).add( tuple);
						break;
					}
					else{
						start = b.getStart();	
						double frac = b.getFrac(start, end);
						if(frac <= 1E-12) continue;
						
						RangeAttribute newP = RangeAttribute.cutCopy(b,start,end); 
						Tuple fracT = RangeTuple.copy((RangeTuple)tuple, attrNum, newP, frac * tuple.getWeight());
                                		partitions.get(j).add(fracT);
						
						if(frac >= (1.0 - 1E-12)) break;

					}
					
				} else{
					double start = split;
					double end = Double.POSITIVE_INFINITY;
					
					if(b.getEnd() <= start) break;

					if(b.getStart() > start){
						partitions.get(j).add(tuple);
						break;
					}
					else{
					
						end = b.getEnd();	
						double frac = b.getFrac(start, end);
                        if(frac <= 1E-12) continue;

                        RangeAttribute newP = RangeAttribute.cutCopy(b,start,end);
                        Tuple fracT = RangeTuple.copy((RangeTuple)tuple, attrNum, 
                        								newP, frac * tuple.getWeight());
                        partitions.get(j).add(fracT);

					}

				}
			}
		}

		return partitions;
	}

	@Override
	public void readTree(String path) {
		// TODO TO BE IMPLEMENTED
		log.warn("Function has not been implemented.");
		
	}

	@Override
	public void saveTree(String path) {
		// TODO TO BE IMPLEMENTED
		log.warn("Function has not been implemented.");
		
	}
}
