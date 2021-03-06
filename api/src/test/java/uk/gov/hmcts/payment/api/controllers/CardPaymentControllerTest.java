package uk.gov.hmcts.payment.api.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import lombok.SneakyThrows;
import org.ff4j.FF4j;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ResourceUtils;
import org.springframework.web.context.WebApplicationContext;
import uk.gov.hmcts.fees.register.legacymodel.Fee;
import uk.gov.hmcts.payment.api.configuration.LaunchDarklyFeatureToggler;
import uk.gov.hmcts.payment.api.contract.CardPaymentRequest;
import uk.gov.hmcts.payment.api.contract.PaymentDto;
import uk.gov.hmcts.payment.api.contract.util.Service;
import uk.gov.hmcts.payment.api.dto.PaymentServiceRequest;
import uk.gov.hmcts.payment.api.dto.PciPalPaymentRequest;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentDtoMapper;
import uk.gov.hmcts.payment.api.external.client.dto.CardDetails;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.service.CardDetailsService;
import uk.gov.hmcts.payment.api.service.DelegatingPaymentService;
import uk.gov.hmcts.payment.api.service.FeePayApportionService;
import uk.gov.hmcts.payment.api.service.PciPalPaymentService;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.ServiceResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.backdoors.UserResolverBackdoor;
import uk.gov.hmcts.payment.api.v1.componenttests.sugar.RestActions;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.*;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.MOCK;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.util.SystemPropertyUtils.resolvePlaceholders;

@RunWith(SpringRunner.class)
@ActiveProfiles({"local", "componenttest"})
@SpringBootTest(webEnvironment = MOCK)
public class CardPaymentControllerTest {
    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(9190);

    @Rule
    public WireMockClassRule instanceRule = wireMockRule;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private ServiceResolverBackdoor serviceRequestAuthorizer;

    @Autowired
    private UserResolverBackdoor userRequestAuthorizer;

    private static final String USER_ID = UserResolverBackdoor.CITIZEN_ID;

    private RestActions restActions;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private LaunchDarklyFeatureToggler featureToggler;

    @MockBean
    private CardDetailsService<CardDetails, String> cardDetailsService;

    @MockBean
    private PciPalPaymentService pciPalPaymentService;

    @MockBean
    private FeePayApportionService feePayApportionService;

    @MockBean
    private DelegatingPaymentService<PaymentFeeLink, String> delegatingPaymentService;

    @MockBean
    private FF4j ff4j;

    @MockBean
    private PaymentDtoMapper paymentDtoMapper;

    @Before
    public void setup() {
        MockMvc mvc = webAppContextSetup(webApplicationContext).apply(springSecurity()).build();
        this.restActions = new RestActions(mvc, serviceRequestAuthorizer, userRequestAuthorizer, objectMapper);

        restActions
            .withAuthorizedService("divorce")
            .withAuthorizedUser(USER_ID)
            .withUserId(USER_ID)
            .withReturnUrl("https://www.moneyclaims.service.gov.uk");
    }


    @Test
    public void createCardPaymentWithValidInputData_shouldReturnStatusCreatedTest() throws Exception {
        when(delegatingPaymentService.create(Mockito.any(PaymentServiceRequest.class))).thenReturn(mock(PaymentFeeLink.class));
        when( pciPalPaymentService.getPciPalLink(Mockito.any(PciPalPaymentRequest.class), Mockito.any(String.class))).thenReturn("link");
        when(featureToggler.getBooleanValue(Mockito.any(String.class),Mockito.anyBoolean())).thenReturn(false);
        MvcResult result = restActions
            .withHeader("service-callback-url", "http://payments.com")
            .post("/card-payments", cardPaymentRequest())
            .andExpect(status().isCreated())
            .andReturn();
    }

    @Test
    public void shouldGetPaymentForValidReference() throws Exception {
        when(delegatingPaymentService.retrieve(Mockito.anyString())).thenReturn(mock(PaymentFeeLink.class));
        when(paymentDtoMapper.toRetrieveCardPaymentResponseDto(any(PaymentFeeLink.class))).thenReturn(PaymentDto.payment2DtoWith().build());
        restActions
            .get("/card-payments/123123123123123")
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldRetrieveWithCardDetails() throws Exception {
        CardDetails cardDetails = CardDetails.cardDetailsWith()
                                        .lastDigitsCardNumber("1234")
                                        .build();
        when(cardDetailsService.retrieve(anyString())).thenReturn(cardDetails);
        restActions
            .get("/card-payments/123123123123123/details")
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldRetrievePaymentStatus() throws Exception {
        StatusHistory statusHistory = StatusHistory.statusHistoryWith().status("Initiated").externalStatus("created").build();
        Payment payment1 = Payment.paymentWith()
            .reference("RC-1612-3710-5335-6484")
            .id(1).build();
        PaymentFeeLink paymentFeeLink = PaymentFeeLink.paymentFeeLinkWith()
            .payments(Collections.singletonList(payment1))
            .build();
        when(delegatingPaymentService.retrieve(Mockito.anyString())).thenReturn(paymentFeeLink);
        when(paymentDtoMapper.toPaymentStatusesDto(Mockito.any(Payment.class))).thenReturn(PaymentDto.payment2DtoWith().build());
        restActions
            .get("/card-payments/RC-1612-3710-5335-6484/statuses")
            .andExpect(status().isOk())
            .andReturn();
    }

    @Test
    public void shouldCallPaymentCancelFunction() throws Exception {
        when(ff4j.check(anyString())).thenReturn(true);
        doNothing().when(delegatingPaymentService).cancel(anyString());
        restActions
            .post("/card-payments/RC-1612-3710-5335-6484/cancel")
            .andExpect(status().isNoContent())
            .andReturn();
    }

    @Test
    public void shouldCallPaymentCancelFunction_ThrowsPaymentException() throws Exception {
        when(ff4j.check(anyString())).thenReturn(false);
        doNothing().when(delegatingPaymentService).cancel(anyString());
        restActions
            .post("/card-payments/RC-1612-3710-5335-6484/cancel")
            .andExpect(status().isBadRequest())
            .andReturn();
    }

    private CardPaymentRequest cardPaymentRequest() throws Exception {
        return objectMapper.readValue(requestJson().getBytes(), CardPaymentRequest.class);
    }

    protected String requestJson() {
        return "{\n" +
            "  \"amount\": 101.89,\n" +
            "  \"description\": \"New passport application\",\n" +
            "  \"ccd_case_number\": \"CCD101\",\n" +
            "  \"channel\": \"telephony\",\n" +
            "  \"case_reference\": \"12345\",\n" +
            "  \"service\": \"PROBATE\",\n" +"  \"language\": \"cy\",\n" +
            "  \"currency\": \"GBP\",\n" +
            "  \"return_url\": \"https://www.moneyclaims.service.gov.uk\",\n" +
            "  \"site_id\": \"AA101\",\n" +
            "  \"fees\": [\n" +
            "    {\n" +
            "      \"calculated_amount\": 101.89,\n" +
            "      \"code\": \"X0101\",\n" +
            "      \"version\": \"1\"\n" +
            "    }\n" +
            "  ]\n" +
            "}";
    }
}
