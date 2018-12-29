package com.cjh.teshehui.swing.bean;

import java.util.Map;

import com.google.common.collect.Maps;

public class TaskResultStatistic {
	
	private TaskResultStatistic() {

	}

	static private TaskResultStatistic instance;

	public void clear() {
		resultCollections.clear();
	}

	private Map<String, Map<Integer, Integer>> resultCollections = Maps.newHashMap();

	static public TaskResultStatistic getInstance() {
		if (instance == null) {
			synchronized (TaskResultStatistic.class) {
				if (instance == null) {
					instance = new TaskResultStatistic();
				}
			}
		}
		return instance;
	}

	synchronized public void addResult(String userName, int taskIndex) {
		Map<Integer, Integer> tmp = Maps.newHashMap();
		if (resultCollections.containsKey(userName)) {
			tmp = resultCollections.get(userName);
			if (tmp.containsKey(taskIndex)) {
				tmp.put(taskIndex, tmp.get(taskIndex) + 1);
			} else {
				tmp.put(taskIndex, 1);
			}
		} else {
			tmp.put(taskIndex, 1);
		}
		resultCollections.put(userName, tmp);
	}

	synchronized public String getTaskResult(int taskIndex) {
		if (resultCollections.keySet().size() > 0) {
			String returnStr = "";
			for (String userName : resultCollections.keySet()) {
				if (resultCollections.get(userName).get(taskIndex) != null) {
					returnStr += userName + "成功" + resultCollections.get(userName).get(taskIndex) + "个 ";
				}
			}
			return returnStr;
		} else {
			return "成功0个";
		}
	}
}
