package uk.gov.hmcts.payment.api.dto.order;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.*;
import uk.gov.hmcts.payment.api.contract.util.CurrencyCode;
import uk.gov.hmcts.payment.api.contract.util.Service;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Objects;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(NON_NULL)
@Builder(builderMethodName = "paymentDtoWith")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class OrderPaymentDto {

    @NotNull
    @DecimalMin("0.01")
    @Positive
    @Digits(integer = 10, fraction = 2, message = "Payment amount cannot have more than 2 decimal places")
    private BigDecimal amount;

    @NotEmpty
    private String description;

    private String ccdCaseNumber;

    private String caseReference;

    @NotNull
    private Service service;

    @NotNull
    private CurrencyCode currency;

    @NotEmpty
    private String customerReference;

    @NotEmpty
    private String organisationName;

    @NotEmpty
    private String accountNumber;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderPaymentDto that = (OrderPaymentDto) o;
        return Objects.equals(amount, that.amount) &&
            Objects.equals(description, that.description) &&
            Objects.equals(ccdCaseNumber, that.ccdCaseNumber) &&
            Objects.equals(caseReference, that.caseReference) &&
            service == that.service &&
            currency == that.currency &&
            Objects.equals(customerReference, that.customerReference) &&
            Objects.equals(organisationName, that.organisationName) &&
            Objects.equals(accountNumber, that.accountNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount.toString(), currency.getCode(), customerReference, accountNumber);
    }

    public int hashCodeWithOrderReference(String orderReference) {
        return Objects.hash(orderReference.trim(), amount.abs().toString(), currency.getCode(), customerReference.trim(), accountNumber.trim());
    }

}
