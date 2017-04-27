package de.md.swaggerunit.core;

import com.atlassian.oai.validator.report.ValidationReport;

import java.util.Collections;
import java.util.List;

/**
 * Created by fpriede on 26.04.2017.
 */
public class HeaderMessage implements ValidationReport.Message{

    private final String key;

    private final String message;

    public HeaderMessage(String key, String message) {
        this.key = key;
        this.message = message;
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
        return ValidationReport.Level.ERROR;
    }

    @Override
    public List<String> getAdditionalInfo() {
        return Collections.EMPTY_LIST;
    }
}
