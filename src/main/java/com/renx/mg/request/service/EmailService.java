package com.renx.mg.request.service;

import java.util.List;

import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.renx.mg.request.common.Constants;
import com.renx.mg.request.model.User;
import com.renx.mg.request.repository.UserRepository;

@Service
public class EmailService {

	private static final Logger log = LoggerFactory.getLogger(EmailService.class);

	/** Paleta del sitio: primary, secondary, background, text (alineado con index.css) */
	private static final String COLOR_PRIMARY = "#10326a";
	private static final String COLOR_SECONDARY = "#61a1d9";
	private static final String COLOR_BG = "#f8f9fa";
	private static final String COLOR_TEXT = "#1a1a1a";
	private static final String COLOR_MUTED = "#666666";
	private static final String COLOR_WHITE = "#ffffff";

	@Autowired
	private JavaMailSender emailSender;

	@Autowired
	private UserRepository userRepository;

	@Value("${mg.mail.override-to:}")
	private String overrideTo;

	@Value("${mg.app.base-url:}")
	private String appBaseUrl;

	private static final String FROM_EMAIL = "support.desk@mgservicesunlimited.com";

	/** Convierte código L/M/H a texto completo para correos (High, Medium, Low). */
	public static String priorityToDisplayText(String code) {
		if (code == null || code.isBlank()) return "";
		switch (code.toUpperCase()) {
			case "H": return "High";
			case "M": return "Medium";
			case "L": return "Low";
			default: return code;
		}
	}

