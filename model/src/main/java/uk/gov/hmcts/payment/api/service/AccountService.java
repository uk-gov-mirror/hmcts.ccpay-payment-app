package uk.gov.hmcts.payment.api.service;

public interface AccountService<T, I, R> {
    T retrieve(I id);
    T post(R request);
}
