package com.cjh.teshehui.swing.task;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.cjh.teshehui.swing.bean.Coupon;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;
import com.cjh.teshehui.swing.bean.TaskResultStatistic;
import com.cjh.teshehui.swing.bean.ViewMsgBean;
import com.cjh.teshehui.swing.service.impl.TeshehuiServiceImpl;
import com.cjh.teshehui.swing.session.TeshehuiSession;

public class BatchOrderTask implements Runnable {

	public static long sleepTime = 1000;

	public BatchOrderTask(Date beginTime, Date endTime, List<SkuBean> skuBeanList, String num,
			TeshehuiSession session) {
		this.beginTime = beginTime;
		this.endTime = endTime;
		this.skuList = skuBeanList;
		this.num = num;
		teshehuiService = new TeshehuiServiceImpl(session);
	}

	TeshehuiServiceImpl teshehuiService;

	Date beginTime;

	Date endTime;

	String num;

	List<SkuBean> skuList;

	private static AtomicBoolean taskFinishFlag = new AtomicBoolean(false);

	public static AtomicBoolean getTaskFinishFlag() {
		return taskFinishFlag;
	}

	public static void setTaskFinish() {
		taskFinishFlag.set(true);
	}

	@Override
	public void run() {
		teshehuiService.getAddress();
		try {
			while (!taskFinishFlag.get()) {
				for (int i = 0; i < skuList.size(); i++) {
					SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");// 设置日期格式
					System.out.println("尝试下单开始:" + df.format(new Date()));
					ViewMsgBean msg = new ViewMsgBean();
					msg.setRow(i);
					msg.setPhone(teshehuiService.getTeshehuiSession().getUserBean().getMobilePhone());
					SkuBean sku = skuList.get(i);
					ReturnResultBean returnBean = new ReturnResultBean();
					boolean canUseCoupon = sku.getAutoCoupon();
					TeshehuiSession teshehuiSession = teshehuiService.getTeshehuiSession();
					String couponBatchCode = "";
					if (sku.getAutoCoupon()) {
						returnBean = teshehuiService.getPromotioninf(sku.getProductCode());
						canUseCoupon = true;
						if (returnBean.getResultCode() == 8888) {
							canUseCoupon = false;
						}
						couponBatchCode = (String) returnBean.getReturnObj();
						returnBean = teshehuiService.getMyCoupon(couponBatchCode);
						if (returnBean.getResultCode() == 0) {
							List<Coupon> couponList = (List<Coupon>) returnBean.getReturnObj();
							for (Coupon coupon : couponList) {
								teshehuiSession.addCoupon(coupon);
							}
						}
					}
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
					if (!canUseCoupon) {
						returnBean = teshehuiService.createOrderLikeApp(sku, num);
					} else {
						boolean hadGotFlag = teshehuiSession.hadGotVerify(couponBatchCode);
						if (!hadGotFlag) {
							returnBean = teshehuiService.getCoupon(couponBatchCode);
							if (returnBean.getResultCode() != 0 && returnBean.getReturnMsg().contains("您已领过该优惠券啦")) {
								teshehuiSession.addHadGotcouponBatch(couponBatchCode);
							}
						}
						returnBean = teshehuiService.createOrderUseMyCoupon(sku);
					}
					if (returnBean.getResultCode() == 0) {
						msg.setMsg("成功啦");
						TaskResultStatistic t = TaskResultStatistic.getInstance();
						t.addResult(teshehuiSession.getUserBean().getNickName(), i);
						ViewTask.msgQueue.put(msg);
					} else {
						msg.setMsg(returnBean.getReturnMsg());
						ViewTask.msgQueue.put(msg);
					}

					System.out.println("尝试下单结束:" + df.format(new Date()));
					Thread.sleep(sleepTime);
				}

			}
		} catch (InterruptedException e) {
			BatchOrderTask.getTaskFinishFlag().set(true);
			return;
		}
	}

}
