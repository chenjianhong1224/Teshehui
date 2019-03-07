package com.cjh.teshehui.swing.task;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.MimeMessage;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;

import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.service.impl.AudioService;
import com.cjh.teshehui.swing.service.impl.TeshehuiServiceImpl;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.cjh.teshehui.swing.session.TeshehuiSessionManager;
import com.cjh.teshehui.swing.utils.SpringContextUtils;
import com.google.common.collect.Lists;

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

	@Override
	public void run() {
		List<TeshehuiServiceImpl> teshehuiServiceImplList = Lists.newArrayList();
		List<Long> lastNoticeTimeList = Lists.newArrayList();
		TeshehuiSessionManager teshehuiSessionManager = (TeshehuiSessionManager) SpringContextUtils.getContext()
				.getBean("teshehuiSessionManager");
		Map<String, TeshehuiSession> sessionMap = teshehuiSessionManager.getAllSession();
		for (String userName : sessionMap.keySet()) {
			TeshehuiServiceImpl teshehuiService = new TeshehuiServiceImpl(sessionMap.get(userName));
			teshehuiServiceImplList.add(teshehuiService);
			lastNoticeTimeList.add(0L);
		}
		while (!OrderTask.getTaskFinishFlag().get()) {
			for (int i = 0; i < teshehuiServiceImplList.size(); i++) {
				ReturnResultBean returnBean = teshehuiServiceImplList.get(i).checkNonPaymentOrder();
				if (returnBean.getResultCode() == 0) {
					Date now = new Date();
					if (now.getTime() - lastNoticeTimeList.get(i) > 15 * 60 * 1000) {
						try {
							MimeMessage mimeMessage = sender.createMimeMessage();
							// 设置utf-8或GBK编码，否则邮件会有乱码
							MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
							messageHelper.setFrom(userName, "系统通知");
							messageHelper.setTo(userName);
							messageHelper.setSubject("特奢汇抢到货了");
							messageHelper.setText(
									teshehuiServiceImplList.get(i).getTeshehuiSession().getUserBean().getNickName()
											+ " " + teshehuiServiceImplList.get(i).getTeshehuiSession().getUserBean()
													.getMobilePhone()
											+ " 有待支付订单",
									true);
							sender.send(mimeMessage);
							lastNoticeTimeList.set(i, now.getTime());
						} catch (Exception e1) {

						}
					}
				}
				try {
					Thread.sleep(1000 * 60);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
