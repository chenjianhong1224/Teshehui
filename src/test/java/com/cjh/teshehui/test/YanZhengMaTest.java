package com.cjh.teshehui.test;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

import org.junit.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.cjh.teshehui.swing.utils.ImgUtils;
import com.github.jaiimageio.stream.FileChannelImageOutputStream;

import net.sourceforge.tess4j.ITesseract;
import net.sourceforge.tess4j.Tesseract;
import net.sourceforge.tess4j.TesseractException;

@SpringBootTest
public class YanZhengMaTest {
	@Test
	public void Tess4JTest() {
		for (int i = 1; i <= 10; i++) {
			String ocrResult = "";
			String url = "https://m.teshehui.com/user/echo_image?phone=18587957108";
			try {
				ITesseract instance = new Tesseract();
				BufferedImage image = ImageIO.read(new URL(url));
				File imgDir = new File("D:\\training\\sample\\in_" + i + ".jpg");
				ImageIO.write(image, "jpg", imgDir);
				// ocrResult = instance.doOCR(imgDir);
				// System.out.println("ocrResult=" + ocrResult);
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	@Test
	public void grayImage() throws IOException {
		for (int i = 1; i <= 500; i++) {
			String OriginalImg = "D:\\training\\sample\\" + i + ".TIFF";
			BufferedImage srcImg = ImageIO.read(new File(OriginalImg));
			int iw = srcImg.getWidth();
			int ih = srcImg.getHeight();
			Graphics2D srcG = srcImg.createGraphics();
			RenderingHints rhs = srcG.getRenderingHints();
			ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_GRAY);
			ColorConvertOp theOp = new ColorConvertOp(cs, rhs);
			BufferedImage dstImg = new BufferedImage(iw, ih, BufferedImage.TYPE_INT_RGB);
			theOp.filter(srcImg, dstImg);
			String ocrResult = "D:\\training\\process_sample\\" + i + ".TIFF";
			ImageIO.write(dstImg, "TIFF", new File(ocrResult));
		}
	}

	@Test
	public void preHandle() throws IOException {
		for (int i = 1; i <= 1; i++) {
			String OriginalImg = "D:\\training\\sample\\" + i + ".TIFF";
			BufferedImage srcImg = ImageIO.read(new File(OriginalImg));
			preHandleImage(srcImg, i);
		}
	}

	@Test
	public void removeBackgroud() throws Exception {
		for (int i = 1; i <= 1; i++) {
			String OriginalImg = "D:\\training\\sample\\" + i + ".TIFF";
			BufferedImage resultImg = removeBackgroud(OriginalImg);
			String ocrResult = "D:\\training\\process_sample\\" + i + ".TIFF";
			File imgDir = new File(ocrResult);
			ImageIO.write(resultImg, "TIFF", imgDir);
		}
	}

	@Test
	public void Test() {
		for (int i = 1; i <= 500; i++) {
			// 原始验证码地址
			String OriginalImg = "D:\\training\\sample\\" + i + ".TIFF";
			// 识别样本输出地址
			String ocrResult = "D:\\training\\process_sample\\" + i + ".TIFF";
			// 去噪点
			removeBackground(OriginalImg, ocrResult);
			// 裁剪边角
			// cuttingImg(ocrResult);
			// OCR识别
			// ITesseract instance = new Tesseract();
			File imgDir = new File(ocrResult);
			// String code = "";
			// try {
			// code = instance.doOCR(imgDir);
			// } catch (TesseractException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// }
		}

	}

	public static void cuttingImg(String imgUrl) {
		try {
			File newfile = new File(imgUrl);
			BufferedImage bufferedimage = ImageIO.read(newfile);
			int width = bufferedimage.getWidth();
			int height = bufferedimage.getHeight();
			if (width > 52) {
				bufferedimage = ImgUtils.cropImage(bufferedimage, (int) ((width - 52) / 2), 0,
						(int) (width - (width - 52) / 2), (int) (height));
				if (height > 16) {
					bufferedimage = ImgUtils.cropImage(bufferedimage, 0, (int) ((height - 16) / 2), 52,
							(int) (height - (height - 16) / 2));
				}
			} else {
				if (height > 16) {
					bufferedimage = ImgUtils.cropImage(bufferedimage, 0, (int) ((height - 16) / 2), (int) (width),
							(int) (height - (height - 16) / 2));
				}
			}
			ImageIO.write(bufferedimage, "jpg", new File(imgUrl));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void removeBackground(String imgUrl, String resUrl) {
		// 定义一个临界阈值
		int threshold = 300;
		try {
			BufferedImage img = ImageIO.read(new File(imgUrl));
			int width = img.getWidth();
			int height = img.getHeight();
			for (int i = 1; i < width; i++) {
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						Color color = new Color(img.getRGB(x, y));
						System.out.println("red:" + color.getRed() + " | green:" + color.getGreen() + " | blue:"
								+ color.getBlue());
						int num = color.getRed() + color.getGreen() + color.getBlue();
						if (num >= threshold) {
							img.setRGB(x, y, Color.WHITE.getRGB());
						}
					}
				}
			}
			for (int i = 1; i < width; i++) {
				Color color1 = new Color(img.getRGB(i, 1));
				int num1 = color1.getRed() + color1.getGreen() + color1.getBlue();
				for (int x = 0; x < width; x++) {
					for (int y = 0; y < height; y++) {
						Color color = new Color(img.getRGB(x, y));

						int num = color.getRed() + color.getGreen() + color.getBlue();
						if (num == num1) {
							img.setRGB(x, y, Color.BLACK.getRGB());
						} else {
							img.setRGB(x, y, Color.WHITE.getRGB());
						}
					}
				}
			}
			File file = new File(resUrl);
			// if (!file.exists()) {
			// File dir = file.getParentFile();
			// if (!dir.exists()) {
			// dir.mkdirs();
			// }
			// try {
			// file.createNewFile();
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
			// }
			ImageIO.write(img, "TIFF", file);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void preHandleImage(BufferedImage bufferedImage, int i) throws IOException {
		int h = bufferedImage.getHeight();
		int w = bufferedImage.getWidth();
		// 灰度化
		int[][] gray = new int[w][h];
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int argb = bufferedImage.getRGB(x, y);
				// 图像加亮（调整亮度识别率非常高）
				int r = (int) (((argb >> 16) & 0xFF) * 1.1 + 30);
				int g = (int) (((argb >> 8) & 0xFF) * 1.1 + 30);
				int b = (int) (((argb >> 0) & 0xFF) * 1.1 + 30);
				if (r >= 255) {
					r = 255;
				}
				if (g >= 255) {
					g = 255;
				}
				if (b >= 255) {
					b = 255;
				}
				gray[x][y] = (int) Math.pow(
						(Math.pow(r, 2.2) * 0.2973 + Math.pow(g, 2.2) * 0.6274 + Math.pow(b, 2.2) * 0.0753), 1 / 2.2);
			}
		}
		// 二值化
		int threshold = ostu(gray, w, h);
		BufferedImage binaryBufferedImage = new BufferedImage(w, h, BufferedImage.TYPE_BYTE_BINARY);
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				if (gray[x][y] > threshold) {
					gray[x][y] |= 0x00FFFF;
				} else {
					gray[x][y] &= 0xFF0000;
				}
				binaryBufferedImage.setRGB(x, y, gray[x][y]);
			}
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		String ocrResult = "D:\\training\\process_sample\\" + i + ".TIFF";
		ImageIO.write(binaryBufferedImage, "TIFF", new File(ocrResult));
	}

	private static int ostu(int[][] gray, int w, int h) {
		int[] histData = new int[w * h];
		// Calculate histogram
		for (int x = 0; x < w; x++) {
			for (int y = 0; y < h; y++) {
				int red = 0xFF & gray[x][y];
				histData[red]++;
			}
		}
		// Total number of pixels
		int total = w * h;
		float sum = 0;
		for (int t = 0; t < 256; t++)
			sum += t * histData[t];
		float sumB = 0;
		int wB = 0;
		int wF = 0;
		float varMax = 0;
		int threshold = 0;
		for (int t = 0; t < 256; t++) {
			wB += histData[t]; // Weight Background
			if (wB == 0)
				continue;
			wF = total - wB; // Weight Foreground
			if (wF == 0)
				break;
			sumB += (float) (t * histData[t]);
			float mB = sumB / wB; // Mean Background
			float mF = (sum - sumB) / wF; // Mean Foreground
			// Calculate Between Class Variance
			float varBetween = (float) wB * (float) wF * (mB - mF) * (mB - mF);
			// Check if new maximum found
			if (varBetween > varMax) {
				varMax = varBetween;
				threshold = t;
			}
		}
		return threshold;
	}

	public static BufferedImage removeBackgroud(String picFile) throws Exception {
		BufferedImage img = ImageIO.read(new File(picFile));
		int width = img.getWidth();
		int height = img.getHeight();
		double subWidth = (double) width / 4.0;
		for (int i = 0; i < 4; i++) {
			Map<Integer, Integer> map = new HashMap<Integer, Integer>();
			for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth && x < width - 1; ++x) {
				for (int y = 0; y < height; ++y) {
					Color color = new Color(img.getRGB(x, y));
					System.out.println(color.getRed() + " " + color.getGreen() + " " + color.getBlue());
					if (isWhiteColor(img.getRGB(x, y)))
						continue;
					if (map.containsKey(img.getRGB(x, y))) {
						map.put(img.getRGB(x, y), map.get(img.getRGB(x, y)) + 1);
					} else {
						map.put(img.getRGB(x, y), 1);
					}
				}
			}
			int max = 0;
			int colorMax = 0;
			for (Integer color : map.keySet()) {
				if (max < map.get(color)) {
					max = map.get(color);
					colorMax = color;
				}
			}
			for (int x = (int) (1 + i * subWidth); x < (i + 1) * subWidth && x < width - 1; ++x) {
				for (int y = 0; y < height; ++y) {
					if (img.getRGB(x, y) != colorMax) {
						img.setRGB(x, y, Color.WHITE.getRGB());
					} else {
						img.setRGB(x, y, Color.BLACK.getRGB());
					}
				}
			}
		}
		return img;
	}

	private static boolean isWhiteColor(BufferedImage image, int x, int y) throws Exception {
		if (x < 0 || y < 0)
			return true;
		if (x >= image.getWidth() || y >= image.getHeight())
			return true;

		Color color = new Color(image.getRGB(x, y));
		if (color.getRed() >= 239 && color.getBlue() >= 239 && color.getGreen() >= 239) {
			return true;
		}
		return false;
	}

	private static boolean isWhiteColor(int colors) throws Exception {
		Color color = new Color(colors);
		if (color.getRed() >= 239 && color.getBlue() >= 239 && color.getGreen() >= 239) {
			return true;
		}
		return false;
	}

	private static boolean isWhite(int colors) {
		Color color = new Color(colors);
		return color.equals(Color.WHITE) ? true : false;
	}
}
