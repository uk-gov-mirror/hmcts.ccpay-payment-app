package uk.gov.hmcts.payment.api.exception;

public class CaseInformationTooLargeException extends RuntimeException {
    public CaseInformationTooLargeException(String message) {
        super(message);
    }
}
