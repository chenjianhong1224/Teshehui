package com.cjh.teshehui.swing;

import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.BoxLayout;
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
import org.springframework.util.StringUtils;

import com.cjh.teshehui.swing.bean.ReturnResultBean;
import com.cjh.teshehui.swing.service.TeshehuiService;
import com.cjh.teshehui.swing.session.TeshehuiSession;
import com.cjh.teshehui.swing.utils.SpringContextUtils;
import com.google.common.collect.Lists;
import com.google.zxing.WriterException;

import java.awt.Color;
import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JPasswordField;
import javax.swing.JButton;
import javax.swing.JTextPane;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
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

@SpringBootApplication
public class MainFrame extends JFrame {

	private JPanel contentPane;
	private JTextField phoneNoField;
	private VerifyImgCodeJDialog vImgJDialog;
	private JLabel addressLabel;
	private JLabel phoneNoLabel;
	private JLabel verifyLabel;
	private TeshehuiService teshehuiService;
	private JButton getVerfiyButton;
	private JButton loginButton;

	private JTextField textField;
	private JTable table;
	private DefaultTableModel dtm = null;
	private JTextArea authCode;
	private JTextArea proxyPlan;
	private JCheckBox chckbxNewCheckBox_1;
	private JFormattedTextField lunxuTime;
	private JTextField smsCodeField;

