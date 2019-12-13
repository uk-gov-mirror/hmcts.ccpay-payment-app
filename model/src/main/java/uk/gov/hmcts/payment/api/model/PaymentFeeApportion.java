package uk.gov.hmcts.payment.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Data
@Builder(builderMethodName = "paymentFeeApportionWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "payment_fee_apportion")
public class PaymentFeeApportion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_id")
    private Payment payment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_id")
    private PaymentFee fee;

    @Column(name = "payment_reference")
    private String paymentReference;

    @Column(name = "fee_reference")
    private String feeReference;

    @Column(name = "apportion_amount")
    private BigDecimal apportionAmount;

    @Column(name = "fee_amount")
    private BigDecimal feeAmount;

    @Column(name = "payment_amount")
    private BigDecimal paymentAmount;

    @Column(name = "ccd_case_number")
    private String ccdCaseNumber;

    @CreationTimestamp
    @Column(name = "date_created", nullable = false)
    private Date dateCreated;
}
