package com.ihidea.core.util;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Transparency;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.swing.ImageIcon;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

import sun.misc.BASE64Encoder;

import com.ihidea.core.support.exception.ServiceException;

/**
 * 图片操作
 * @author lingjun
 */
public class ImageUtilsEx {

	/**
	 * 旋转图片为指定角度
	 * @param bufferedimage 目标图像
	 * @param degree 旋转角度
	 * @return
	 */
	public static BufferedImage rotateImage(final BufferedImage bufferedimage, final int degree) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		// 确定旋转圆心
		int xRot = w / 2;
		int yRot = w / 2;
		if (degree > 0) {
			xRot = h / 2;
			yRot = h / 2;
		}
		int nw = w;
		int nh = h;
		if (Math.abs(degree) % 180 > 0) {
			nw = h;
			nh = w;
		} else {
			xRot = w / 2;
			yRot = h / 2;
		}

		int type = bufferedimage.getColorModel().getTransparency();
		BufferedImage img;
		Graphics2D graphics2d = (img = new BufferedImage(nw, nh, type)).createGraphics();

		AffineTransform origXform = graphics2d.getTransform();
		AffineTransform newXform = (AffineTransform) (origXform.clone());

		newXform.rotate(Math.toRadians(degree), xRot, yRot);

		graphics2d.setTransform(newXform);
		// draw image centered in panel
		graphics2d.drawImage(bufferedimage, 0, 0, null);
		// Reset to Original
		graphics2d.setTransform(origXform);
		graphics2d.dispose();

		return img;
	}

	/**
	 * 改变图片大小
	 * @param image
	 * @param w
	 * @param h
	 * @param imageFormat
	 * @return
	 * @throws Exception
	 */
	public static byte[] resizeImage(byte[] image, int w, int h, String imageFormat) throws Exception {

		// 判断: 是否安装MagickImage, 是否windows，是否linux
		// if (SystemUtilsEx.hasMagickImage()) {
		// return resizeImageMagickImage(image, w, h);
		// } else {

		return resizeImageDefault(new ByteArrayInputStream(image), w, h, imageFormat);
		// }
	}

	public static byte[] resizeImage(InputStream in, int w, int h, String imageFormat) throws Exception {
		return resizeImageDefault(in, w, h, StringUtils.isBlank(imageFormat) ? "JPG" : imageFormat);
	}

