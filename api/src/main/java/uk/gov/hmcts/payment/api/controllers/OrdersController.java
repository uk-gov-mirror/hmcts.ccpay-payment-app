package uk.gov.hmcts.payment.api.controllers;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import io.swagger.annotations.SwaggerDefinition;
import io.swagger.annotations.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.payment.api.contract.CreateOrderRequest;
import uk.gov.hmcts.payment.api.contract.CreateOrderResponseDto;
import uk.gov.hmcts.payment.api.contract.FeeDto;
import uk.gov.hmcts.payment.api.dto.OrganisationalServiceDto;
import uk.gov.hmcts.payment.api.model.CaseDetails;
import uk.gov.hmcts.payment.api.model.PaymentFee;
import uk.gov.hmcts.payment.api.model.PaymentFeeLink;
import uk.gov.hmcts.payment.api.service.OrderCasesService;
import uk.gov.hmcts.payment.api.service.ReferenceDataService;

import javax.validation.Valid;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@Api(tags = {"Orders"})
@SwaggerDefinition(tags = {@Tag(name = "OrdersController", description = "Create/Manage orders REST API")})
public class OrdersController {

    private static final Logger LOG = LoggerFactory.getLogger(OrdersController.class);

    @Autowired
    private ReferenceDataService referenceDataService;

    @Autowired
    private OrderCasesService orderCasesService;

    @PostMapping(path = "/order")
    @ApiOperation(value = "Create credit account payment", notes = "Create credit account payment")
    @ApiResponses(value = {
        @ApiResponse(code = 201, message = "Order created"),
        @ApiResponse(code = 400, message = "Bad Request"),
        @ApiResponse(code = 403, message = "Forbidden"),
        @ApiResponse(code = 404, message = "No Service found for given CaseType"),
        @ApiResponse(code = 422, message = "Invalid or missing attribute"),
        @ApiResponse(code = 504, message = "Unable to retrieve service information. Please try again later"),
        @ApiResponse(code = 500, message = "Internal server error. Please try again later")
    })
    @Transactional
    public CreateOrderResponseDto createOrder(@Valid @RequestBody CreateOrderRequest request, @RequestHeader(required = false) MultiValueMap<String, String> headers) {
        OrganisationalServiceDto organisationalServiceDto = referenceDataService.getOrgDetail(request.getCaseType(), headers);
        PaymentFeeLink pf = PaymentFeeLink.paymentFeeLinkWith()
            .orgId(organisationalServiceDto.getServiceCode())
            .enterpriseServiceName(organisationalServiceDto.getServiceDescription())
            .fees(request.getFees().stream().map(this::toFee).collect(Collectors.toList()))
            .build();

        CaseDetails cd = CaseDetails.caseDetailsWith()
            .orders(new HashSet<>())
            .caseReference(request.getCaseReference())
            .ccdCaseNumber(request.getCcdCaseNumber()).build();
        cd.getOrders().add(pf);
        return new CreateOrderResponseDto(orderCasesService.createOrder(cd, pf));
    }

    public PaymentFee toFee(FeeDto feeDto) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return PaymentFee.feeWith()
            .calculatedAmount(feeDto.getCalculatedAmount())
            .code(feeDto.getCode())
            .version(feeDto.getVersion())
            .volume(feeDto.getVolume() == null ? 1 : feeDto.getVolume().intValue())
            .dateCreated(timestamp)
            .build();
    }
}
