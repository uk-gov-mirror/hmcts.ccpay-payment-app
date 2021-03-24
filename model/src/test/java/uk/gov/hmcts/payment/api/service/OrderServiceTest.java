package uk.gov.hmcts.payment.api.service;

import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.junit4.SpringRunner;
import uk.gov.hmcts.payment.api.dto.OrganisationalServiceDto;
import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.CaseDetailsRepository;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;
import uk.gov.hmcts.payment.api.model.PaymentFeeLinkRepository;
import uk.gov.hmcts.payment.api.service.OrderCasesService;
import uk.gov.hmcts.payment.api.service.OrderCasesServiceImpl;
import uk.gov.hmcts.payment.referencedata.controllers.ReferenceDataController;
import uk.gov.hmcts.payment.referencedata.dto.SiteDTO;
import uk.gov.hmcts.payment.referencedata.model.Site;
import uk.gov.hmcts.payment.referencedata.service.SiteService;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;

public class OrderServiceTest {

    @Autowired
    private OrderCasesService orderCasesService;

    @Autowired
    private CaseDetailsRepository caseDetailsRepository;

    @Autowired
    private PaymentFeeLinkRepository paymentFeeLinkRepository;


    @Test
    public void sampleTest() throws Exception{

        CaseDetails cd = CaseDetails.caseDetailsWith()
            .caseReference("request.getCaseReference()")
            .ccdCaseNumber("request.getCcdCaseNumber()")
            .orders(null)
            .build();

        PaymentFeeLink pf = PaymentFeeLink.paymentFeeLinkWith()
            .enterpriseServiceName("organisationalServiceDto.getServiceDescription()")
            .orgId("organisationalServiceDto.getServiceCode()")
            .caseDetails((Set<CaseDetails>) cd)
            .build();

        orderCasesService.createOrder(cd,pf);

    }
}
