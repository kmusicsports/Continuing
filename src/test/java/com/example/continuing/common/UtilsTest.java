package com.example.continuing.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

}