package uk.gov.hmcts.payment.api.service;

import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;

import java.util.List;

public interface OrderCasesService<T, ID>{

    String createOrder(CaseDetails caseDetails, PaymentFeeLink paymentFeeLink);
}
