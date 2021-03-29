package uk.gov.hmcts.payment.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.CaseDetailsRepository;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;
import uk.gov.hmcts.payment.api.model.PaymentFeeLinkRepository;

import java.util.Arrays;
import java.util.stream.Collectors;

@Service
public class OrderCasesServiceImpl implements OrderCasesService<PaymentFeeLink,String> {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCasesServiceImpl.class);

    @Autowired
    private CaseDetailsRepository caseDetailsRepository;

    @Autowired
    private PaymentFeeLinkRepository paymentFeeLinkRepository;

    @Override
    public String createOrder (CaseDetails caseDetails, PaymentFeeLink pf){

        LOG.info("CaseDetails {}",caseDetails.getCcdCaseNumber());
        caseDetailsRepository.save(caseDetails);
        LOG.info("CaseRef {}",caseDetails.getCaseReference());
        paymentFeeLinkRepository.save(pf);
        LOG.info("asdfghjklkjhgfdsdfghjkjhgvc");
        return "asdfghjk";
    }
}
