package com.ihidea.core.util;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.patchca.color.SingleColorFactory;
import org.patchca.filter.predefined.CurvesRippleFilterFactory;
import org.patchca.filter.predefined.DiffuseRippleFilterFactory;
import org.patchca.filter.predefined.DoubleRippleFilterFactory;
import org.patchca.filter.predefined.MarbleRippleFilterFactory;
import org.patchca.filter.predefined.WobbleRippleFilterFactory;
import org.patchca.font.RandomFontFactory;
import org.patchca.service.Captcha;
import org.patchca.service.ConfigurableCaptchaService;
import org.patchca.word.RandomWordFactory;

public class CaptchaUtils {

	private BufferedImage image;

	private String imageCode;

	public BufferedImage getImage() {
		return image;
	}

	public String getImageCode() {
		return imageCode;
	}

	public CaptchaUtils() {
		captcha(null, null, null);
	}

	public CaptchaUtils(Integer width, Integer height, Integer fontSize) {
		captcha(width, height, fontSize);
	}

	private void captcha(Integer width, Integer height, Integer fontSize) {

		ConfigurableCaptchaService cs = new ConfigurableCaptchaService();

		cs.setWordFactory(new MyWordFactory());
		cs.setFontFactory(new MyFontFactory(fontSize));

		if (width != null) {
			cs.setWidth(width);
		}

		if (height != null) {
			cs.setHeight(height);
		}

		cs.setColorFactory(new SingleColorFactory(new Color(25, 60, 170)));
		switch ((int) (System.currentTimeMillis() % 5)) {
		case 0:
			cs.setFilterFactory(new CurvesRippleFilterFactory(cs.getColorFactory()));
			break;
		case 1:
			cs.setFilterFactory(new MarbleRippleFilterFactory());
			break;
		case 2:
			cs.setFilterFactory(new DoubleRippleFilterFactory());
			break;
		case 3:
			cs.setFilterFactory(new WobbleRippleFilterFactory());
			break;
		case 4:
			cs.setFilterFactory(new DiffuseRippleFilterFactory());
			break;
		}

		Captcha captcha = cs.getCaptcha();

		image = captcha.getImage();

		imageCode = captcha.getChallenge();
	}

	private class MyWordFactory extends RandomWordFactory {
		public MyWordFactory() {

			// 文本范围和长度
			characters = "0123456789";

			minLength = 4;

			maxLength = 4;
		}
	}

	private class MyFontFactory extends RandomFontFactory {
		public MyFontFactory(Integer fontSize) {

			if (fontSize != null) {
				minSize = fontSize;
				maxSize = fontSize;
			}
		}
	}

	public static void main(String[] args) throws Exception {

		// CaptchaUtils c = new CaptchaUtils(130, 58, 40);
		CaptchaUtils c = new CaptchaUtils(86, 30, 25);

		ByteArrayOutputStream out = new ByteArrayOutputStream();

		ImageIO.write(c.getImage(), "PNG", out);

		FileUtils.writeByteArrayToFile(new File("E:/1.png"), out.toByteArray());

		System.out.println(c.getImageCode());

	}

}