	/**
	 * Launch the application.
	 * 
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
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
		if (isLogin) {
			loginButton.setText("登出");
		} else {
			loginButton.setText("登录");
		}
	}

	/**
	 * Create the frame.
	 */
	public MainFrame() {
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
		loginPane.setBounds(10, 10, 228, 154);
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
		phoneNoField.setBounds(76, 43, 142, 21);
		loginPane.add(phoneNoField);
		phoneNoField.setColumns(10);

		getVerfiyButton = new JButton("获取验证码");
		getVerfiyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (StringUtils.isEmpty(phoneNoField.getText())) {
					JOptionPane.showMessageDialog(loginPane, "手机号不能为空");
					return;
				}
				vImgJDialog = new VerifyImgCodeJDialog(MainFrame.this, phoneNoField.getText());
				vImgJDialog.setVisible(true);
				teshehuiService = (TeshehuiService) SpringContextUtils.getContext().getBean("teshehuiServiceImpl");
				ReturnResultBean returnBean = teshehuiService.getLoginSmsCode(phoneNoField.getText(),
						vImgJDialog.getVerifyImgCode());
				if (StringUtils.isEmpty(vImgJDialog.getVerifyImgCode())) {
					return;
				}
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg());
				}
			}
		});
		getVerfiyButton.setBounds(125, 119, 93, 26);
		loginPane.add(getVerfiyButton);

		verifyLabel = new JLabel("验证码");
		verifyLabel.setBounds(10, 84, 46, 15);
		loginPane.add(verifyLabel);

		smsCodeField = new JTextField();
		smsCodeField.setColumns(10);
		smsCodeField.setBounds(76, 81, 142, 21);
		loginPane.add(smsCodeField);

		loginButton = new JButton("登录");
		loginButton.setBounds(6, 119, 93, 26);
		loginButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (loginButton.getText().equals("登出")) {
					doLoginOrOut(false);
					TeshehuiSession teshehuiSession = (TeshehuiSession) SpringContextUtils.getContext()
							.getBean("teshehuiSession");
					teshehuiSession.cleanSession();
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
				teshehuiService = (TeshehuiService) SpringContextUtils.getContext().getBean("teshehuiServiceImpl");
				ReturnResultBean returnBean = teshehuiService.login(phoneNoField.getText(), smsCodeField.getText());
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg());
					return;
				}
				returnBean = teshehuiService.getAddress();
				if (returnBean.getResultCode() != 0) {
					JOptionPane.showMessageDialog(loginPane, returnBean.getReturnMsg() + " 请设置好地址后重新登录软件");
					return;
				}
				doLoginOrOut(true);
			}
		});
		loginPane.add(loginButton);

		addressLabel = new JLabel("");
		addressLabel.setBounds(10, 36, 208, 73);
		addressLabel.setVisible(false);
		loginPane.add(addressLabel);
		// 登录框end

		JPanel panel_2 = new JPanel();
		panel_2.setBorder(new LineBorder(new Color(0, 0, 0)));
		panel_2.setBounds(259, 10, 909, 154);
		contentPane.add(panel_2);
		panel_2.setLayout(null);

		JLabel lblNewLabel_5 = new JLabel("");
		lblNewLabel_5.setBounds(646, 105, 110, 18);
		panel_2.add(lblNewLabel_5);

		JLabel lblNewLabel_2 = new JLabel("购买商品的url地址");
		lblNewLabel_2.setBounds(14, 16, 201, 18);
		panel_2.add(lblNewLabel_2);

		textField = new JTextField();
		textField.setText(
				"https://www.emaotai.cn/smartsales-b2c-web-pc/details/1180731799924468740-1173773178264259584.html?skuId=1180731799931808771");

		textField.setBounds(229, 13, 524, 24);
		panel_2.add(textField);
		textField.setColumns(10);

		JLabel lblNewLabel_3 = new JLabel("购买的商品是否是预购模式");
		lblNewLabel_3.setBounds(14, 59, 201, 18);
		panel_2.add(lblNewLabel_3);

		JCheckBox chckbxNewCheckBox = new JCheckBox("是");
		chckbxNewCheckBox.setSelected(true);
		chckbxNewCheckBox.setBounds(225, 55, 63, 27);
		panel_2.add(chckbxNewCheckBox);

		NumberFormat nf = NumberFormat.getIntegerInstance();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		// -------------
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
		Date endTime = new Date(beginTime.getTime() + 1000 * 60 * 5L);

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
		// -------------

		// -------------
		JLabel label_2 = new JLabel("购买个数");
		label_2.setBounds(447, 59, 56, 18);
		panel_2.add(label_2);

		JFormattedTextField formattedTextField_4 = new JFormattedTextField(nf);
		formattedTextField_4.setBounds(503, 57, 32, 24);
		formattedTextField_4.setValue(1);
		panel_2.add(formattedTextField_4);
		// -------------

		// -------------
		JLabel label_3 = new JLabel("并发执行任务个数");
		label_3.setBounds(560, 59, 128, 18);
		panel_2.add(label_3);

		JFormattedTextField bingfaNum = new JFormattedTextField(nf);
		bingfaNum.setEditable(false);
		bingfaNum.setBounds(697, 56, 56, 24);
		bingfaNum.setValue(1);
		panel_2.add(bingfaNum);
		// -------------

		JButton btnNewButton_1 = new JButton("开始执行");

		btnNewButton_1.setBounds(782, 12, 113, 111);
		panel_2.add(btnNewButton_1);

		JLabel lblNewLabel_6 = new JLabel("轮询时间");
		lblNewLabel_6.setBounds(304, 61, 56, 15);
		panel_2.add(lblNewLabel_6);

		lunxuTime = new JFormattedTextField();
		lunxuTime.setBounds(370, 58, 32, 21);
		lunxuTime.setText("10");
		panel_2.add(lunxuTime);

		JPanel panel_3 = new JPanel(new BorderLayout());
		panel_3.setBorder(new TitledBorder(null, "", TitledBorder.LEADING, TitledBorder.TOP, null, null));
		panel_3.setBounds(10, 191, 1158, 154);
		contentPane.add(panel_3);

		String[] columnNames = { "任务号", "时间", "执行描述" };
		dtm = new DefaultTableModel(columnNames, 0);
		table = new JTable(dtm);
		table.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		table.setShowGrid(false);
		table.getColumnModel().getColumn(0).setPreferredWidth(120);
		table.getColumnModel().getColumn(0).setMaxWidth(160);
		table.getColumnModel().getColumn(0).sizeWidthToFit();
		table.getColumnModel().getColumn(1).setPreferredWidth(120);
		table.getColumnModel().getColumn(1).setMaxWidth(160);
		table.getColumnModel().getColumn(1).sizeWidthToFit();
		table.getColumnModel().getColumn(2).sizeWidthToFit();

		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		panel_3.add(table.getTableHeader(), BorderLayout.NORTH);
		panel_3.add(table, BorderLayout.CENTER);
	}
}
