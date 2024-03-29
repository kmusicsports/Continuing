package com.example.continuing.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.Calendar;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class UtilsTest {

    @Nested
    @DisplayName("[isAllDoubleSpaceメソッドのテスト]")
    public class NestedTestIsAllDoubleSpace {

        @Test
        @DisplayName("nullを渡す")
        public void passNull() {
            boolean actual = Utils.isAllDoubleSpace(null);
            assertTrue(actual);
        }

        @ParameterizedTest
        @CsvSource({"''", "'　'"})
        public void tests(String s) {
            boolean actual = Utils.isAllDoubleSpace(s);
            assertTrue(actual);
        }

        @Test
        @DisplayName("文字列を渡す")
        public void passNormalStr() {
            boolean actual = Utils.isAllDoubleSpace("test");
            assertFalse(actual);
        }
    }

    @Nested
    @DisplayName("[isBlankメソッドのテスト]")
    public class NestedTestIsBlank {

        @Test
        @DisplayName("nullを渡す")
        public void passNull() {
            boolean actual = Utils.isBlank(null);
            assertTrue(actual);
        }

        @ParameterizedTest
        @CsvSource({"''", "' '", "'\t'"})
        public void tests(String s) {
            boolean actual = Utils.isBlank(s);
            assertTrue(actual);
        }

        @Test
        @DisplayName("文字列を渡す")
        public void passNormalStr() {
            boolean actual = Utils.isBlank("test");
            assertFalse(actual);
        }
    }

    @Test
    @DisplayName("[strToIntメソッドのテスト]")
    public void testStrToInt() {
        int actual = Utils.strToInt("09:30");
        assertThat(actual).isEqualTo(570);
    }

    @Nested
    @DisplayName("[strToDateメソッドのテスト]")
    public class NestedTestStrToDate {

        @Test
        @DisplayName("正常系")
        public void success() {
            java.sql.Date actual = Utils.strToDate("2022-04-30");

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(2022, Calendar.APRIL, 30);

            java.sql.Date expected = new java.sql.Date(calendar.getTimeInMillis());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("異常系")
        public void fail() {
            java.sql.Date actual = Utils.strToDate("");
            assertNull(actual);
        }
    }

    @Nested
    @DisplayName("[checkTimeFormatメソッドのテスト]")
    public class NestedTestCheckTimeFormat {

        @Test
        @DisplayName("正常系")
        public void success() {
            boolean actual = Utils.checkTimeFormat("09:30");
            assertTrue(actual);
        }

        @Test
        @DisplayName("異常系")
        public void fail() {
            boolean actual = Utils.checkTimeFormat("");
            assertFalse(actual);
        }
    }

    @Nested
    @DisplayName("[strToTimeメソッドのテスト]")
    public class NestedTestStrToTime {

        @Test
        @DisplayName("正常系")
        public void success() {
            java.sql.Time actual = Utils.strToTime("09:30");

            Calendar calendar = Calendar.getInstance();
            calendar.clear();
            calendar.set(Calendar.HOUR_OF_DAY, 9);
            calendar.set(Calendar.MINUTE, 30);

            java.sql.Time expected = new java.sql.Time(calendar.getTimeInMillis());
            assertThat(actual).isEqualTo(expected);
        }

        @Test
        @DisplayName("異常系")
        public void fail() {
            java.sql.Time actual = Utils.strToTime("");
            assertNull(actual);
        }
    }

    @Test
    public void testDateToStr() {

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(2022, Calendar.APRIL, 30);

        java.sql.Date sqlDate = new java.sql.Date(calendar.getTimeInMillis());

        String actual = Utils.dateToStr(sqlDate);
        String expected = "2022/04/30";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testTimeToStr() {

        Calendar calendar = Calendar.getInstance();
        calendar.clear();
        calendar.set(Calendar.HOUR_OF_DAY, 9);
        calendar.set(Calendar.MINUTE, 30);

        java.sql.Time sqlTime = new java.sql.Time(calendar.getTimeInMillis());

        String actual = Utils.timeToStr(sqlTime);
        String expected = "09:30";
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    @DisplayName("[dateAndTimeToTimestampメソッドのテスト]")
    public void testDateAndTimeToTimestamp() {
        Calendar calendarDate = Calendar.getInstance();
        calendarDate.clear();
        calendarDate.set(2022, Calendar.APRIL, 30);
        long msDate = calendarDate.getTimeInMillis();
        java.sql.Date sqlDate = new java.sql.Date(msDate);

        Calendar calendarTime = Calendar.getInstance();
        calendarTime.clear();
        calendarTime.set(Calendar.HOUR_OF_DAY, 9);
        calendarTime.set(Calendar.MINUTE, 30);
        long msTime = calendarTime.getTimeInMillis();
        java.sql.Time sqlTime = new java.sql.Time(msTime);

        java.sql.Timestamp actual = Utils.dateAndTimeToTimestamp(sqlDate, sqlTime);
        java.sql.Timestamp expected = java.sql.Timestamp.valueOf("2022-04-30 09:30:00");

        assertThat(actual).isEqualTo(expected);
    }

}