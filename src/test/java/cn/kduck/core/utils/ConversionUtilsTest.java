package cn.kduck.core.utils;

import org.junit.jupiter.api.Test;

import java.util.Date;

public class ConversionUtilsTest {

    @Test
    public void convertYMD() {
        ConversionUtils.convert("2020-5-29", Date.class);
        ConversionUtils.convert("2020-5-2", Date.class);
        ConversionUtils.convert("2020-05-02", Date.class);
        ConversionUtils.convert("2020-11-11", Date.class);
    }

    @Test
    public void convertYMDHM() {
        ConversionUtils.convert("2020-5-29 12:12", Date.class);
        ConversionUtils.convert("2020-5-2 0:0", Date.class);
        ConversionUtils.convert("2020-05-02 1:1", Date.class);
        ConversionUtils.convert("2020-11-11 0:59", Date.class);
    }

    @Test
    public void convertYMDHMS() {
        ConversionUtils.convert("2020-5-29 12:12:0", Date.class);
        ConversionUtils.convert("2020-5-2 0:0:0", Date.class);
        ConversionUtils.convert("2020-05-02 1:1:11", Date.class);
        ConversionUtils.convert("2020-11-11 0:1:59", Date.class);
    }
}