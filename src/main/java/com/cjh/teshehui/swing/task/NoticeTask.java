package com.cjh.teshehui.swing.task;

import java.util.Date;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import com.cjh.teshehui.swing.service.impl.AudioService;

public class NoticeTask implements Runnable {

	long lastNoticeTime = 0;

	public static LinkedBlockingQueue<Integer> msgQueue = new LinkedBlockingQueue<Integer>();

	@Override
	public void run() {
		while (!OrderTask.getTaskFinishFlag().get()) {
			try {
				Integer t = msgQueue.poll(300, TimeUnit.MILLISECONDS);
				Date now = new Date();
				if (now.getTime() - lastNoticeTime > 60000) {
					AudioService.getInstance().play("1.wav");
					lastNoticeTime = now.getTime();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

}
