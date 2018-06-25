package uk.gov.hmcts.payment.api.scheduler;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.hmcts.payment.api.reports.FeesService;
import uk.gov.hmcts.payment.api.reports.PaymentsReportService;

import java.util.Date;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
@RunWith(MockitoJUnitRunner.class)
public class CardPaymentsReportSchedulerTest {


    @Mock
    private PaymentsReportService paymentsReportService;

    @Mock
    private FeesService feesService;

    private CardPaymentsReportScheduler cardPaymentsReportScheduler;


    @Before
    public void setUp() {
        cardPaymentsReportScheduler = new CardPaymentsReportScheduler(paymentsReportService,feesService);
    }

    @Test
    public void shouldInvokeGenerateCardPaymentsReport() {
        // given

        // when
        cardPaymentsReportScheduler.generateCardPaymentsReportTask();

        // then
        verify(feesService).dailyRefreshOfFeesData();
        verify(paymentsReportService).generateCardPaymentsCsvAndSendEmail(any(Date.class), any(Date.class));
    }

}

