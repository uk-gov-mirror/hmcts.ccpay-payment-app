package uk.gov.hmcts.payment.api.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.CaseDetailsRepository;

import uk.gov.hmcts.payment.api.model.OrderCases;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;
import uk.gov.hmcts.payment.api.model.PaymentFeeLinkRepository;

import java.util.Collections;
import java.util.Set;


@Service
public class OrderCasesServiceImpl implements OrderCasesService {

    private static final Logger LOG = LoggerFactory.getLogger(OrderCasesServiceImpl.class);

    @Autowired
    private CaseDetailsRepository caseDetailsRepository;

    @Autowired
    private PaymentFeeLinkRepository paymentFeeLinkRepository;

    @Autowired
    private CaseDetails cd;

    @Autowired
    private PaymentFeeLink paymentFeeLink;

    @Override
    public void createOrder ( CaseDetails caseDetails, PaymentFeeLink pf){

        LOG.info("CaseDetails {}",caseDetails.getCcdCaseNumber());
        caseDetailsRepository.save(caseDetails);
        paymentFeeLinkRepository.save(pf);

//        caseDetailsRepository.save(caseDetails);
        LOG.info("asdfghjklkjhgfdsdfghjkjhgvc");

    }
}
