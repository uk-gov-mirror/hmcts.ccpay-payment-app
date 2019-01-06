package uk.gov.hmcts.payment.api.exception;

public class ExceededCreditLimitException extends RuntimeException {
    public ExceededCreditLimitException(String message) {
        super(message);
    }
}
