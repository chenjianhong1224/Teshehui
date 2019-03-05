package com.cjh.teshehui.swing.task;

import java.util.Date;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.MimeMessage;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.cjh.teshehui.swing.service.impl.AudioService;

public class EmailNoticeTask implements Runnable {
	long lastNoticeTime = 0;
	String email;
	private final String host = "smtp.163.com";
	private final Integer port = 25;
	private final String userName;
	private final String password;
	private JavaMailSenderImpl sender;

	public EmailNoticeTask(String userName, String password) {
		this.userName = userName;
		this.password = password;
		sender = new JavaMailSenderImpl();
		sender.setHost(host);
		sender.setPort(port);
		sender.setUsername(userName);
		sender.setPassword(password);
		sender.setDefaultEncoding("Utf-8");
		Properties p = new Properties();
		p.setProperty("mail.smtp.timeout", "25000");
		p.setProperty("mail.smtp.auth", "false");
		sender.setJavaMailProperties(p);
	}

	public static LinkedBlockingQueue<Integer> msgQueue = new LinkedBlockingQueue<Integer>();

	@Override
	public void run() {
		while (!OrderTask.getTaskFinishFlag().get()) {
			try {
				Integer t = msgQueue.poll(300, TimeUnit.MILLISECONDS);
				if (t != null) {
					Date now = new Date();
					if (now.getTime() - lastNoticeTime > 15 * 60 * 1000) {
						try {
							MimeMessage mimeMessage = sender.createMimeMessage();
							// 设置utf-8或GBK编码，否则邮件会有乱码
							MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
							messageHelper.setFrom(userName, "系统通知");
							messageHelper.setTo(userName);
							messageHelper.setSubject("特奢汇抢到货了");
							messageHelper.setText("", true);
							sender.send(mimeMessage);
							lastNoticeTime = now.getTime();
						} catch (Exception e1) {

						}
					}
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
