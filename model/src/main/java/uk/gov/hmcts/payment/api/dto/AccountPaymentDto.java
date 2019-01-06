package uk.gov.hmcts.payment.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Wither;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(NON_NULL)
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "createAccountPaymentRequestDtoWith")
@Wither
public class AccountPaymentDto {
    // TODO: clarify naming strategy
    // TODO: clarify if Fee object needs to be changed to a Fees array of Fees in the payload and inform Liberata of the change
    // TODO: clarify with Liberata that Fee property 'amount' needs to be changed to calculatedAmount
    // TODO: think about where FeeDto and LiberataFeeDto should sit (if this approach taken)
    PaymentByAccountRequest paymentByAccountRequest;
    List<Fee> fees;
}
