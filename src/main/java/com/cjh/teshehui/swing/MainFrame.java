package com.cjh.teshehui.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.border.LineBorder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSON;
import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.bean.SkuBean;
import com.cjh.teshehui.swing.bean.TaskResultStatistic;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.service.impl.TeshehuiServiceImpl;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.cjh.teshehui.swing.session.TeshehuiSessionManager;
import com.cjh.teshehui.swing.task.NoticeTask;
import com.cjh.teshehui.swing.task.OrderTask;
import com.cjh.teshehui.swing.task.ViewTask;
import com.cjh.teshehui.swing.utils.SpringContextUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.zxing.WriterException;

import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.UIManager;
import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.Panel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;
import javax.swing.border.TitledBorder;
import javax.swing.border.EtchedBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JComboBox;

@SpringBootApplication
public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JTextField phoneNoField;
	private VerifyImgCodeJDialog vImgJDialog;
	private VerifyImgCodeJDialog2 vImgJDialog2;
	private JTextPane addressLabel;
	private JLabel phoneNoLabel;
	private JLabel verifyLabel;
	private JButton getVerfiyButton;
	private JButton sessionLoginButton;
	private JButton loginButton;
	private JTextField urlField;
	private JComboBox skuComboBox;
	private JLabel productNameLabel;
	private Map<String, SkuBean> skuComboBoxMap;
	private JButton addTaskButton;
	private List<SkuBean> taskBeans;
	private List<Thread> taskThreadList = Lists.newArrayList();
	private Thread viewThread = null;
	private Thread noticeThread = null;
	private boolean isRunning = false;
	private JButton excuteButton;

	private JTable table;
	private DefaultTableModel dtm = null;
	private JTextArea authCode;
	private JTextArea proxyPlan;
	private JCheckBox chckbxNewCheckBox_1;
	private JFormattedTextField lunxuTime;
	private JTextField smsCodeField;
	private JTextField ydmUserNmae;
	private JTextField ydmPasswd;
	private JTextPane ydmMsg;

	/**
	 * Launch the application.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) {
		SpringApplication.run(MainFrame.class, args);
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame frame = new MainFrame();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void doLoginOrOut(boolean isLogin) {
		addressLabel.setVisible(isLogin);
		phoneNoLabel.setVisible(!isLogin);
		phoneNoField.setVisible(!isLogin);
		verifyLabel.setVisible(!isLogin);
		smsCodeField.setVisible(!isLogin);
		getVerfiyButton.setVisible(!isLogin);
		sessionLoginButton.setVisible(!isLogin);
		if (isLogin) {
			loginButton.setText("登出");
		} else {
			loginButton.setText("登录");
			phoneNoField.setEditable(true);
		}
	}

	private void doExcute(boolean isExcute) {
		addTaskButton.setEnabled(!isExcute);
		loginButton.setEnabled(!isExcute);
		if (isExcute) {
			excuteButton.setText("停止");
		} else {
			excuteButton.setText("开始执行");
			TaskResultStatistic t = TaskResultStatistic.getInstance();
			t.clear();
			OrderTask.getTaskFinishFlag().set(true);
			for (Thread taskThread : taskThreadList) {
				try {
					taskThread.join();
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if (viewThread != null) {
				try {
					viewThread.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			if (noticeThread != null) {
				try {
					noticeThread.join();
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			while (true) {
				try {
					ViewTask.msgQueue.remove();
				} catch (NoSuchElementException e2) {
					break;
				}
			}
			taskThreadList.clear();
			taskBeans.clear();
			int rowCount = table.getRowCount();
			while (rowCount > 0) {
				dtm.removeRow(0);
				rowCount--;
			}
			table.validate();
		}
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		skuComboBoxMap = Maps.newHashMap();
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 1200, 600);
		contentPane = new JPanel();
		contentPane.setForeground(Color.DARK_GRAY);
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);

		// 登录框begin
		JPanel loginPane = new JPanel();
		loginPane.setBorder(new LineBorder(new Color(0, 0, 0)));
		loginPane.setBounds(10, 10, 291, 154);
		contentPane.add(loginPane);
		loginPane.setLayout(null);

		JLabel loginLabel = new JLabel("登录信息");
		loginLabel.setFont(new Font("宋体", Font.PLAIN, 14));
		loginLabel.setBounds(10, 10, 68, 26);
		loginPane.add(loginLabel);
		loginPane.setLayout(null);

		phoneNoLabel = new JLabel("手机号");
		phoneNoLabel.setBounds(10, 46, 46, 15);
		loginPane.add(phoneNoLabel);

		phoneNoField = new JTextField();
		phoneNoField.setBounds(76, 43, 101, 21);
		loginPane.add(phoneNoField);
		phoneNoField.setColumns(10);

		sessionLoginButton = new JButton("用密码登录");
		sessionLoginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				try {
					File file = new File("config.txt");
					BufferedReader reader = null;
					reader = new BufferedReader(new FileReader(file));
					String tempString = null;
					int line = 1;
					// 一次读入一行，直到读入null为文件结束
					while ((tempString = reader.readLine()) != null) {
						String userName = tempString.split("=")[0];
						String passwd = tempString.split("=")[1];
						TeshehuiServiceImpl teshehuiService = new TeshehuiServiceImpl();
						TeshehuiSessionManager teshehuiSessionManager = (TeshehuiSessionManager) SpringContextUtils
								.getContext().getBean("teshehuiSessionManager");
						ReturnResultBean returnBean = teshehuiService.loginByPasswd(userName, passwd);
						if (returnBean.getResultCode() != 0) {
							JOptionPane.showMessageDialog(loginPane, "第" + line + "行登录失败");
							reader.close();
							teshehuiSessionManager.clear();
							return;
						}
						teshehuiSessionManager.addSession(userName, (TeshehuiSession) returnBean.getReturnObj());
						line++;
						if (line == 3) {
							reader.close();
							break;
						}
					}
					reader.close();
				} catch (Exception e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(loginPane, "登录失败");
					return;
				}
				TeshehuiSessionManager manager = (TeshehuiSessionManager) SpringContextUtils.getContext()
						.getBean("teshehuiSessionManager");
				String text = "共登录会话" + manager.getAllSession().size() + "个，分别为：";
				for (String key : manager.getAllSession().keySet()) {
					text += key + " ";
				}
				addressLabel.setText(text);
				doLoginOrOut(true);
				urlField.requestFocus();
			}
		});

		sessionLoginButton.setBounds(105, 119, 176, 26);
		loginPane.add(sessionLoginButton);

		verifyLabel = new JLabel("验证码");
		verifyLabel.setBounds(10, 84, 46, 15);
		loginPane.add(verifyLabel);

		smsCodeField = new JTextField();
		smsCodeField.setColumns(10);
		smsCodeField.setBounds(76, 81, 101, 21);
		loginPane.add(smsCodeField);

		loginButton = new JButton("登录");
		loginButton.setBounds(10, 119, 68, 26);
		loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TeshehuiSessionManager teshehuiSessionManager = (TeshehuiSessionManager) SpringContextUtils.getContext()
						.getBean("teshehuiSessionManager");
				if (loginButton.getText().equals("登出")) {
					doLoginOrOut(false);
					teshehuiSessionManager.clear();
					return;
				}
				if (StringUtils.isEmpty(phoneNoField.getText())) {
					JOptionPane.showMessageDialog(loginPane, "手机号不能为空");
					return;
				}
				if (StringUtils.isEmpty(smsCodeField.getText())) {
					JOptionPane.showMessageDialog(loginPane, "短信验证码不能为空");
					return;
				}
				TeshehuiServiceImpl teshehuiService = new TeshehuiServiceImpl();
				ReturnResultBean returnBean = teshehuiService.login(phoneNoField.getText(), smsCodeField.getText());
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg());
					return;
				}
				TeshehuiSession session = (TeshehuiSession) returnBean.getReturnObj();
				teshehuiSessionManager.addSession(phoneNoField.getText(), session);
				returnBean = teshehuiService.getAddress();
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg() + " 请设置好地址后重新登录软件");
					return;
				}
				TeshehuiSession teshehuiSession = teshehuiSessionManager.getAllSession().get(phoneNoField.getText());
				addressLabel.setText("共登录会话" + teshehuiSessionManager.getAllSession().size() + "个，当前会话："
						+ (teshehuiSession.getUserBean().getNickName() == null ? ""
								: teshehuiSession.getUserBean().getNickName())
						+ " 电话:" + teshehuiSession.getUserBean().getMobilePhone() + ", 地址:"
						+ teshehuiSession.getUserBean().getAddressDetail());
				doLoginOrOut(true);
				urlField.requestFocus();
			}
		});
		loginPane.add(loginButton);

		addressLabel = new JTextPane();
		addressLabel.setBounds(10, 36, 271, 73);
		addressLabel.setVisible(false);
		loginPane.add(addressLabel);

		getVerfiyButton = new JButton("获取验证码");
		getVerfiyButton.setBounds(187, 42, 94, 57);
		loginPane.add(getVerfiyButton);
		getVerfiyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (StringUtils.isEmpty(phoneNoField.getText())) {
					JOptionPane.showMessageDialog(loginPane, "手机号不能为空");
					return;
				}
				vImgJDialog = new VerifyImgCodeJDialog(MainFrame.this, phoneNoField.getText());
				vImgJDialog.setVisible(true);
				if (StringUtils.isEmpty(vImgJDialog.getVerifyImgCode())) {
					return;
				}
				TeshehuiServiceImpl teshehuiService = new TeshehuiServiceImpl();
				ReturnResultBean returnBean = teshehuiService.getLoginSmsCode(phoneNoField.getText(),
						vImgJDialog.getVerifyImgCode());
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg());
				}
			}
		});
		// 登录框end
		// 商品选择框begin
		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setBounds(334, 10, 834, 154);
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblNewLabel_2 = new JLabel("购买商品的url地址");
		lblNewLabel_2.setBounds(14, 16, 201, 18);
		panel_2.add(lblNewLabel_2);

		taskBeans = Lists.newArrayList();
		urlField = new JTextField();
		urlField.setText("https://m.teshehui.com/goods/detail/070400430057");
		urlField.setBounds(229, 13, 524, 24);
		panel_2.add(urlField);
		urlField.setColumns(10);
		urlField.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				TeshehuiSessionManager teshehuiSessionManager = (TeshehuiSessionManager) SpringContextUtils.getContext()
						.getBean("teshehuiSessionManager");
				Map<String, TeshehuiSession> sessionMap = teshehuiSessionManager.getAllSession();
				Set<String> userNameSet = sessionMap.keySet();
				String userName = "";
				for (String str : userNameSet) {
					userName = str;
					break;
				}
				if (!userName.equals("")) {
					TeshehuiServiceImpl teshehuiService = new TeshehuiServiceImpl(sessionMap.get(userName));
					ReturnResultBean returnBean = teshehuiService.getProductStockInfo(urlField.getText());
					if (returnBean.getResultCode() == 0) {
						skuComboBox.removeAllItems();
						List<SkuBean> skuList = (List<SkuBean>) returnBean.getReturnObj();
						for (SkuBean bean : skuList) {
							skuComboBoxMap.put(bean.getAttrValue() + " " + bean.getProductName(), bean);
							productNameLabel.setText(bean.getProductName());
							skuComboBox.addItem(bean.getAttrValue());
						}
					} else if (returnBean.getResultCode() == 7777) {
						returnBean = teshehuiService.getCheckCode();
						if (returnBean.getResultCode() == 0) {
							vImgJDialog2 = new VerifyImgCodeJDialog2(MainFrame.this,
									(byte[]) returnBean.getReturnObj());
							vImgJDialog2.setVisible(true);
							if (StringUtils.isEmpty(vImgJDialog2.getVerifyImgCode())) {
								return;
							}
							returnBean = teshehuiService.checkCode(vImgJDialog2.getVerifyImgCode(), "1");
							if (returnBean.getResultCode() != 0) {
								JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg());
							}
							urlField.requestFocus();
						} else {
							JOptionPane.showMessageDialog(panel_2, returnBean.getReturnMsg());
							urlField.requestFocus();
						}
					} else {
						JOptionPane.showMessageDialog(panel_2, "请确认url是否正确");
						urlField.requestFocus();
					}
				} else {
					JOptionPane.showMessageDialog(panel_2, "请登录");
				}
			}
		});

		productNameLabel = new JLabel("选择要购买的商品");
		productNameLabel.setBounds(14, 59, 187, 18);
		panel_2.add(productNameLabel);
		skuComboBox = new JComboBox();
		skuComboBox.setBounds(229, 58, 194, 21);
		panel_2.add(skuComboBox);

		NumberFormat nf = NumberFormat.getIntegerInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		JLabel lblNewLabel_4 = new JLabel("任务执行时间（格式按样例）");
		lblNewLabel_4.setBounds(14, 105, 201, 18);
		panel_2.add(lblNewLabel_4);

		Date now = new Date();
		String strBeginTime = new SimpleDateFormat("yyyy-MM-dd").format(now) + " 15:00:00";
		Date beginTime = null;
		try {
			beginTime = df.parse(strBeginTime);
		} catch (ParseException e3) {
			// TODO Auto-generated catch block
			e3.printStackTrace();
		}
		Date endTime = new Date(beginTime.getTime() + 1000 * 60 * 15L);

		JFormattedTextField formattedTextField = new JFormattedTextField(df);
		formattedTextField.setBounds(229, 99, 173, 24);
		formattedTextField.setValue(beginTime);
		panel_2.add(formattedTextField);

		JLabel label_1 = new JLabel("到");
		label_1.setBounds(416, 105, 15, 18);
		panel_2.add(label_1);

		JFormattedTextField formattedTextField_1 = new JFormattedTextField(df);
		formattedTextField_1.setBounds(445, 99, 173, 24);
		formattedTextField_1.setValue(endTime);
		panel_2.add(formattedTextField_1);

		JLabel label_2 = new JLabel("购买个数");
		label_2.setBounds(588, 61, 56, 18);
		panel_2.add(label_2);

		JFormattedTextField formattedTextField_4 = new JFormattedTextField(nf);
		formattedTextField_4.setBounds(644, 59, 32, 24);
		formattedTextField_4.setValue(1);
		// formattedTextField_4.setEditable(false);
		panel_2.add(formattedTextField_4);

		JLabel lblNewLabel_6 = new JLabel("轮询时间");
		lblNewLabel_6.setBounds(445, 63, 56, 15);
		panel_2.add(lblNewLabel_6);

		lunxuTime = new JFormattedTextField();
		lunxuTime.setBounds(511, 60, 32, 21);
		lunxuTime.setText("3");
		// lunxuTime.setEditable(false);
		panel_2.add(lunxuTime);

		excuteButton = new JButton("开始执行");
		excuteButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				boolean runFlag = false;
				if (excuteButton.getText().equals("开始执行")) {
					if (table.getRowCount() <= 0) {
						JOptionPane.showMessageDialog(panel_2, "请先添加任务");
						return;
					}
					if (!loginButton.getText().equals("登出")) {
						JOptionPane.showMessageDialog(panel_2, "请先登录");
						return;
					}
					OrderTask.sleepTime = Long
							.valueOf(StringUtils.isEmpty(lunxuTime.getText()) ? "1" : lunxuTime.getText()) * 1000;
					OrderTask.getTaskFinishFlag().set(false);
					Date beginTime = (Date) formattedTextField.getValue();
					Date endTime = (Date) formattedTextField_1.getValue();
					int i = 0;
					String num = formattedTextField_4.getText();
					for (SkuBean sku : taskBeans) {
						TeshehuiSessionManager teshehuiSessionManager = (TeshehuiSessionManager) SpringContextUtils
								.getContext().getBean("teshehuiSessionManager");
						Map<String, TeshehuiSession> sessionMap = teshehuiSessionManager.getAllSession();
						for (String userName : sessionMap.keySet()) {
							OrderTask task = new OrderTask(beginTime, endTime, sku, num, i, sessionMap.get(userName));
							Thread taskThread = new Thread(task, "用户工作任务-" + i);
							taskThreadList.add(taskThread);
							taskThread.start();
						}
						i++;
					}
					ViewTask viewTask = new ViewTask(table, dtm);
					viewThread = new Thread(viewTask, "下单结果显示任务");
					viewThread.start();
					noticeThread = new Thread(new NoticeTask(), "通知任务");
					noticeThread.start();
					runFlag = true;
				}
				doExcute(runFlag);
			}
		});
		excuteButton.setBounds(707, 99, 113, 27);
		panel_2.add(excuteButton);

		addTaskButton = new JButton("添加进任务");
		addTaskButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (skuComboBox.getSelectedItem() != null
						&& !StringUtils.isEmpty((String) skuComboBox.getSelectedItem())) {
					String key = (String) skuComboBox.getSelectedItem() + " " + productNameLabel.getText();
					String row[] = { key, "", "", "" };
					dtm.addRow(row);
					taskBeans.add(skuComboBoxMap.get(key));
				} else {
					JOptionPane.showMessageDialog(panel_2, "无法添加任务，请确认url正确");
				}
			}
		});
		addTaskButton.setBounds(707, 57, 113, 27);
		panel_2.add(addTaskButton);
		// 商品选择框end

		JPanel panel_3 = new JPanel(new BorderLayout());
		panel_3.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setBounds(10, 191, 1158, 254);
		contentPane.add(panel_3);

		String[] columnNames = { "任务名", "时间", "是否成功", "执行描述" };
		dtm = new DefaultTableModel(columnNames, 0);
		table = new JTable(dtm);
		table.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		table.setShowGrid(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(420);
		table.getColumnModel().getColumn(0).setMaxWidth(460);
		table.getColumnModel().getColumn(0).sizeWidthToFit();
		table.getColumnModel().getColumn(1).setPreferredWidth(120);
		table.getColumnModel().getColumn(1).setMaxWidth(160);
		table.getColumnModel().getColumn(1).sizeWidthToFit();
		table.getColumnModel().getColumn(2).setPreferredWidth(120);
		table.getColumnModel().getColumn(2).setMaxWidth(160);
		table.getColumnModel().getColumn(2).sizeWidthToFit();
		table.getColumnModel().getColumn(3).sizeWidthToFit();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		panel_3.add(table.getTableHeader(), BorderLayout.NORTH);
		panel_3.add(table, BorderLayout.CENTER);

		ydmUserNmae = new JTextField();
		ydmUserNmae.setBounds(21, 469, 106, 21);
		contentPane.add(ydmUserNmae);
		ydmUserNmae.setColumns(10);

		ydmPasswd = new JTextField();
		ydmPasswd.setBounds(21, 515, 106, 21);
		contentPane.add(ydmPasswd);
		ydmPasswd.setColumns(10);

		JButton btnNewButton = new JButton("登录云打码");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				TeshehuiServiceImpl tshService = new TeshehuiServiceImpl();
				ReturnResultBean bean = tshService.loginYDM(ydmUserNmae.getText(), ydmPasswd.getText());
				if (bean.getResultCode() == 0) {
					ydmMsg.setVisible(true);
					ydmMsg.setText("云打码登录成功，用户为" + ydmUserNmae.getText());
					ydmUserNmae.setVisible(false);
					ydmUserNmae.setEditable(false);
					ydmPasswd.setVisible(false);
					ydmPasswd.setEditable(false);
				} else {
					JOptionPane.showMessageDialog(panel_2, bean.getReturnMsg());
				}
			}
		});
		btnNewButton.setBounds(146, 468, 113, 23);
		contentPane.add(btnNewButton);

		ydmMsg = new JTextPane();
		ydmMsg.setBounds(21, 469, 106, 67);
		contentPane.add(ydmMsg);
		ydmMsg.setVisible(false);
	}
}
