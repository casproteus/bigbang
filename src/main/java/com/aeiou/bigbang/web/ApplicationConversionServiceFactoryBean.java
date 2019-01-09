package com.aeiou.bigbang.web;

import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;
import org.springframework.roo.addon.web.mvc.controller.converter.RooConversionService;

/**
 * A central place to register application converters and formatters.
 */
@RooConversionService
public class ApplicationConversionServiceFactoryBean extends FormattingConversionServiceFactoryBean {

    // @Override
    // protected void installFormatters(
    // FormatterRegistry registry) {
    // super.installFormatters(registry);
    // // Register application converters and formatters
    // }

    public void installLabelConverters(
            FormatterRegistry registry) {
        // registry.addConverter(getBigTagToStringConverter());
        registry.addConverter(getIdToBigTagConverter());
        registry.addConverter(getStringToBigTagConverter());
        registry.addConverter(getCircleToStringConverter());
        registry.addConverter(getIdToCircleConverter());
        registry.addConverter(getStringToCircleConverter());
        registry.addConverter(getContentToStringConverter());
        registry.addConverter(getIdToContentConverter());
        registry.addConverter(getStringToContentConverter());
        registry.addConverter(getCustomizeToStringConverter());
        registry.addConverter(getIdToCustomizeConverter());
        registry.addConverter(getStringToCustomizeConverter());
        registry.addConverter(getMessageToStringConverter());
        registry.addConverter(getIdToMessageConverter());
        registry.addConverter(getStringToMessageConverter());
        registry.addConverter(getRemarkToStringConverter());
        registry.addConverter(getIdToRemarkConverter());
        registry.addConverter(getStringToRemarkConverter());
        registry.addConverter(getTwitterToStringConverter());
        registry.addConverter(getIdToTwitterConverter());
        registry.addConverter(getStringToTwitterConverter());
        registry.addConverter(getUserAccountToStringConverter());
        registry.addConverter(getIdToUserAccountConverter());
        registry.addConverter(getStringToUserAccountConverter());
    }
}
