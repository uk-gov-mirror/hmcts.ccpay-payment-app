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
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpStatusCodeException;
import uk.gov.hmcts.payment.api.dto.AccountDto;
import uk.gov.hmcts.payment.api.dto.AccountPaymentDto;
import uk.gov.hmcts.payment.api.exception.AccountNotFoundException;
import uk.gov.hmcts.payment.api.exception.CaseInformationTooLargeException;
import uk.gov.hmcts.payment.api.exception.ExceededCreditLimitException;
import uk.gov.hmcts.payment.api.service.AccountService;

import javax.validation.Valid;

@RestController
@Api(tags = {"AccountController"})
@SwaggerDefinition(tags = {@Tag(name = "AccountController", description = "Account API")})
public class AccountController {

    private static final Logger LOG = LoggerFactory.getLogger(AccountController.class);

    @Autowired
    private AccountService<AccountDto, String, AccountPaymentDto> accountService;

    @ApiOperation(value = "Get the account status and available balance for a PBA account number from Liberata", notes = "Get the account status and available balance for a PBA account number from Liberata")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Account details retrieved"),
        @ApiResponse(code = 404, message = "Account not found")
    })
    @GetMapping(value = "/accounts/{accountNumber}")
    public AccountDto getAccounts(@PathVariable("accountNumber") String accountNumber) {
        try {
            return accountService.retrieve(accountNumber);
        } catch (HttpStatusCodeException ex) {
            LOG.error("Error while calling account: {}", ex);
            throw new AccountNotFoundException("Account not found");
        }
    }

    @ApiOperation(value = "Make payment by PBA Number at Liberata", notes = "Returns your updated Account")
    @ApiResponses(value = {
        @ApiResponse(code = 200, message = "Successful operation"),
        @ApiResponse(code = 400, message = "Exceeded Credit Limit"),
        @ApiResponse(code = 401, message = "Unauthenticated"),
        @ApiResponse(code = 404, message = "Account not found"),
        @ApiResponse(code = 413, message = "Case information too large"),
    })
    @PostMapping(value = "/accounts")
    public AccountDto postAccountPayment(@Valid @RequestBody AccountPaymentDto accountPaymentRequest) {
        try {
            return accountService.post(accountPaymentRequest);
        } catch (HttpStatusCodeException ex) {
            LOG.error("Error while making liberata payment: {}", ex);
            switch (ex.getStatusCode()) {
                case NOT_FOUND:
                    throw new AccountNotFoundException("Account not found");
                case BAD_REQUEST:
                    throw new ExceededCreditLimitException("Exceeded Credit Limit");
                case PAYLOAD_TOO_LARGE:
                    throw new CaseInformationTooLargeException("Case Information too large");
                default:
                    throw new AccountNotFoundException("Account not found");
            }
        }
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(AccountNotFoundException.class)
    public String return404(AccountNotFoundException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ExceededCreditLimitException.class)
    public String return400(ExceededCreditLimitException ex) {
        return ex.getMessage();
    }

    @ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
    @ExceptionHandler(CaseInformationTooLargeException.class)
    public String return413(CaseInformationTooLargeException ex) {
        return ex.getMessage();
    }
}
