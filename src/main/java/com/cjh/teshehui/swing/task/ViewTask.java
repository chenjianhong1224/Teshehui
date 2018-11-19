package com.cjh.teshehui.swing.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import com.cjh.teshehui.swing.bean.ViewMsgBean;
import com.cjh.teshehui.swing.service.impl.AudioService;

public class ViewTask implements Runnable {

	public ViewTask(JTable table, DefaultTableModel dtm) {
		this.table = table;
		this.dtm = dtm;
	}

	JTable table;
	DefaultTableModel dtm;

	public static LinkedBlockingQueue<com.cjh.teshehui.swing.bean.ViewMsgBean> msgQueue = new LinkedBlockingQueue<ViewMsgBean>();

	@Override
	public void run() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		while (!OrderTask.getTaskFinishFlag().get()) {
			try {
				ViewMsgBean msgBean = msgQueue.poll(300, TimeUnit.MILLISECONDS);
				if (msgBean != null) {
					table.setValueAt(sdf.format(msgBean.getTime()), msgBean.getRow(), 1);
					if (msgBean.getMsg().contains("成功")) {
						table.setValueAt(msgBean.getMsg(), msgBean.getRow(), 2);
					} else {
						table.setValueAt("还未下成功", msgBean.getRow(), 2);
					}
					table.setValueAt(msgBean.getMsg(), msgBean.getRow(), 3);
					table.validate();
					try {
						if (msgBean.getMsg().contains("成功")) {
							AudioService.getInstance().play("1.wav");
						}
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			} catch (InterruptedException e) {
				OrderTask.getTaskFinishFlag().set(true);
				return;
			}
		}
	}

}
