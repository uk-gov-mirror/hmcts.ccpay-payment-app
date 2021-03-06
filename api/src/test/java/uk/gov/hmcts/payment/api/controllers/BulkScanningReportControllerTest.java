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
import uk.gov.hmcts.payment.api.contract.BulkScanningUnderOverPaymentDto;
import uk.gov.hmcts.payment.api.dto.RemissionRequest;
import uk.gov.hmcts.payment.api.dto.mapper.BulkScanningReportMapper;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.service.PaymentService;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.sugar.RestActions;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.hibernate.type.descriptor.java.JdbcDateTypeDescriptor.DATE_FORMAT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
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

    private static final String USER_ID = UserResolverBackdoor.CASEWORKER_ID;

    @MockBean
    private PaymentService<PaymentFeeLink, String> paymentService;

    @MockBean
    private BulkScanningReportMapper bulkScanningReportMapper;

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
        when(paymentService.getPayments(any(Date.class), any(Date.class))).thenReturn(Arrays.asList(mock(Payment.class)));
        when(bulkScanningReportMapper.toBulkScanningUnallocatedReportDto(anyList())).thenReturn(Arrays.asList(BulkScanningReportDto.report2DtoWith().build()));
        String startDate = LocalDate.now().minusDays(1).toString(DATE_FORMAT);
        String endDate = LocalDate.now().toString(DATE_FORMAT);
        MvcResult result = restActions
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .get("/payment/bulkscan-data-report?date_from=" + startDate + "&date_to=" + endDate + "&report_type=PROCESSED_UNALLOCATED")
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void testGetBulkScanReports_WithSurPlusShortFall() throws Exception {
        when(paymentService.getPayments(any(Date.class), any(Date.class))).thenReturn(Arrays.asList(mock(Payment.class)));
        when(bulkScanningReportMapper.toSurplusAndShortfallReportdto(anyList())).thenReturn(Arrays.asList(BulkScanningUnderOverPaymentDto.report2DtoWith().build()));
        String startDate = LocalDate.now().minusDays(1).toString(DATE_FORMAT);
        String endDate = LocalDate.now().toString(DATE_FORMAT);
        MvcResult result = restActions
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .get("/payment/bulkscan-data-report?date_from=" + startDate + "&date_to=" + endDate + "&report_type=SURPLUS_AND_SHORTFALL")
            .andExpect(status().isOk())
            .andReturn();
    }

}
