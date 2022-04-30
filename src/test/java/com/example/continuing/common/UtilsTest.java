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

}