package com.ihidea.component.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.ihidea.core.support.exception.ServiceException;

/**
 * @作者 黎江
 * @创建日期 Sep 16, 2013
 * @创建时间 10:51:23 AM
 * @版本号 V 1.0
 * @description-
 */
@Service
public class MailService {

	/**
	 * 异步发送邮件
	 * @param mailInfo
	 */
	public void send(final MailInfo mailInfo) {

		new Thread(new Runnable() {
			public void run() {
				sendWithSyn(mailInfo);
			}
		}).start();
	}

	/**
	 * 同步发送邮件
	 * @param mailInfo
	 */
	public void sendWithSyn(final MailInfo mailInfo) {

		JavaMailSenderImpl mailSender = new JavaMailSenderImpl();

		if (StringUtils.isNotBlank(mailInfo.getSenderHost())) {
			mailSender.setHost(mailInfo.getSenderHost());
		}

		if (mailInfo.getSenderPort() != null) {
			mailSender.setPort(mailInfo.getSenderPort());
		}

		if (StringUtils.isNotBlank(mailInfo.getSenderUserName())) {
			mailSender.setUsername(mailInfo.getSenderUserName());
		}

		if (StringUtils.isNotBlank(mailInfo.getSenderPassword())) {
			mailSender.setPassword(mailInfo.getSenderPassword());
		}

		MimeMessage mailMessage = mailSender.createMimeMessage();

		try {
			// 设置utf-8或GBK编码，否则邮件会有乱码
			MimeMessageHelper messageHelper = new MimeMessageHelper(mailMessage, true, "UTF-8");

			// 接受者
			messageHelper.setTo(mailInfo.getTo());

			// 发送者,这里还可以另起Email别名，不用和xml里的username一致
			if (StringUtils.isNotBlank(mailInfo.getFrom())) {
				messageHelper.setFrom(withNickName(mailInfo.getFrom(), mailInfo.getNickName()));
			} else {
				messageHelper.setFrom(withNickName(mailSender.getUsername(), mailInfo.getNickName()));
			}

			messageHelper.setSubject(mailInfo.getSubject());// 主题

			// 邮件内容，注意加参数true，表示启用html格式
			messageHelper.setText(mailInfo.getText(), (mailInfo.getText().indexOf("<html") > -1));

			// 这里的方法调用和插入图片是不同的，使用MimeUtility.encodeWord()来解决附件名称的中文问题
			if (mailInfo.getAttachmentList() != null && mailInfo.getAttachmentList().size() > 0) {

				for (final String fileName : mailInfo.getAttachmentList().keySet()) {
					messageHelper.addAttachment(MimeUtility.encodeWord(fileName), new InputStreamSource() {

						@Override
						public InputStream getInputStream() throws IOException {
							return new ByteArrayInputStream(mailInfo.getAttachmentList().get(fileName));
						}
					});
				}
			}

			mailSender.send(mailMessage);

		} catch (Exception e) {
			throw new ServiceException("发送邮件异常!" + e.getMessage());
		}
	}

	private InternetAddress withNickName(String mail, String nickName) throws Exception {

		if (StringUtils.isBlank(nickName)) {
			return new InternetAddress(mail);
		} else {
			return new InternetAddress(MimeUtility.encodeText(nickName) + " <" + mail + ">");
		}
	}

	public static void main(String[] args) throws Exception {
		new MailService().send(new MailInfo("service@ibmssapad.com", new String[] { "45392841@qq.com" }, "test", "test", null, "smtp.exmail.qq.com",
				"service@ibmssapad.com", "wasd1234", "service@ibmssapad.com"));
	}

}
