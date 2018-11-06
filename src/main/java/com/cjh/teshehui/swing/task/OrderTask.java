package com.cjh.teshehui.swing.task;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;
import com.cjh.teshehui.swing.bean.ViewMsgBean;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.utils.SpringContextUtils;

public class OrderTask implements Runnable {

	public static long sleepTime = 1000;

	public OrderTask(Date beginTime, Date endTime, SkuBean skuBean, String num, int rowIndex) {
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.sku = skuBean;
		this.num = num;
		this.rowIndex = rowIndex;
	}

	int rowIndex;

	Date beginTime;

	Date endTime;

	String num;

	SkuBean sku;

	private static AtomicBoolean taskFinishFlag = new AtomicBoolean(false);

	public static AtomicBoolean getTaskFinishFlag() {
		return taskFinishFlag;
	}

	public static void setTaskFinish() {
		taskFinishFlag.set(true);
	}

	@Override
	public void run() {
		ViewMsgBean msg = new ViewMsgBean();
		msg.setRow(rowIndex);
		try {
			while (!taskFinishFlag.get()) {
				Date now = new Date();
				msg.setTime(now);
				if (now.getTime() < beginTime.getTime()) {
					msg.setMsg("还未到点, 休息中...");
					ViewTask.msgQueue.put(msg);
				}
				if (now.getTime() > endTime.getTime()) {
					msg.setMsg("到点了, 收工...");
					ViewTask.msgQueue.put(msg);
					return;
				}
				if (now.getTime() < beginTime.getTime()) {
					if ((beginTime.getTime() - now.getTime()) < (1 * 1000 * 60)) { // 小于1分钟
						Thread.sleep(1);
						continue; // 提高刷新频率
					}
					Thread.sleep(1000 * 2); // 2秒刷新一次，避免session失效
					continue;
				}
				if (now.getTime() > endTime.getTime()) {
					msg.setMsg("到点了, 收工...");
					ViewTask.msgQueue.put(msg);
					return;
				}
				TeshehuiService teshehuiService = (TeshehuiService) SpringContextUtils.getContext()
						.getBean("teshehuiServiceImpl");
				ReturnResultBean returnBean = teshehuiService.getProductStockInfo(sku.getProductCode());
				if (returnBean.getResultCode() == 0) {
					List<SkuBean> skuList = (List<SkuBean>) returnBean.getReturnObj();
					for (SkuBean queryBean : skuList) {
						if (queryBean.getSkuCode().equals(sku.getSkuCode())) {
							if (queryBean.getSkuStock() > 0) {
								returnBean = teshehuiService.createOrder(sku);
								if (returnBean.getResultCode() == 0) {
									msg.setMsg("下单成功啦, 收队...");
									ViewTask.msgQueue.put(msg);
									return;
								} else {
									msg.setMsg(returnBean.getReturnMsg());
									ViewTask.msgQueue.put(msg);
								}
							} else {
								msg.setMsg("到点了, 开始干活...目前没有库存");
								ViewTask.msgQueue.put(msg);
							}
						}
					}
				} else {
					msg.setMsg(returnBean.getReturnMsg());
					ViewTask.msgQueue.put(msg);
				}
				Thread.sleep(sleepTime);
			}
		} catch (InterruptedException e) {
			OrderTask.getTaskFinishFlag().set(true);
			return;
		}
	}
}
