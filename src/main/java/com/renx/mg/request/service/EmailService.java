package com.renx.mg.request.service;

import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.UserRepository;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private UserRepository userRepository;

	public void sendMessage(String[] to, String subject, String content) {
		if (to == null || to.length == 0) {
			log.warn("sendMessage: no recipients, subject={}", subject);
			return;
		}
		MimeMessage message = emailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(content);
			emailSender.send(message);
			log.info("Email sent to {} recipients, subject={}", to.length, subject);
		} catch (Exception e) {
			log.error("Error sending email to {}, subject={}: {}", String.join(",", to), subject, e.getMessage(), e);
		}
	}

	public String[] adminCompanyEmail(Long companyId) {
		try {
			List<User> userList = userRepository.findByCustomer_CompanyIdAndProfileId(companyId, Constants.COMPANY_ADMIN_PROFILE_ID);
			String[] emailList = new String[userList.size() + 1];
			emailList[0] = "aacosta@mgserviceunlimited.com";
			int count = 1;
			for (User user : userList) {
				if (user.getProfileId().equals(Constants.COMPANY_ADMIN_PROFILE_ID)) {
					emailList[count] = user.getCustomer().getEmail();
					count++;
				}
			}
			return emailList;
		} catch (Exception e) {
			log.error("Error building adminCompanyEmail for companyId={}: {}", companyId, e.getMessage(), e);
			return new String[] { "aacosta@mgserviceunlimited.com" };
		}
	}
  
  

}