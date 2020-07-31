package uk.gov.hmcts.payment.api.controllers;

import io.swagger.annotations.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.payment.api.dto.PaymentGroupDto;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentGroupDtoMapper;
import uk.gov.hmcts.payment.api.model.*;
import uk.gov.hmcts.payment.api.service.PaymentService;
import uk.gov.hmcts.payment.api.util.DateFormatter;
import uk.gov.hmcts.payment.api.v1.model.exceptions.PaymentNotFoundException;

import java.util.List;
import java.util.Optional;

@RestController
@Api(tags = {"PaymentApportion"})
@SwaggerDefinition(tags = {@Tag(name = "FeePayApportionController", description = "FeePayApportion REST API")})
public class FeePayApportionController {

    private static final Logger LOG = LoggerFactory.getLogger(CardPaymentController.class);

    private final PaymentService<PaymentFeeLink, String> paymentService;

    private final PaymentFeeRepository paymentFeeRepository;

    private final PaymentGroupDtoMapper paymentGroupDtoMapper;

    private final DateFormatter dateFormatter;


    @Value("${apportion.live.date}")
    private String apportionLiveDate;

    @Autowired
    public FeePayApportionController(PaymentService<PaymentFeeLink, String> paymentService,PaymentFeeRepository paymentFeeRepository,PaymentGroupDtoMapper paymentGroupDtoMapper,DateFormatter dateFormatter) {
        this.paymentService = paymentService;
        this.paymentFeeRepository = paymentFeeRepository;
        this.paymentGroupDtoMapper = paymentGroupDtoMapper;
        this.dateFormatter = dateFormatter;
    }

    @ApiOperation(value = "Get apportion details by payment reference", notes = "Get apportion details for supplied payment reference")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Apportionment Details retrieved"),
        @ApiResponse(code = 403, message = "Payment info forbidden"),
        @ApiResponse(code = 404, message = "Payment not found")
    })
    @GetMapping(value = "/payment-groups/fee-pay-apportion/{paymentreference}")
    public ResponseEntity<PaymentGroupDto> retrieveApportionDetails(@PathVariable("paymentreference") String paymentReference) {

        LOG.info("Apportionment Details to be retrieved by Payment Reference : {}", paymentReference);
        PaymentFeeLink paymentFeeLink = paymentService.retrieve(paymentReference);

        Optional<Payment> payment = paymentFeeLink.getPayments().stream()
            .filter(p -> p.getReference().equals(paymentReference)).findAny();
        List<PaymentFee> feeList = paymentFeeLink.getFees();
        if ((payment.isPresent() && (payment.get().getDateCreated().after(dateFormatter.parseDate(apportionLiveDate)) ||
            payment.get().getDateCreated().equals(dateFormatter.parseDate(apportionLiveDate)))))
        {
                List<FeePayApportion> feePayApportionList = paymentService.findByPaymentId(payment.get().getId());
                if(feePayApportionList.isEmpty()) {
                    LOG.info("Apportionment Empty for Payment Reference : {}", paymentReference);
                } else {
                    LOG.info("Count Apportionment for Payment Reference : {}", feePayApportionList.size());
                    feePayApportionList.stream()
                        .forEach(feePayApportion -> {
                            feeList.stream()
                                .forEach(paymentFee -> {
                                    if (feePayApportion.getFeeId().equals(paymentFee.getId())) {
                                        PaymentFee fee = paymentFeeRepository.findById(feePayApportion.getFeeId()).get();
                                        fee.setApportionAmount(feePayApportion.getApportionAmount());
                                    }
                                });
                        });
                    paymentFeeLink.setFees(feeList);
                }
    }
        return new ResponseEntity<>(paymentGroupDtoMapper.toPaymentGroupDto(paymentFeeLink), HttpStatus.OK);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(PaymentNotFoundException.class)
    public String notFound(PaymentNotFoundException ex) {
        return ex.getMessage();
    }
}
