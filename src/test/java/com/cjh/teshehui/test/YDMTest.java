package com.cjh.teshehui.test;

import com.sun.jna.Library;
import com.sun.jna.Native;

public class YDMTest {

	// 下载云打码DLL http://yundama.com/apidoc/YDM_SDK.html#DLL
	// yundamaAPI 32位, yundamaAPI-x64 64位
	public static String DLLPATH = "D:\\云打码\\Java调用示例\\yundamaAPI-x64.dll";

	public interface YDM1 extends Library {
		YDM1 INSTANCE = (YDM1) Native.loadLibrary(DLLPATH, YDM1.class);

		public void YDM_SetBaseAPI(String lpBaseAPI);

		public void YDM_SetAppInfo(int nAppId, String lpAppKey);

		public int YDM_Login(String lpUserName, String lpPassWord);

		public int YDM_DecodeByPath(String lpFilePath, int nCodeType, byte[] pCodeResult);

		public int YDM_UploadByPath(String lpFilePath, int nCodeType);

		public int YDM_EasyDecodeByPath(String lpUserName, String lpPassWord, int nAppId, String lpAppKey,
				String lpFilePath, int nCodeType, int nTimeOut, byte[] pCodeResult);

		public int YDM_DecodeByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, byte[] pCodeResult);

		public int YDM_UploadByBytes(byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType);

		public int YDM_EasyDecodeByBytes(String lpUserName, String lpPassWord, int nAppId, String lpAppKey,
				byte[] lpBuffer, int nNumberOfBytesToRead, int nCodeType, int nTimeOut, byte[] pCodeResult);

		public int YDM_GetResult(int nCaptchaId, byte[] pCodeResult);

		public int YDM_Report(int nCaptchaId, boolean bCorrect);

		public int YDM_EasyReport(String lpUserName, String lpPassWord, int nAppId, String lpAppKey, int nCaptchaId,
				boolean bCorrect);

		public int YDM_GetBalance(String lpUserName, String lpPassWord);

		public int YDM_EasyGetBalance(String lpUserName, String lpPassWord, int nAppId, String lpAppKey);

		public int YDM_SetTimeOut(int nTimeOut);

		public int YDM_Reg(String lpUserName, String lpPassWord, String lpEmail, String lpMobile, String lpQQUin);

		public int YDM_EasyReg(int nAppId, String lpAppKey, String lpUserName, String lpPassWord, String lpEmail,
				String lpMobile, String lpQQUin);

		public int YDM_Pay(String lpUserName, String lpPassWord, String lpCard);

		public int YDM_EasyPay(String lpUserName, String lpPassWord, long nAppId, String lpAppKey, String lpCard);
	}

	public static void main(String[] args) throws Exception {
		// 注意这里是普通会员账号，不是开发者账号，注册地址 http://www.yundama.com/index/reg/user
		// 开发者可以联系客服领取免费调试题分
		String username = "cjh_k1";
		String password = "fld10263";

		// 测试时可直接使用默认的软件ID密钥，但要享受开发者分成必须使用自己的软件ID和密钥
		// 1. http://www.yundama.com/index/reg/developer 注册开发者账号
		// 2. http://www.yundama.com/developer/myapp 添加新软件
		// 3. 使用添加的软件ID和密钥进行开发，享受丰厚分成
		int appid = 6504;
		String appkey = "ec22306220a76668b75a32bab669ff43";

		// 图片路径
		String imagepath = "D:\\training\\sample\\in_1.jpg";

		// 例：1004表示4位字母数字，不同类型收费不同。请准确填写，否则影响识别率。在此查询所有类型
		// http://www.yundama.com/price.html
		int codetype = 1004;

		// 只需要在初始的时候登陆一次
		int uid = 0;
		YDM1.INSTANCE.YDM_SetAppInfo(appid, appkey); // 设置软件ID和密钥
		uid = YDM1.INSTANCE.YDM_Login(username, password); // 登陆到云打码

		if (uid > 0) {
			System.out.println("登陆成功,正在提交识别...");

			byte[] byteResult = new byte[30];
			int cid = YDM1.INSTANCE.YDM_DecodeByPath(imagepath, codetype, byteResult);
			String strResult = new String(byteResult, "UTF-8").trim();

			// 返回其他错误代码请查询 http://www.yundama.com/apidoc/YDM_ErrorCode.html
			System.out.println("识别返回代码:" + cid);
			System.out.println("识别返回结果:" + strResult);

		} else {
			System.out.println("登录失败，错误代码为：" + uid);
		}
	}
}
