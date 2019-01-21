package uk.gov.hmcts.payment.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.payment.api.dto.PaymentSearchCriteria;
import uk.gov.hmcts.payment.api.dto.PciPalPayment;
import uk.gov.hmcts.payment.api.external.client.dto.State;
import uk.gov.hmcts.payment.api.model.PaymentFee;

import java.util.Date;
import java.util.List;

@Service
public class PciPalPaymentService implements DelegatingPaymentService<PciPalPayment, String> {
    private static final Logger LOG = LoggerFactory.getLogger(PciPalPaymentService.class);

    @Override
    public PciPalPayment create(String paymentReference, String description, String returnUrl, String ccdCaseNumber,
                                String caseReference, String currency, String siteId, String serviceType,
                                List<PaymentFee> fees, int amount, String serviceCallbackUrl, String channel,
                                String provider) {
        PciPalPayment payment = PciPalPayment.pciPalPaymentWith().paymentId("spoof_id")
            .state(State.stateWith().code("code").finished(false).message("message").status("status").build()).build();
        LOG.info("PciPal service called, returning with: {}", payment);
        return payment;
    }

    @Override
    public PciPalPayment retrieve(String s) {
        return null;
    }

    @Override
    public PciPalPayment retrieve(String s, String paymentTargetService) {
        return null;
    }

    @Override
    public List<PciPalPayment> search(PaymentSearchCriteria searchCriteria) {
        return null;
    }
}
