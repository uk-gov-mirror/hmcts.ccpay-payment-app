package uk.gov.hmcts.payment.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.payment.api.contract.PaymentDto;
import uk.gov.hmcts.payment.api.dto.PaymentGroupDto;
import uk.gov.hmcts.payment.api.dto.PaymentSearchCriteria;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentDtoMapper;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentGroupDtoMapper;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.service.FeesService;
import uk.gov.hmcts.payment.api.service.PaymentGroupService;
import uk.gov.hmcts.payment.api.service.PaymentService;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.sugar.RestActions;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
public class CaseControllerTest {
    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    protected ServiceResolverBackdoor serviceRequestAuthorizer;

    @Autowired
    protected UserResolverBackdoor userRequestAuthorizer;

    RestActions restActions;

    @MockBean
    private PaymentFeeLinkRepository paymentFeeLinkRepository;


    @Autowired
    private ObjectMapper objectMapper;

    private static final String USER_ID = UserResolverBackdoor.CASEWORKER_ID;

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
    public void testRetrieveCasePayments() throws Exception {
        when(paymentFeeLinkRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(getPaymentFeeLink()));
        MvcResult result = restActions
            .get("/cases/12341234213412/payments")
            .andExpect(status().isOk())
            .andReturn();
    }


    @Test
    public void testRetrieveCasePayments_ThrowsPaymentNotFound() throws Exception {
      when(paymentFeeLinkRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList());
        MvcResult result = restActions
            .get("/cases/12341234213412/payments")
            .andExpect(status().isNotFound())
            .andReturn();
    }


    @Test
    public void testRetrieveCasePaymentGroups() throws Exception {
        when(paymentFeeLinkRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(getPaymentFeeLink()));
        MvcResult result = restActions
            .get("/cases/12341234213412/paymentgroups")
            .andExpect(status().isOk())
            .andReturn();
    }


    @Test
    public void testRetrieveCasePaymentGroups_ThrowsPaymentGroupNotFoundException() throws Exception {
        when(paymentFeeLinkRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList());
        MvcResult result = restActions
            .get("/cases/12341234213412/paymentgroups")
            .andExpect(status().isNotFound())
            .andReturn();
    }


    private PaymentFeeLink getPaymentFeeLink(){
        List<PaymentFee> paymentFees = new ArrayList<>();
        PaymentFee fee = PaymentFee.feeWith()
            .feeAmount(BigDecimal.valueOf(30))
            .calculatedAmount(BigDecimal.valueOf(101.89))
            .code("X0101")
            .ccdCaseNumber("CCD101")
            .build();
        paymentFees.add(fee);
        Payment payment = Payment.paymentWith()
            .paymentStatus(PaymentStatus.SUCCESS)
            .status("success")
            .paymentChannel(PaymentChannel.paymentChannelWith().name("card").build())
            .currency("GBP")
            .caseReference("case-reference")
            .ccdCaseNumber("ccd-number")
            .paymentMethod(PaymentMethod.paymentMethodWith().name("cash").build())
            .dateCreated(Date.valueOf("2020-02-01"))
            .externalReference("external-reference")
            .reference("2021-1614709196068")
            .build();
        List<Payment> paymentList = new ArrayList<>();
        paymentList.add(payment);
        return PaymentFeeLink.paymentFeeLinkWith()
            .paymentReference("2021-1614709196068")
            .dateCreated(Date.valueOf("2020-01-20"))
            .dateUpdated(Date.valueOf("2020-01-21"))
            .fees(paymentFees)
            .payments(paymentList)
            .build();
    }
}