	public void sendMessage(String[] to, String subject, String content) {
		if (to == null || to.length == 0) {
			log.warn("sendMessage: no recipients, subject={}", subject);
			return;
		}
		String[] actualTo = resolveRecipients(to);
		MimeMessage message = emailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(FROM_EMAIL);
			helper.setTo(actualTo);
			helper.setSubject(subject);
			helper.setText(content);
			emailSender.send(message);
			log.info("Email sent to {} recipients, subject={}", actualTo.length, subject);
		} catch (Exception e) {
			log.error("Error sending email to {}, subject={}: {}", String.join(",", actualTo), subject, e.getMessage(), e);
		}
	}

	/**
	 * Envía el correo en segundo plano para no bloquear la respuesta HTTP.
	 */
	@Async("mailTaskExecutor")
	public void sendMessageAsync(String[] to, String subject, String content) {
		sendMessage(to, subject, content);
	}

	/**
	 * Envía un correo HTML con la plantilla de marca (paleta del sitio). Se ejecuta en segundo plano.
	 */
	@Async("mailTaskExecutor")
	public void sendHtmlMessageAsync(String[] to, String subject, String title, String bodyHtml) {
		if (to == null || to.length == 0) {
			log.warn("sendHtmlMessageAsync: no recipients, subject={}", subject);
			return;
		}
		String[] actualTo = resolveRecipients(to);
		String html = buildHtmlTemplate(title, bodyHtml);
		MimeMessage message = emailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
			helper.setFrom(FROM_EMAIL);
			helper.setTo(actualTo);
			helper.setSubject(subject);
			helper.setText(htmlToPlainText(html), html);
			emailSender.send(message);
			log.info("HTML email sent to {} recipients, subject={}", actualTo.length, subject);
		} catch (Exception e) {
			log.error("Error sending HTML email to {}, subject={}: {}", String.join(",", actualTo), subject, e.getMessage(), e);
		}
	}

	private String buildHtmlTemplate(String title, String bodyHtml) {
		return ""
			+ "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><meta name=\"viewport\" content=\"width=device-width,initial-scale=1\"></head>"
			+ "<body style=\"margin:0;padding:0;font-family:'Segoe UI',Tahoma,Geneva,Verdana,sans-serif;font-size:15px;line-height:1.5;color:" + COLOR_TEXT + ";background:" + COLOR_BG + ";\">"
			+ "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"background:" + COLOR_BG + ";\">"
			+ "<tr><td style=\"padding:24px 16px;\">"
			+ "<table role=\"presentation\" width=\"100%\" cellpadding=\"0\" cellspacing=\"0\" style=\"max-width:560px;margin:0 auto;background:" + COLOR_WHITE + ";border-radius:8px;box-shadow:0 2px 8px rgba(0,0,0,0.08);\">"
			+ "<tr><td style=\"background:" + COLOR_PRIMARY + ";color:" + COLOR_WHITE + ";padding:20px 24px;border-radius:8px 8px 0 0;\">"
			+ "<span style=\"font-weight:600;font-size:18px;\">MG Request</span><span style=\"color:" + COLOR_SECONDARY + ";margin-left:6px;font-weight:500;\">Support</span>"
			+ "</td></tr>"
			+ "<tr><td style=\"padding:24px;\">"
			+ "<h2 style=\"margin:0 0 16px 0;font-size:18px;font-weight:600;color:" + COLOR_PRIMARY + ";\">" + escapeHtml(title) + "</h2>"
			+ "<div style=\"color:" + COLOR_TEXT + ";\">" + bodyHtml + "</div>"
			+ "</td></tr>"
			+ "<tr><td style=\"padding:16px 24px;border-top:1px solid #eee;font-size:12px;color:" + COLOR_MUTED + ";\">"
			+ "MG Services Unlimited · Support Desk"
			+ "</td></tr>"
			+ "</table></td></tr></table></body></html>";
	}

	private static String escapeHtml(String s) {
		if (s == null) return "";
		return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
	}

	private static String htmlToPlainText(String html) {
		if (html == null) return "";
		return html.replaceAll("<[^>]+>", " ").replaceAll("&nbsp;", " ").replaceAll("\\s+", " ").trim();
	}

	/** Cuerpo HTML para correo de nueva solicitud / asignación. Prioridad en texto completo (High/Medium/Low). Si requestId y baseUrl están definidos, se añade botón "View request". */
	public String buildNewRequestBody(String companyName, String siteName, String priorityCode, String description, Long requestId) {
		String priorityLabel = priorityToDisplayText(priorityCode);
		String d = escapeHtml(description != null ? description : "");
		String body = "<p style=\"margin:0 0 12px 0;\"><strong style=\"color:" + COLOR_PRIMARY + ";\">Company:</strong> " + escapeHtml(companyName) + "</p>"
			+ "<p style=\"margin:0 0 12px 0;\"><strong style=\"color:" + COLOR_PRIMARY + ";\">Site:</strong> " + escapeHtml(siteName) + "</p>"
			+ "<p style=\"margin:0 0 12px 0;\"><strong style=\"color:" + COLOR_PRIMARY + ";\">Priority:</strong> " + escapeHtml(priorityLabel) + "</p>"
			+ "<p style=\"margin:0 0 16px 0;\"><strong style=\"color:" + COLOR_PRIMARY + ";\">Request detail:</strong><br/>" + d.replace("\n", "<br/>") + "</p>";
		return body + buildViewRequestButton(requestId);
	}

	/** Cuerpo HTML para correo de cambio de estado. Si requestId y baseUrl están definidos, se añade botón "View request". */
	public String buildStatusChangeBody(String firstName, String statusMessage, String detailDescription, Long requestId) {
		String detail = escapeHtml(detailDescription != null ? detailDescription : "").replace("\n", "<br/>");
		String body = "<p style=\"margin:0 0 12px 0;\">Dear " + escapeHtml(firstName != null ? firstName : "") + ",</p>"
			+ "<p style=\"margin:0 0 12px 0;\">" + statusMessage + "</p>"
			+ "<p style=\"margin:0 0 16px 0;padding:12px;background:" + COLOR_BG + ";border-radius:6px;font-size:14px;\"><strong>Request detail:</strong><br/>" + detail + "</p>";
		return body + buildViewRequestButton(requestId);
	}

	private String buildViewRequestButton(Long requestId) {
		if (requestId == null || appBaseUrl == null || appBaseUrl.isBlank()) return "";
		String url = appBaseUrl.replaceAll("/$", "") + "/requests/" + requestId;
		return "<p style=\"margin:24px 0 0 0;\">"
			+ "<a href=\"" + escapeHtml(url) + "\" style=\"display:inline-block;padding:12px 24px;background:" + COLOR_PRIMARY + ";color:" + COLOR_WHITE + ";text-decoration:none;font-weight:600;border-radius:6px;\">View request</a>"
			+ "</p>";
	}

	/**
	 * Si mg.mail.override-to está definido (perfil test), redirige todos los correos a esas direcciones.
	 * En prod no se define y se usan los destinatarios reales.
	 */
	private String[] resolveRecipients(String[] to) {
		if (overrideTo == null || overrideTo.isBlank()) {
			return to;
		}
		String[] override = overrideTo.split(",");
		String[] result = new String[override.length];
		for (int i = 0; i < override.length; i++) {
			result[i] = override[i].trim();
		}
		log.debug("Mail override active: redirecting to {} (original: {})", overrideTo, String.join(",", to));
		return result;
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