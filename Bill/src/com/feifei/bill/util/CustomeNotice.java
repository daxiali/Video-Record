package com.feifei.bill.util;

import com.feifei.bill.R;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;


public class CustomeNotice {
	/**
	 * 
	 * 
	 * @param context
	 * @param nm
	 *            ֪ͨ������
	 * @param pd
	 *            ������ʾ
	 * @param geoLat
	 *            ����
	 * @param geoLng
	 *            γ�� void
	 */
	@SuppressWarnings("deprecation")
	public static void NotifiyManager(Context context, NotificationManager nm,
			PendingIntent pd,String msg) {
		
		// �½�״̬��֪ͨ
		Notification baseNF = new Notification();
		// ����֪ͨ��״̬����ʾ��ͼ��
		baseNF.icon = R.drawable.rectmenuidi_hover;
		// ֪ͨʱ��״̬����ʾ������
		baseNF.tickerText = "�����Զ�������";
		
		// ֪ͨ��Ĭ�ϲ��� DEFAULT_SOUND, DEFAULT_VIBRATE, DEFAULT_LIGHTS.
		// ���Ҫȫ������Ĭ��ֵ, �� DEFAULT_ALL.
		// �˴�����Ĭ������
		baseNF.defaults |= Notification.DEFAULT_SOUND;
		baseNF.defaults |= Notification.DEFAULT_VIBRATE;
		baseNF.defaults |= Notification.DEFAULT_LIGHTS;

		// ��������������ѭ����ֱ���û���Ӧ
		// baseNF.flags |= Notification.FLAG_INSISTENT;

		// ֪ͨ��������Զ���ʧ
		baseNF.flags |= Notification.FLAG_AUTO_CANCEL;

		// ���'Clear'ʱ���������֪ͨ(QQ��֪ͨ�޷�����������õ����)
		// baseNF.flags |= Notification.FLAG_NO_CLEAR;

		// �ڶ������� ������״̬��ʱ��ʾ����Ϣ���� expanded message title
		// ����������������״̬��ʱ��ʾ����Ϣ���� expanded message text
		// ���ĸ������������֪ͨʱִ��ҳ����ת
		baseNF.setLatestEventInfo(context, "service��������", msg, pd);

		// ����״̬��֪ͨ
		// The first parameter is the unique ID for the Notification
		// and the second is the Notification object.
		nm.notify(110, baseNF);
	}
}
