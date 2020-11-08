package uk.gov.hmcts.payment.api.service;

import org.springframework.data.domain.Page;
import uk.gov.hmcts.payment.api.dto.PaymentSearchCriteria;
import uk.gov.hmcts.payment.api.dto.Reference;
import uk.gov.hmcts.payment.api.model.FeePayApportion;
import uk.gov.hmcts.payment.api.model.Payment;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;

import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface PaymentService<T, ID> {

    T retrieve(ID id);

    List<Reference> listInitiatedStatusPaymentsReferences();

    List<T> search(PaymentSearchCriteria searchCriteria);

    void updateTelephonyPaymentStatus(String reference, String status, String payload);

    List<Payment> getPayments(Date atStartOfDay, Date atEndOfDay);

    List<FeePayApportion> findByPaymentId(Integer paymentId);

    Page<PaymentFeeLink> search1(PaymentSearchCriteria searchCriteria);

}
