package com.feifei.bill.util;

import java.util.ArrayList;
import java.util.List;

public class CommonUitls {
	public static List<String> getRecordType() {
		List<String> typeList = new ArrayList<String>();
		typeList.add("������");
		typeList.add("������");
		typeList.add("������");
		typeList.add("�˳���");
		typeList.add("����");
		return typeList;
	}

	public static List<String> getRecordPerson() {
		List<String> typeList = new ArrayList<String>();
		typeList.add("�ɷ���");
		typeList.add("������");
		typeList.add("С�һ�");
		typeList.add("����");
		return typeList;
	}
}
