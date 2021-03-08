package uk.gov.hmcts.payment.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.joda.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.payment.api.contract.BulkScanningReportDto;
import uk.gov.hmcts.payment.api.dto.RemissionRequest;
import uk.gov.hmcts.payment.api.dto.mapper.BulkScanningReportMapper;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.service.PaymentService;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.sugar.RestActions;
import uk.gov.hmcts.payment.api.v1.model.PaymentRepository;

import java.math.BigDecimal;
import java.util.*;

import static org.hibernate.type.descriptor.java.JdbcDateTypeDescriptor.DATE_FORMAT;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static uk.gov.hmcts.payment.api.model.PaymentFee.feeWith;
import static uk.gov.hmcts.payment.api.util.DateUtil.atEndOfDay;
import static uk.gov.hmcts.payment.api.util.DateUtil.atStartOfDay;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
public class BulkScanningReportControllerTest {

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected ServiceResolverBackdoor serviceRequestAuthorizer;

    @Autowired
    protected UserResolverBackdoor userRequestAuthorizer;

    @Autowired
    private ObjectMapper objectMapper;

    private String USER_ID = UserResolverBackdoor.CASEWORKER_ID;

    @MockBean
    private Payment2Repository paymentRepository;

    RestActions restActions;

    @Before
    public void setUp(){
        MockMvc mvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        this.restActions = new RestActions(mvc, serviceRequestAuthorizer, userRequestAuthorizer, objectMapper);
        restActions
            .withAuthorizedService("divorce")
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .withReturnUrl("https://www.moneyclaims.service.gov.uk");
    }

    @Test
    public void testGetBulkScanReports_WithProcessedUnAllocated() throws Exception {
        when(paymentRepository.findAllByDateCreatedBetween(any(Date.class),any(Date.class))).thenReturn(getPayments());
        String startDate = LocalDate.now().minusDays(1).toString(DATE_FORMAT);
        String endDate = LocalDate.now().toString(DATE_FORMAT);
        restActions
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .get("/payment/bulkscan-data-report?date_from=" + startDate + "&date_to=" + endDate + "&report_type=PROCESSED_UNALLOCATED")
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGetBulkScanReports_WithSurPlusShortFall() throws Exception {
        when(paymentRepository.findAllByDateCreatedBetween(any(Date.class),any(Date.class))).thenReturn(getPayments());
        String startDate = LocalDate.now().minusDays(1).toString(DATE_FORMAT);
        String endDate = LocalDate.now().toString(DATE_FORMAT);
        restActions
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .get("/payment/bulkscan-data-report?date_from=" + startDate + "&date_to=" + endDate + "&report_type=SURPLUS_AND_SHORTFALL")
            .andExpect(status().isOk())
            .andReturn();
    }

    private Optional<List<Payment>> getPayments(){
        Payment payment =  Payment.paymentWith()
            .paymentStatus(PaymentStatus.SUCCESS)
            .status("success")
            .paymentChannel(PaymentChannel.paymentChannelWith().name("card").build())
            .currency("GBP")
            .caseReference("case-reference")
            .ccdCaseNumber("ccd-number")
            .paymentMethod(PaymentMethod.paymentMethodWith().name("cash").build())
            .dateCreated(java.sql.Date.valueOf("2020-02-01"))
            .externalReference("external-reference")
            .reference("RC-1234-1234-1234-1234")
            .paymentLink(getPaymentFeeLink())
            .build();
        return Optional.of(Arrays.asList(payment));
    }


    private PaymentFeeLink getPaymentFeeLink(){
        PaymentFee fee = PaymentFee.feeWith()
            .feeAmount(BigDecimal.valueOf(30))
            .calculatedAmount(BigDecimal.valueOf(10))
            .code("FEE-123")
            .build();
        return PaymentFeeLink.paymentFeeLinkWith()
            .paymentReference("RC-1234-1234-1234-1234")
            .dateCreated(java.sql.Date.valueOf("2020-01-20"))
            .dateUpdated(java.sql.Date.valueOf("2020-01-21"))
            .fees(Arrays.asList(fee))
            .payments(Arrays.asList( Payment.paymentWith()
                .paymentStatus(PaymentStatus.SUCCESS)
                .status("success")
                .paymentChannel(PaymentChannel.paymentChannelWith().name("card").build())
                .currency("GBP")
                .caseReference("case-reference")
                .ccdCaseNumber("ccd-number")
                .paymentMethod(PaymentMethod.paymentMethodWith().name("cash").build())
                .dateCreated(java.sql.Date.valueOf("2020-02-01"))
                .externalReference("external-reference")
                .reference("RC-1234-1234-1234-1234")
                .build()))
            .build();
    }
}
