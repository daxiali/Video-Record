package com.feifei.bill.util;

import java.util.ArrayList;
import java.util.List;

public class CommonUitls {
	public static List<String> getRecordType() {
		List<String> typeList = new ArrayList<String>();
		typeList.add("超市类");
		typeList.add("网购类");
		typeList.add("汽车类");
		typeList.add("菜场类");
		typeList.add("其它");
		return typeList;
	}

	public static List<String> getRecordPerson() {
		List<String> typeList = new ArrayList<String>();
		typeList.add("飞飞廖");
		typeList.add("春芬刘");
		typeList.add("小灰灰");
		typeList.add("其它");
		return typeList;
	}
}
