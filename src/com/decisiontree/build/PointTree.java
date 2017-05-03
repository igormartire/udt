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
import com.decisiontree.data.PointAttribute;
import com.decisiontree.data.PointDataSet;
import com.decisiontree.data.Tuple;
import com.decisiontree.operation.SplitSearch;

/**
 * 
 * PointTree - Builds a decision tree by point-valued data.
 *
 * @author Smith Tsang
 * @since 0.8
 *
 */
public class PointTree extends Tree{
	
	private static Logger log = Logger.getLogger(PointTree.class);

	public PointTree(PointDataSet db, SplitSearch splitSearch){
		super(db, splitSearch);
	}
	
	public PointTree(DataSet db, double maxSize, double prune){
		super(db);
	}
	
	public PointTree(DataSet db, SplitSearch splitSearch , double maxSize, double prune){
		super(db, splitSearch, maxSize, prune);
	}
	
	public PointDataSet getDB(){
		return (PointDataSet) dataSet;
	}

	@Override
	public List<List<Tuple>> genPartitions(List<Tuple> data, int attrNum, double split) {
		
		List<List<Tuple>> partitions = new ArrayList<List<Tuple>>();
		for(int i = 0; i < NO_PARTITION; i++){
			partitions.add(new ArrayList<Tuple>(data.size()));
		}

		Tuple tuple = null;
		Iterator<Tuple> iter = data.iterator();
		while(iter.hasNext()){
			tuple = iter.next();
			if(((PointAttribute)(tuple.getAttribute(attrNum))).getValue() > split)
				partitions.get(1).add(tuple);
			else partitions.get(0).add(tuple);
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