//	private static byte[] resizeImageMagickImage(byte[] image, int w, int h) throws Exception {
//
//		MagickImage magickImage = null;
//
//		byte[] result = null;
//
//		try {
//			magickImage = new MagickImage(new ImageInfo(), image);
//
//			// 得到原图尺寸
//			magickImage.getDimension();
//
//			// 如果等比例,直接返回
//			if (magickImage.getDimension().getWidth() == w && magickImage.getDimension().getHeight() == h) {
//				return image;
//			}
//
//			// 如果w或h有为0的，则进行等比例压缩
//			if (w == 0 || h == 0) {
//
//				if (w == 0 && h == 0) {
//					throw new ServiceException("图片压缩不能输入宽高都为0!");
//				}
//
//				if (w == 0) {
//					w = (int) ((double) magickImage.getDimension().getWidth() * (double) h / (double) magickImage.getDimension()
//							.getHeight());
//				} else {
//					h = (int) ((double) magickImage.getDimension().getHeight() * (double) w / (double) magickImage.getDimension()
//							.getWidth());
//				}
//			}
//
//			// 缩小图片
//			magickImage.scaleImage(w, h);
//
//			// 移除图片的信息,减少图片体积
//			magickImage.profileImage("*", null);
//
//			ImageInfo info = new ImageInfo();
//			magickImage.writeImage(info);
//
//			result = magickImage.imageToBlob(info);
//		} finally {
//
//			if (magickImage != null) {
//				magickImage.destroyImages();
//			}
//
//		}
//
//		return result;
//	}

	private static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Create a buffered image with a format that's compatible with the screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {

			// PNG透明图照旧透明
			int transparency = Transparency.TRANSLUCENT;

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			int type = BufferedImage.TYPE_INT_RGB;
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	public static byte[] resizeImageDefault(InputStream in, int w, int h, String imageFormat) throws Exception {

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		BufferedImage inbi = null;

		BufferedImage outbi = null;

		try {

			// 如果是win的环境,有图形界面,调用toolkit来读取image,避免因为ImageIO.read读取丢失ICC信息,有些图像会蒙红的现象
			// if (StringUtils.containsIgnoreCase(SystemUtilsEx.getOSName(), "win")) {
			// inbi = toBufferedImage(Toolkit.getDefaultToolkit().createImage(image));
			// } else {
			inbi = ImageIO.read(in);
			// }

			// 如果等比例,直接返回
			if (inbi.getWidth() == w && inbi.getHeight() == h) {
				outbi = inbi;
			} else {
				// 如果w或h有为0的，则进行等比例压缩
				if (w == 0 || h == 0) {

					if (w == 0 && h == 0) {
						throw new ServiceException("图片压缩不能输入宽高都为0!");
					}

					if (w == 0) {
						w = (int) ((double) inbi.getWidth() * (double) h / (double) inbi.getHeight());
					} else {
						h = (int) ((double) inbi.getHeight() * (double) w / (double) inbi.getWidth());
					}
				}

				// TODO
				// 需要使用原有type
				// alpha通道需要变为透明的,如果时alpha的图片,则使用透明
				// imageType如果时0的话，设置为5，否则报java.lang.IllegalArgumentException: Unknown image type 0
				BufferedImage redrawImage = new BufferedImage(w, h, inbi.isAlphaPremultiplied() ? BufferedImage.TRANSLUCENT
						: (inbi.getType() == 0 ? 5 : inbi.getType()));

				// Image.SCALE_SMOOTH 的缩略算法 生成缩略图片的平滑度的,优先级比速度高 生成的图片质量比较好 但速度慢
				redrawImage.getGraphics().drawImage(inbi.getScaledInstance(w, h, Image.SCALE_SMOOTH), 0, 0, null);

				redrawImage.getGraphics().dispose();

				outbi = redrawImage;
			}

			ImageIO.write(outbi, imageFormat, out);

			return out.toByteArray();
		} finally {

			if (inbi != null) {
				inbi.flush();
			}

			if (out != null) {
				out.close();
			}
			if (in != null) {
				in.close();
			}
		}
	}

	/**
	 * 水平翻转图像
	 * @param bufferedimage 目标图像
	 * @return
	 */
	public static BufferedImage flipImage(final BufferedImage bufferedimage) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, bufferedimage.getColorModel().getTransparency())).createGraphics()).drawImage(
				bufferedimage, 0, 0, w, h, w, 0, 0, h, null);
		// AffineTransform transform = new
		// AffineTransform(-1,0,0,1,sourceImage.getWidth()-1,0);//水平翻转

		graphics2d.dispose();
		return img;
	}

	/**
	 * 垂直翻转图像
	 * @param bufferedimage 目标图像
	 * @return
	 */
	public static BufferedImage flopImage(final BufferedImage bufferedimage) {
		int w = bufferedimage.getWidth();
		int h = bufferedimage.getHeight();
		BufferedImage img;
		Graphics2D graphics2d;
		(graphics2d = (img = new BufferedImage(w, h, bufferedimage.getColorModel().getTransparency())).createGraphics()).drawImage(
				bufferedimage, new AffineTransform(1, 0, 0, -1, 0, h - 1), null);
		graphics2d.dispose();
		return img;
	}

	/**
	 * <pre>
	 * 图像转换成base64字符串
	 * </pre>
	 * @param imgFilePath 图片本地文件路径
	 * @return base64字符串
	 * @throws IOException
	 */
	public static String Img2Base64(String imgFilePath) throws IOException {

		byte[] data = null;

		InputStream in = null;
		// 读取图片字节数组
		try {
			in = new FileInputStream(imgFilePath);
			data = new byte[in.available()];
			in.read(data);
			in.close();
		} finally {
			if (in != null) {
				in.close();
			}
		}

		// 对字节数组Base64编码
		return new BASE64Encoder().encode(data);
	}

	public static byte[] cut(byte[] image, String format, int x, int y, int width, int height) throws Exception {

		ByteArrayInputStream bais = null;
		ImageInputStream iis = null;

		try {

			// 读取图片文件
			bais = new ByteArrayInputStream(image);

			// ImageReader声称能够解码指定格式
			Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName(format);
			ImageReader reader = it.next();

			// 获取图片流
			iis = ImageIO.createImageInputStream(bais);

			// 输入源中的图像将只按顺序读取
			reader.setInput(iis, true);

			// 描述如何对流进行解码
			ImageReadParam param = reader.getDefaultReadParam();

			// 图片裁剪区域
			Rectangle rect = new Rectangle(x, y, width, height);

			// 提供一个 BufferedImage，将其用作解码像素数据的目标
			param.setSourceRegion(rect);

			// 使用所提供的 ImageReadParam 读取通过索引 imageIndex 指定的对象
			BufferedImage bi = reader.read(0, param);

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			ImageIO.write(bi, format, out);

			return out.toByteArray();
		} finally {

			if (bais != null) {
				bais.close();
			}
			if (iis != null) {
				iis.close();
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// ImageUtilsEx.resizePNG("D:/111111111111111/12333.png", 72, 72, "D:/111111111111111/114.png", 100, 100);

		FileUtils.writeByteArrayToFile(new File("D:/111111111111111/123334441.png"),
				ImageUtilsEx.cut(FileUtils.readFileToByteArray(new File("D:/111111111111111/12333.png")), "png", 50, 50, 50, 50));

		// FileUtils.writeByteArrayToFile(new File("D:/111111111111111/12333444.png"),
		// ImageUtilsEx.resizeImage(FileUtils.readFileToByteArray(new File("D:/111111111111111/12333.png")), 100, 100, "png"));
		//
		// FileUtils.writeByteArrayToFile(new File("D:/1111111111111111111111111111111/2012053113023532_1.png"), ImageUtilsEx.resizeImage(
		// FileUtils.readFileToByteArray(new File("D:/1111111111111111111111111111111/2012053113023532.png")), 300, 300, "png"));

		// ImageUtilsEx.compressPic("D:/11111111111111111111/2012053113023532.png", "D:/11111111111111111111/111.png", 100, 100, false);

		// System.out.println(FileUtilsEx.getSuffix("21.png"));
	}
}
