package uk.gov.hmcts.payment.api.service;

import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;

public interface OrderCasesService<T, ID>{
    void createOrder(CaseDetails caseDetails, PaymentFeeLink pf);
}
