package com.example.continuing.common;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

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

        @Test
        @DisplayName("空文字を渡す")
        public void passEmptyChar() {
            boolean actual = Utils.isAllDoubleSpace("");
            assertTrue(actual);
        }

        @Test
        @DisplayName("全角スペースを渡す")
        public void passAllDoubleSpace() {
            boolean actual = Utils.isAllDoubleSpace("　");
            assertTrue(actual);
        }

        @Test
        @DisplayName("文字列を渡す")
        public void passNormalStr() {
            boolean actual = Utils.isAllDoubleSpace("test");
            assertFalse(actual);
        }
    }

}