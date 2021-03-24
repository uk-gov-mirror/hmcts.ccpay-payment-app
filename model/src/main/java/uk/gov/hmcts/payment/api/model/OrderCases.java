package uk.gov.hmcts.payment.api.model;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import uk.gov.hmcts.payment.api.jpaaudit.listner.Auditable;
import uk.gov.hmcts.payment.api.jpaaudit.listner.PaymentFeeEntityListener;

import javax.persistence.*;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder(builderMethodName = "ordercasesWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "order_cases")
public class OrderCases {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private PaymentFeeLink orders;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_details_id")
    private CaseDetails caseDetails;

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
