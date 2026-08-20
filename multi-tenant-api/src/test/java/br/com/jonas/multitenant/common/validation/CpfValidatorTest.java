package br.com.jonas.multitenant.common.validation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class CpfValidatorTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "54647142949",   
            "546.471.429-49" 
    })
    void isValid_validCpf_returnsTrue(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "11438374798",
            "72935710037",
            "34759211039"
    })
    void isValid_otherValidCpfs_returnsTrue(String cpf) {
        assertTrue(CpfValidator.isValid(cpf));
    }

    @Test
    void isValid_firstDigitIncorrect_returnsFalse() {
        assertFalse(CpfValidator.isValid("54647142049"));
    }

    @Test
    void isValid_secondDigitIncorrect_returnsFalse() {
        assertFalse(CpfValidator.isValid("54647142940"));
    }

    @Test
    void isValid_tenDigits_returnsFalse() {
        assertFalse(CpfValidator.isValid("5464714294"));
    }

    @Test
    void isValid_twelveDigits_returnsFalse() {
        assertFalse(CpfValidator.isValid("546471429490"));
    }

    @Test
    void isValid_containsLetters_returnsFalse() {
        assertFalse(CpfValidator.isValid("5464714294a"));
    }

    @Test
    void isValid_containsSpecialCharacters_returnsFalse() {
        assertFalse(CpfValidator.isValid("54647142@49"));
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "00000000000",
            "11111111111",
            "22222222222",
            "33333333333",
            "44444444444",
            "55555555555",
            "66666666666",
            "77777777777",
            "88888888888",
            "99999999999"
    })
    void isValid_allSameDigits_returnsFalse(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void isValid_nullOrEmpty_returnsFalse(String cpf) {
        assertFalse(CpfValidator.isValid(cpf));
    }

    @Test
    void isValid_blankString_returnsFalse() {
        assertFalse(CpfValidator.isValid("   "));
    }

    @Test
    void isValid_maskedValidCpf_returnsTrue() {
        assertTrue(CpfValidator.isValid("114.383.747-98"));
    }

    @Test
    void isValid_unmaskedValidCpf_returnsTrue() {
        assertTrue(CpfValidator.isValid("11438374798"));
    }
}
