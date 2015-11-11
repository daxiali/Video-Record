package com.feifei.bill.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amap.api.location.AMapLocalDayWeatherForecast;
import com.feifei.bill.R;

public class WeatherUtil {
	/**
	 * <������������Ԥ�����ݶ�Ӧ�������� ����ͼ��,��,��,ѩ,��,(��,��),(��,��)>
	 * 
	 * @param forcasts
	 * @return
	 */
	public static List<Map<String, Object>> initThreeDatesWeather(
			List<AMapLocalDayWeatherForecast> forcasts) {
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < forcasts.size(); i++) {
			AMapLocalDayWeatherForecast forcast = forcasts.get(i);
			Map<String, Object> map = new HashMap<String, Object>();
			String date = "";
			if (i == 0) {
				date = "����";
			}
			if (i == 1) {
				date = "����";
			}
			if (i == 2) {
				date = "����";
			}
			if (i == 3) {
				date = forcast.getDate().substring(5,
						forcast.getDate().length());
			}
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			String weather = forcast.getDayWeather();
			int image = 0;
			if (weather.indexOf("��") != -1) {
				if (hour > 19) {
					image = R.drawable.sunnynight;
				}
				image = R.drawable.sunny;
			} else if (weather.indexOf("��") != -1) {
				if (weather.equals("������")) {
					image = R.drawable.thrain;
				}
				image = R.drawable.rain;
			} else if (weather.indexOf("ѩ") != -1) {
				image = R.drawable.snow;
			} else if (weather.indexOf("��") != -1) {
				if (hour > 19) {
					image = R.drawable.coludynight;
				}
				image = R.drawable.cloudy;
			} else if (weather.indexOf("��") != -1) {
				image = R.drawable.yin;
			} else if (weather.indexOf("��") != -1) {
				image = R.drawable.mai;
			} else if (weather.indexOf("��") != -1) {
				image = R.drawable.fog;
			} else {
				image = R.drawable.sunny;
			}
			map.put("image", image);
			map.put("details", date);
			map.put("details2",
					forcast.getDayTemp() + "/" + forcast.getNightTemp() + "��");
			map.put("details3", forcast.getDayWindDir() + "�� " + forcast.getDayWindPower()
					+ "��");
			map.put("details4", "("+forcast.getDayWeather()+")");
			map.put("details5", date+" "+forcast.getDayWeather()+" "+forcast.getDayTemp() + "/" + forcast.getNightTemp() + "��");
			result.add(map);
		}
		return result;
	}
}
