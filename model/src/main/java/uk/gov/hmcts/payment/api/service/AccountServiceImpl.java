package uk.gov.hmcts.payment.api.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.stereotype.Service;
import uk.gov.hmcts.payment.api.dto.AccountDto;
import uk.gov.hmcts.payment.api.dto.AccountPaymentDto;

@Service
public class AccountServiceImpl implements AccountService<AccountDto, String, AccountPaymentDto> {

    @Autowired
    private OAuth2RestOperations restTemplate;

    @Value("${liberata.api.account.url}")
    private String accountBaseUrl;

    @Value("${liberata.api.payment.url}")
    private String paymentBaseUrl;

    @Override
    public AccountDto retrieve(String pbaCode) {
        return restTemplate
            .getForObject(accountBaseUrl + "/" + pbaCode, AccountDto.class);
    }

    @Override
    public AccountDto post(AccountPaymentDto request) {
        return restTemplate
            .postForObject(paymentBaseUrl + "/", request, AccountDto.class);
    }

}
