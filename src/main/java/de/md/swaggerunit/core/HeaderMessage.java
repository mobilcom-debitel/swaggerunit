package de.md.swaggerunit.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.atlassian.oai.validator.report.ValidationReport;
import com.atlassian.oai.validator.report.ValidationReport.Level;
import com.atlassian.oai.validator.report.ValidationReport.Message;
import com.atlassian.oai.validator.report.ValidationReport.MessageContext;

/**
 * Created by fpriede on 26.04.2017.
 */
public class HeaderMessage implements ValidationReport.Message {

	private final String key;

	private final String message;

	private Level level;

	private List<String> additionalInfo;

	public HeaderMessage(String key, String message) {
		this.key = key;
		this.message = message;
		this.additionalInfo = new ArrayList<>();
		this.level = Level.ERROR;
	}

	@Override
	public String getKey() {
		return key;
	}

	@Override
	public String getMessage() {
		return message;
	}

	@Override
	public ValidationReport.Level getLevel() {
		return level;
	}

	@Override
	public List<String> getAdditionalInfo() {
		return additionalInfo;
	}

	@Override
	public Message withLevel(Level level) {
		this.level = level;
		return this;
	}

	@Override
	public Message withAdditionalInfo(String info) {
		this.additionalInfo.add(info);
		return this;
	}

	@Override
	public Optional<MessageContext> getContext() {
		return Optional.empty();
	}

	@Override
	public Message withAdditionalContext(MessageContext context) {
		return this;
	}

}
