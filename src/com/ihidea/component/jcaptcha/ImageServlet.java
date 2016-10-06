package com.ihidea.component.jcaptcha;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Random;

import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ImageServlet extends HttpServlet {

	public static final String JCAPTCHA_NAME = "valiCode";

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

		// 设置页面不缓存
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache");
		response.setDateHeader("Expires", 0);

		// 设置默认生成4个验证码
		int length = 4;
		if (request.getParameter("len") != null) {
			length = Integer.valueOf(request.getParameter("len"));
		}
		String allstring = "abcdefghijklmnopqrstuvwxyz0123456789";
		String numstring = "0123456789";
		String base;
		if (request.getParameter("onlynum") != null) {
			if (Boolean.valueOf(request.getParameter("onlynum"))) {
				base = numstring;
			} else {
				base = allstring;
			}
		} else {
			base = allstring;
		}

		// 设置图片的长宽
		int width = 15 * length, height = 22;

		// 创建内存图像
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// 获取图形上下文
		Graphics g = image.createGraphics();

		// 设定图像背景色(因为是做背景，所以偏淡)
		g.setColor(getRandColor(180, 250));
		g.fillRect(0, 0, width, height);

		// 设置字体
		g.setFont(new Font("Tahoma", Font.PLAIN, 17));

		java.util.Random rand = new Random(); // 设置随机种子

		// 设置备选验证码:包括"a-z"和数字"0-9"
		int size = base.length();
		StringBuffer str = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int start = rand.nextInt(size);
			String tmpStr = base.substring(start, start + 1);

			str.append(tmpStr);

			// 生成随机颜色(因为是做前景，所以偏深)
			g.setColor(getRandColor(10, 150));

			// 将此字画到图片上
			g.drawString(tmpStr, 13 * i + 6 + rand.nextInt(5), 14 + rand.nextInt(6));

		}

		// 将认证码存入session
		request.getSession().setAttribute(ImageServlet.JCAPTCHA_NAME, str.toString());

		// 图象生效
		g.dispose();

		// 输出图象到页面
		ImageIO.write(image, "JPEG", response.getOutputStream());
//		JPEGImageEncoder encoder = JPEGCodec.createJPEGEncoder(response.getOutputStream());
//		encoder.encode(image);
	}

	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		doGet(request, response);
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init() throws ServletException {
	}

	/**
	 * 给定范围获得一个随机颜色
	 * @param fc
	 * @param bc
	 * @return
	 */
	private Color getRandColor(int fc, int bc) {
		Random random = new Random();
		if (fc > 255)
			fc = 255;
		if (bc > 255)
			bc = 255;
		int r = fc + random.nextInt(bc - fc);
		int g = fc + random.nextInt(bc - fc);
		int b = fc + random.nextInt(bc - fc);
		return new Color(r, g, b);
	}

}
