package uk.gov.hmcts.payment.api.contract;


import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.UniqueElements;

import javax.persistence.UniqueConstraint;
import javax.validation.Valid;
import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder(builderMethodName = "createOrderRequestDtoWith")
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class CreateOrderRequest {

    @NotBlank
    private String ccdCaseNumber;

    @NotBlank
    private String caseReference;

    @NotBlank
    private String caseType;

    @NotEmpty(message = "Fees must be passed")
    @Valid
    @UniqueElements
    private List<FeeDto> fees;

    @AssertFalse(message = "Fee code must be unique")
    private boolean isFeeCodeUnique(){
        Set<String> unique = new HashSet<>();
        return fees.stream()
            .anyMatch(p -> !unique.add(p.getCode()));
    }

}
