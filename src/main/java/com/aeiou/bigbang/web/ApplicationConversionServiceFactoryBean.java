package com.aeiou.bigbang.web;

import com.aeiou.bigbang.domain.BigTag;
import com.aeiou.bigbang.domain.Circle;
import com.aeiou.bigbang.domain.Content;
import com.aeiou.bigbang.domain.Customize;
import com.aeiou.bigbang.domain.Message;
import com.aeiou.bigbang.domain.Remark;
import com.aeiou.bigbang.domain.Twitter;
import com.aeiou.bigbang.domain.UserAccount;
import org.springframework.beans.factory.annotation.Configurable;
import org.springframework.core.convert.converter.Converter;
import org.springframework.format.FormatterRegistry;
import org.springframework.format.support.FormattingConversionServiceFactoryBean;

@Configurable
/**
 * A central place to register application converters and formatters.
 */
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

	public Converter<BigTag, String> getBigTagToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.BigTag, java.lang.String>() {
            public String convert(BigTag bigTag) {
                return new StringBuilder().append(bigTag.getContentID()).append(' ').append(bigTag.getContentTitle()).append(' ').append(bigTag.getContentURL()).append(' ').append(bigTag.getTwitterID()).toString();
            }
        };
    }

	public Converter<Long, BigTag> getIdToBigTagConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.BigTag>() {
            public com.aeiou.bigbang.domain.BigTag convert(java.lang.Long id) {
                return BigTag.findBigTag(id);
            }
        };
    }

	public Converter<String, BigTag> getStringToBigTagConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.BigTag>() {
            public com.aeiou.bigbang.domain.BigTag convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), BigTag.class);
            }
        };
    }

	public Converter<Circle, String> getCircleToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Circle, java.lang.String>() {
            public String convert(Circle circle) {
                return new StringBuilder().append(circle.getCircleName()).append(' ').append(circle.getDescription()).append(' ').append(circle.getCreatedDate()).toString();
            }
        };
    }

	public Converter<Long, Circle> getIdToCircleConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Circle>() {
            public com.aeiou.bigbang.domain.Circle convert(java.lang.Long id) {
                return Circle.findCircle(id);
            }
        };
    }

	public Converter<String, Circle> getStringToCircleConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Circle>() {
            public com.aeiou.bigbang.domain.Circle convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Circle.class);
            }
        };
    }

	public Converter<Content, String> getContentToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Content, java.lang.String>() {
            public String convert(Content content) {
                return new StringBuilder().append(content.getAddingTagFlag()).append(' ').append(content.getTitle()).append(' ').append(content.getSourceURL()).append(' ').append(content.getConentCache()).toString();
            }
        };
    }

	public Converter<Long, Content> getIdToContentConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Content>() {
            public com.aeiou.bigbang.domain.Content convert(java.lang.Long id) {
                return Content.findContent(id);
            }
        };
    }

	public Converter<String, Content> getStringToContentConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Content>() {
            public com.aeiou.bigbang.domain.Content convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Content.class);
            }
        };
    }

	public Converter<Customize, String> getCustomizeToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Customize, java.lang.String>() {
            public String convert(Customize customize) {
                return new StringBuilder().append(customize.getCusKey()).append(' ').append(customize.getCusValue()).toString();
            }
        };
    }

	public Converter<Long, Customize> getIdToCustomizeConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Customize>() {
            public com.aeiou.bigbang.domain.Customize convert(java.lang.Long id) {
                return Customize.findCustomize(id);
            }
        };
    }

	public Converter<String, Customize> getStringToCustomizeConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Customize>() {
            public com.aeiou.bigbang.domain.Customize convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Customize.class);
            }
        };
    }

	public Converter<Message, String> getMessageToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Message, java.lang.String>() {
            public String convert(Message message) {
                return new StringBuilder().append(message.getContent()).append(' ').append(message.getPostTime()).append(' ').append(message.getStatus()).toString();
            }
        };
    }

	public Converter<Long, Message> getIdToMessageConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Message>() {
            public com.aeiou.bigbang.domain.Message convert(java.lang.Long id) {
                return Message.findMessage(id);
            }
        };
    }

	public Converter<String, Message> getStringToMessageConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Message>() {
            public com.aeiou.bigbang.domain.Message convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Message.class);
            }
        };
    }

	public Converter<Remark, String> getRemarkToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Remark, java.lang.String>() {
            public String convert(Remark remark) {
                return new StringBuilder().append(remark.getRefresh_time()).append(' ').append(remark.getContent()).append(' ').append(remark.getRemarkTime()).append(' ').append(remark.getAuthority()).toString();
            }
        };
    }

	public Converter<Long, Remark> getIdToRemarkConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Remark>() {
            public com.aeiou.bigbang.domain.Remark convert(java.lang.Long id) {
                return Remark.findRemark(id);
            }
        };
    }

	public Converter<String, Remark> getStringToRemarkConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Remark>() {
            public com.aeiou.bigbang.domain.Remark convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Remark.class);
            }
        };
    }

	public Converter<Twitter, String> getTwitterToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.Twitter, java.lang.String>() {
            public String convert(Twitter twitter) {
                return new StringBuilder().append(twitter.getAddingTagFlag()).append(' ').append(twitter.getTwitent()).append(' ').append(twitter.getTwitDate()).append(' ').append(twitter.getAuthority()).toString();
            }
        };
    }

	public Converter<Long, Twitter> getIdToTwitterConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.Twitter>() {
            public com.aeiou.bigbang.domain.Twitter convert(java.lang.Long id) {
                return Twitter.findTwitter(id);
            }
        };
    }

	public Converter<String, Twitter> getStringToTwitterConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.Twitter>() {
            public com.aeiou.bigbang.domain.Twitter convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), Twitter.class);
            }
        };
    }

	public Converter<UserAccount, String> getUserAccountToStringConverter() {
        return new org.springframework.core.convert.converter.Converter<com.aeiou.bigbang.domain.UserAccount, java.lang.String>() {
            public String convert(UserAccount userAccount) {
                return new StringBuilder().append(userAccount.getName()).append(' ').append(userAccount.getEmail()).append(' ').append(userAccount.getPassword()).append(' ').append(userAccount.getDescription()).toString();
            }
        };
    }

	public Converter<Long, UserAccount> getIdToUserAccountConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.Long, com.aeiou.bigbang.domain.UserAccount>() {
            public com.aeiou.bigbang.domain.UserAccount convert(java.lang.Long id) {
                return UserAccount.findUserAccount(id);
            }
        };
    }

	public Converter<String, UserAccount> getStringToUserAccountConverter() {
        return new org.springframework.core.convert.converter.Converter<java.lang.String, com.aeiou.bigbang.domain.UserAccount>() {
            public com.aeiou.bigbang.domain.UserAccount convert(String id) {
                return getObject().convert(getObject().convert(id, Long.class), UserAccount.class);
            }
        };
    }

	public void afterPropertiesSet() {
        super.afterPropertiesSet();
        installLabelConverters(getObject());
    }
}
