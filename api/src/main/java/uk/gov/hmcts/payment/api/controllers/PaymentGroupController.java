package uk.gov.hmcts.payment.api.controllers;

import com.google.common.collect.Lists;
import io.swagger.annotations.*;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.validator.routines.checkdigit.CheckDigitException;
import org.apache.http.MethodNotSupportedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import uk.gov.hmcts.payment.api.contract.CardPaymentRequest;
import uk.gov.hmcts.payment.api.contract.PaymentDto;
import uk.gov.hmcts.payment.api.dto.PaymentGroupDto;
import uk.gov.hmcts.payment.api.dto.PaymentServiceRequest;
import uk.gov.hmcts.payment.api.dto.PciPalPaymentRequest;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentDtoMapper;
import uk.gov.hmcts.payment.api.dto.mapper.PaymentGroupDtoMapper;
import uk.gov.hmcts.payment.api.model.Payment;
import uk.gov.hmcts.payment.api.model.PaymentFee;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;
import uk.gov.hmcts.payment.api.service.DelegatingPaymentService;
import uk.gov.hmcts.payment.api.service.PaymentGroupService;
import uk.gov.hmcts.payment.api.service.PciPalPaymentService;
import uk.gov.hmcts.payment.api.util.ReferenceUtil;
import uk.gov.hmcts.payment.api.v1.model.exceptions.InvalidFeeRequestException;
import uk.gov.hmcts.payment.api.v1.model.exceptions.InvalidPaymentGroupReferenceException;
import uk.gov.hmcts.payment.api.v1.model.exceptions.PaymentNotFoundException;

import javax.validation.ConstraintViolationException;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Api(tags = {"Payment group"})
@SwaggerDefinition(tags = {@Tag(name = "PaymentGroupController", description = "Payment group REST API")})
@Validated
public class PaymentGroupController {

    private final PaymentGroupService<PaymentFeeLink, String> paymentGroupService;

    private final PaymentGroupDtoMapper paymentGroupDtoMapper;

    private final DelegatingPaymentService<PaymentFeeLink, String> delegatingPaymentService;

    private final PaymentDtoMapper paymentDtoMapper;

    private final PciPalPaymentService pciPalPaymentService;

    private final ReferenceUtil referenceUtil;


    @Autowired
    public PaymentGroupController(PaymentGroupService paymentGroupService, PaymentGroupDtoMapper paymentGroupDtoMapper,
                                  DelegatingPaymentService<PaymentFeeLink, String> delegatingPaymentService,
                                  PaymentDtoMapper paymentDtoMapper, PciPalPaymentService pciPalPaymentService,
                                  ReferenceUtil referenceUtil) {
        this.paymentGroupService = paymentGroupService;
        this.paymentGroupDtoMapper = paymentGroupDtoMapper;
        this.delegatingPaymentService = delegatingPaymentService;
        this.paymentDtoMapper = paymentDtoMapper;
        this.pciPalPaymentService = pciPalPaymentService;
        this.referenceUtil = referenceUtil;
    }

