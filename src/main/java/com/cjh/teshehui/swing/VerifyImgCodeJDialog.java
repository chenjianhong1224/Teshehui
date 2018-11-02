package com.cjh.teshehui.swing;

import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.MalformedURLException;
import java.net.URL;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

import org.springframework.util.StringUtils;

public class VerifyImgCodeJDialog extends JDialog {

	private JTextField verifyImgField;

	public String getVerifyImgCode() {
		return verifyImgField.getText();
	}

	public VerifyImgCodeJDialog(MainFrame frame, String phoneNo) { // 构造函数 可以不写
																	// 那个 void
		super(frame, "输入图片中的英文字母", true);// 实例化一个JDialog 类对象，指定对话框的父窗体 窗体标题 和类型
		Container container = getContentPane();
		container.setLayout(null);
		try {
			JLabel lblNewLabel = new JLabel("");
			lblNewLabel.setBounds(10, 10, 151, 36);
			container.add(lblNewLabel);
			ImageIcon icon = new ImageIcon(new URL("https://m.teshehui.com/user/echo_image?phone=" + phoneNo));
			lblNewLabel.setIcon(icon);
			icon.getImage().flush();
			verifyImgField = new JTextField();
			verifyImgField.setBounds(10, 56, 118, 21);
			container.add(verifyImgField);
			verifyImgField.setColumns(10);

			JButton btnNewButton = new JButton("发送");
			btnNewButton.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (StringUtils.isEmpty(verifyImgField.getText())) {
						JOptionPane.showMessageDialog(container, "图片校验码不能为空");
					} else {
						dispose();
					}
				}
			});
			btnNewButton.setBounds(10, 88, 93, 23);
			container.add(btnNewButton);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		setBounds(220, 220, 200, 160);
	}
}
