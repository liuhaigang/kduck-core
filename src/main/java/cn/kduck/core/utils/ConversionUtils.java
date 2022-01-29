package cn.kduck.core.utils;

import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.support.DefaultConversionService;
import org.springframework.core.convert.support.GenericConversionService;
import org.springframework.util.StringUtils;

import java.io.Reader;
import java.io.StringReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * LiuHG
 */
public final class ConversionUtils {

    private static ConversionService conversionService = DefaultConversionService.getSharedInstance();

    static {
        GenericConversionService genericConversionService = (GenericConversionService)conversionService;
        genericConversionService.addConverter(new Converter<Long, Date>() {
            @Override
            public Date convert(Long source) {
                return new Date(source);
            }
        });

        genericConversionService.addConverter(new Integer2DateConverter());
        genericConversionService.addConverter(new DateConverter());
        genericConversionService.addConverter(new Boolean2IntegerConverter());
        genericConversionService.addConverter(new Integer2BooleanConverter());
        genericConversionService.addConverter(new ReaderConverter());

    }

    public static <T> T convert(Object source, Class<T> targetType){
        return conversionService.convert(source,targetType);
    }

    public static class Boolean2IntegerConverter implements Converter<Boolean, Integer>{
        @Override
        public Integer convert(Boolean source) {
            if(source == null) return 0;
            return source.booleanValue() ? 1 : 0;
        }
    }

    public static class Integer2BooleanConverter implements Converter<Integer,Boolean>{
        @Override
        public Boolean convert(Integer source) {
            if(source == null) return false;
            return source.intValue() > 0 ? true : false;
        }
    }

    public static class Integer2DateConverter implements Converter<Integer, Date>{
        @Override
        public Date convert(Integer source) {
            return new Date(source.longValue());
        }
    }

    public static class ReaderConverter implements Converter<String, Reader>{

        @Override
        public Reader convert(String source) {
            if(source == null) return null;
            return new StringReader(source);
        }
    }

    public static class DateConverter implements Converter<String, Date>{

        private ThreadLocal<DateFormatContainer> dateConverterThreadLocal = new ThreadLocal<>();

//        private DateFormat DATE_FORMAT_YM = new SimpleDateFormat("yyyy-MM");
//        private DateFormat DATE_FORMAT_YMD = new SimpleDateFormat("yyyy-MM-dd");
//        private DateFormat DATE_FORMAT_YMDHM = new SimpleDateFormat("yyyy-MM-dd HH:mm");
//        private DateFormat DATE_FORMAT_YMDHMS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        @Override
        public Date convert(String dateValue) {
            String value = dateValue.trim();
            if (!StringUtils.hasText(value)) {
                return null;
            }
            if (dateValue.matches("^\\d{4}-\\d{1,2}$")) {
                return parseDate(dateValue, DateFormatType.DATE_FORMAT_YM);
            } else if (dateValue.matches("^\\d{4}-\\d{1,2}-\\d{1,2}$")) {
                return parseDate(dateValue, DateFormatType.DATE_FORMAT_YMD);
            } else if (dateValue.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}$")) {
                return parseDate(dateValue, DateFormatType.DATE_FORMAT_YMDHM);
            } else if (dateValue.matches("^\\d{4}-\\d{1,2}-\\d{1,2} {1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")) {
                return parseDate(dateValue, DateFormatType.DATE_FORMAT_YMDHMS);
            } else if (dateValue.matches("^[\\-|1-9]\\d*$")) {
                return new Date(Long.valueOf(dateValue));
            } else if (dateValue.matches("^{1}\\d{1,2}:\\d{1,2}:\\d{1,2}$")) {
                return parseDate(dateValue, DateFormatType.DATE_FORMAT_HMS);
            } else {
                throw new IllegalArgumentException("没有对该日期类型格式准备适合的转换器：" + dateValue);
            }
        }

        private Date parseDate(String dateValue, DateFormatType format) {
            DateFormatContainer dateFormatContainer = dateConverterThreadLocal.get();
            if(dateFormatContainer == null){
                dateFormatContainer = new DateFormatContainer();
                dateConverterThreadLocal.set(dateFormatContainer);
            }

            DateFormat dateFormat = dateFormatContainer.getDateFormat(format);
            try {
                return dateFormat.parse(dateValue);
            } catch (ParseException e) {
                throw new RuntimeException("格式化日期错误：pattern=" + ((SimpleDateFormat)dateFormat).toPattern() + ",dateValue=" + dateValue);
            }
        }

        private class DateFormatContainer {

            private Map<DateFormatType,DateFormat> dateFormatMap = new HashMap();

            public DateFormatContainer(){
                dateFormatMap.put(DateFormatType.DATE_FORMAT_HMS,new SimpleDateFormat("HH:mm:ss"));
                dateFormatMap.put(DateFormatType.DATE_FORMAT_YM,new SimpleDateFormat("yyyy-MM"));
                dateFormatMap.put(DateFormatType.DATE_FORMAT_YMD,new SimpleDateFormat("yyyy-MM-dd"));
                dateFormatMap.put(DateFormatType.DATE_FORMAT_YMDHM,new SimpleDateFormat("yyyy-MM-dd HH:mm"));
                dateFormatMap.put(DateFormatType.DATE_FORMAT_YMDHMS,new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));
            }

            private DateFormat getDateFormat(DateFormatType format){
                return dateFormatMap.get(format);
            }

        }

        private enum DateFormatType{
            DATE_FORMAT_YM,
            DATE_FORMAT_YMD,
            DATE_FORMAT_YMDHM,
            DATE_FORMAT_YMDHMS,
            DATE_FORMAT_HMS,
        }
    }

}
