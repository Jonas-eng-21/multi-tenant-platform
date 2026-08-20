package br.com.jonas.multitenant.common.validation;

public final class CpfValidator {

    private CpfValidator() {
    }

    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.isBlank()) {
            return false;
        }

        String digits = cpf.replaceAll("[.\\-]", "");

        if (!digits.matches("\\d{11}")) {
            return false;
        }

        if (digits.chars().distinct().count() == 1) {
            return false;
        }

        int firstDigit = calculateDigit(digits, 10);
        int secondDigit = calculateDigit(digits, 11);

        return digits.charAt(9) - '0' == firstDigit
                && digits.charAt(10) - '0' == secondDigit;
    }

    private static int calculateDigit(String digits, int weight) {
        int sum = 0;
        int count = weight - 1;

        for (int i = 0; i < count; i++) {
            sum += (digits.charAt(i) - '0') * (weight - i);
        }

        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
