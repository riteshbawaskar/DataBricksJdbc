package com.databricks.test.service;

import com.databricks.test.config.TestConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TransformationService {

    private static final Logger logger = LoggerFactory.getLogger(TransformationService.class);

    public Object applyTransformation(Object value, String transformationRule,
                                      TestConfiguration.ValidationRules validationRules) {

        if (value == null) {
            return null;
        }

        try {
            switch (transformationRule) {
                case "DIRECT_COMPARE":
                    return value;

                case "PAD_LEFT_ZERO_8":
                    return applyPadding(value.toString(), validationRules.getPaddingConfig());

                case "DATE_FORMAT_DD_MON_YY_TO_DD_MM_YY":
                    return convertDateFormat(value.toString(), validationRules.getDateFormats());

                default:
                    logger.warn("Unknown transformation rule: {}", transformationRule);
                    return value;
            }
        } catch (Exception e) {
            logger.error("Error applying transformation rule '{}' to value '{}'", transformationRule, value, e);
            return value;
        }
    }

    private String applyPadding(String value, TestConfiguration.PaddingConfig paddingConfig) {
        if (value == null || paddingConfig == null) {
            return value;
        }

        int targetLength = paddingConfig.getTargetLength();
        String padChar = paddingConfig.getPadChar();
        String padDirection = paddingConfig.getPadDirection();

        if (value.length() >= targetLength) {
            return value;
        }

        if ("LEFT".equalsIgnoreCase(padDirection)) {
            return StringUtils.leftPad(value, targetLength, padChar);
        } else if ("RIGHT".equalsIgnoreCase(padDirection)) {
            return StringUtils.rightPad(value, targetLength, padChar);
        }

        return value;
    }

    private String convertDateFormat(String dateString, TestConfiguration.DateFormats dateFormats) {
        if (dateString == null || dateFormats == null) {
            return dateString;
        }

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat(dateFormats.getInput());
            SimpleDateFormat outputFormat = new SimpleDateFormat(dateFormats.getOutput());

            Date date = inputFormat.parse(dateString);
            return outputFormat.format(date);
        } catch (ParseException e) {
            logger.error("Error converting date format from '{}' to '{}' for value '{}'",
                    dateFormats.getInput(), dateFormats.getOutput(), dateString, e);
            return dateString;
        }
    }

    public boolean compareValues(Object expected, Object actual, String transformationRule,
                                 TestConfiguration.ValidationRules validationRules) {

        if (expected == null && actual == null) {
            return true;
        }

        if (expected == null || actual == null) {
            return false;
        }

        // Apply transformation to expected value for comparison
        Object transformedExpected = applyTransformation(expected, transformationRule, validationRules);

        // Handle numeric comparison with tolerance
        if (transformedExpected instanceof Number && actual instanceof Number) {
            double expectedNum = ((Number) transformedExpected).doubleValue();
            double actualNum = ((Number) actual).doubleValue();
            double tolerance = validationRules.getNumericTolerance();

            return Math.abs(expectedNum - actualNum) <= tolerance;
        }

        // String comparison
        return String.valueOf(transformedExpected).equals(String.valueOf(actual));
    }

    public String getTransformationDescription(String transformationRule) {
        switch (transformationRule) {
            case "DIRECT_COMPARE":
                return "Direct comparison without transformation";
            case "PAD_LEFT_ZERO_8":
                return "Left pad with zeros to 8 characters";
            case "DATE_FORMAT_DD_MON_YY_TO_DD_MM_YY":
                return "Convert date format from dd-MMM-yy to dd/MM/yy";
            default:
                return "Unknown transformation: " + transformationRule;
        }
    }
}