    @ApiOperation(value = "Get payments/remissions/fees details by payment group reference", notes = "Get payments/remissions/fees details for supplied payment group reference")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Payment retrieved"),
        @ApiResponse(code = 403, message = "Payment info forbidden"),
        @ApiResponse(code = 404, message = "Payment not found")
    })
    @GetMapping(value = "/payment-groups/{payment-group-reference}")
    public ResponseEntity<PaymentGroupDto> retrievePayment(@PathVariable("payment-group-reference") String paymentGroupReference) {
        PaymentFeeLink paymentFeeLink = paymentGroupService.findByPaymentGroupReference(paymentGroupReference);

        return new ResponseEntity<>(paymentGroupDtoMapper.toPaymentGroupDto(paymentFeeLink), HttpStatus.OK);
    }

    @ApiOperation(value = "Add Payment Group with Fees", notes = "Add Payment Group with Fees")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Payment group with fee(s) created"),
        @ApiResponse(code = 400, message = "Payment group creation failed")
    })
    @PostMapping(value = "/payment-groups")
    public ResponseEntity<PaymentGroupDto> addNewFee(@Valid @RequestBody PaymentGroupDto paymentGroupDto) {

        String paymentGroupReference = PaymentReference.getInstance().getNext();

        paymentGroupDto.getFees().stream().forEach(f -> {
            if (f.getCcdCaseNumber() == null && f.getReference() == null){
                throw new InvalidFeeRequestException("Either ccdCaseNumber or caseReference is required.");
            }
        });

        List<PaymentFee> feeList = paymentGroupDto.getFees().stream()
            .map(paymentGroupDtoMapper::toPaymentFee).collect(Collectors.toList());

        PaymentFeeLink feeLink = PaymentFeeLink.paymentFeeLinkWith()
            .paymentReference(paymentGroupReference)
            .fees(Lists.newArrayList(feeList))
            .build();
        feeList.stream().forEach(fee -> fee.setPaymentLink(feeLink));

        PaymentFeeLink paymentFeeLink = paymentGroupService.addNewFeeWithPaymentGroup(feeLink);

        return new ResponseEntity<>(paymentGroupDtoMapper.toPaymentGroupDto(paymentFeeLink), HttpStatus.CREATED);
    }


    @ApiOperation(value = "Add new Fee(s) to existing Payment Group", notes = "Add new Fee(s) to existing Payment Group")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Fee(s) added to Payment Group"),
        @ApiResponse(code = 400, message = "Payment group creation failed"),
        @ApiResponse(code = 404, message = "Payment Group not found")
    })
    @PutMapping(value = "/payment-groups/{payment-group-reference}")
    public ResponseEntity<PaymentGroupDto> addNewFeetoPaymentGroup(@PathVariable("payment-group-reference") String paymentGroupReference,
                                                                   @Valid @RequestBody PaymentGroupDto paymentGroupDto) {

        paymentGroupDto.getFees().stream().forEach(f -> {
            if (f.getCcdCaseNumber() == null && f.getReference() == null){
                throw new InvalidFeeRequestException("Either ccdCaseNumber or caseReference is required.");
            }
        });

        PaymentFeeLink paymentFeeLink = paymentGroupService.
            addNewFeetoExistingPaymentGroup(paymentGroupDto.getFees().stream()
                .map(paymentGroupDtoMapper::toPaymentFee).collect(Collectors.toList()), paymentGroupReference);

        return new ResponseEntity<>(paymentGroupDtoMapper.toPaymentGroupDto(paymentFeeLink), HttpStatus.OK);
    }

    @ApiOperation(value = "Create card payment in Payment Group", notes = "Create card payment in Payment Group")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Payment created"),
        @ApiResponse(code = 400, message = "Payment creation failed"),
        @ApiResponse(code = 422, message = "Invalid or missing attribute")
    })
    @PostMapping(value = "/payment-groups/{payment-group-reference}/card-payments")
    @ResponseBody
    public ResponseEntity<PaymentDto> createCardPayment(
        @RequestHeader(value = "return-url") String returnURL,
        @RequestHeader(value = "service-callback-url", required = false) String serviceCallbackUrl,
        @PathVariable("payment-group-reference") String paymentGroupReference,
        @Valid @RequestBody CardPaymentRequest request) throws CheckDigitException, MethodNotSupportedException {

        if (StringUtils.isEmpty(request.getChannel()) || StringUtils.isEmpty(request.getProvider())) {
            request.setChannel("online");
            request.setProvider("gov pay");
        }

        PaymentServiceRequest paymentServiceRequest = PaymentServiceRequest.paymentServiceRequestWith()
            .description(request.getDescription())
            .paymentGroupReference(paymentGroupReference)
            .paymentReference(referenceUtil.getNext("RC"))
            .returnUrl(returnURL)
            .ccdCaseNumber(request.getCcdCaseNumber())
            .caseReference(request.getCaseReference())
            .currency(request.getCurrency().getCode())
            .siteId(request.getSiteId())
            .serviceType(request.getService().getName())
            .amount(request.getAmount())
            .serviceCallbackUrl(serviceCallbackUrl)
            .channel(request.getChannel())
            .provider(request.getProvider())
            .build();

        PaymentFeeLink paymentLink = delegatingPaymentService.update(paymentServiceRequest);
        Payment payment = getPayment(paymentLink, paymentServiceRequest.getPaymentReference());
        PaymentDto paymentDto = paymentDtoMapper.toCardPaymentDto(payment, paymentGroupReference);

        if (request.getChannel().equals("telephony") && request.getProvider().equals("pci pal")) {
            PciPalPaymentRequest pciPalPaymentRequest = PciPalPaymentRequest.pciPalPaymentRequestWith().orderAmount(request.getAmount().toString()).orderCurrency(request.getCurrency().getCode())
                .orderReference(paymentDto.getReference()).build();
            pciPalPaymentRequest.setCustomData2(payment.getCcdCaseNumber());
            String link = pciPalPaymentService.getPciPalLink(pciPalPaymentRequest, request.getService().name());
            paymentDto = paymentDtoMapper.toPciPalCardPaymentDto(paymentLink, payment, link);
        }

        return new ResponseEntity<>(paymentDto, HttpStatus.CREATED);
    }

    private Payment getPayment(PaymentFeeLink paymentFeeLink, String paymentReference){
        return paymentFeeLink.getPayments().stream().filter(p -> p.getReference().equals(paymentReference)).findAny()
            .orElseThrow(() -> new PaymentNotFoundException("Payment with reference " + paymentReference + " does not exists."));
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(InvalidPaymentGroupReferenceException.class)
    public String return403(InvalidPaymentGroupReferenceException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public String return400(ConstraintViolationException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(PaymentNotFoundException.class)
    public String return400(PaymentNotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidFeeRequestException.class)
    public String return400(InvalidFeeRequestException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public String return400(MethodArgumentNotValidException ex) {
        return ex.getMessage();
    }
}
