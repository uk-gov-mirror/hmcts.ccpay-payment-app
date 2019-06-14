package uk.gov.hmcts.payment.api.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Entity
@Data
@Builder(builderMethodName = "paymentNoUpdateWith")
@AllArgsConstructor
@NoArgsConstructor
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "payment", indexes = {
    @Index(name = "ix_pay_ccd_case_number", columnList = "ccd_case_number"),
    @Index(name = "ix_pay_payment_status_provider", columnList = "payment_status, payment_provider")
})
public class PaymentNoUpdate {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id")
    private String userId;

    @Column(name = "external_reference")
    private String externalReference;

    @CreationTimestamp
    @Column(name = "date_created")
    private Date dateCreated;

    @Column(name = "date_updated")
    private Date dateUpdated;

    @Transient
    private String email;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "description")
    private String description;

    @Column(name = "site_id")
    private String siteId;

    @Column(name = "giro_slip_no")
    private String giroSlipNo;

    @Transient
    private String status;
    @Transient
    private Boolean finished;
    @Transient
    private String returnUrl;
    @Transient
    private String nextUrl;
    @Transient
    private String cancelUrl;
    @Transient
    private String refundsUrl;

    @Column(name = "currency")
    private String currency;

    @Column(name = "ccd_case_number")
    private String ccdCaseNumber;

    @Column(name = "case_reference")
    private String caseReference;

    @Column(name = "service_type")
    private String serviceType;

    @Column(name = "s2s_service_name")
    private String s2sServiceName;

    @ManyToOne
    @JoinColumn(name = "payment_channel")
    private PaymentChannel paymentChannel;

    @ManyToOne
    @JoinColumn(name = "payment_method")
    private PaymentMethod paymentMethod;

    @ManyToOne
    @JoinColumn(name = "payment_provider")
    private PaymentProvider paymentProvider;

    @ManyToOne
    @JoinColumn(name = "payment_status")
    private PaymentStatus paymentStatus;

    @Column(name = "organisation_name")
    private String organisationName;

    @Column(name = "pba_number")
    private String pbaNumber;

    @Column(name = "customer_reference")
    private String customerReference;

    @Column(name = "reference")
    private String reference;

    @Column(name = "reported_date_offline")
    private Date reportedDateOffline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_link_id", insertable = false, updatable = false)
    @ToString.Exclude
    private PaymentFeeLink paymentLink;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "payment_id", referencedColumnName = "id", nullable = false)
    @ToString.Exclude
    private List<StatusHistoryNoUpdate> statusHistories;

    @Column(name = "service_callback_url")
    private String serviceCallbackUrl;

    public static PaymentNoUpdate fromPayment(Payment payment) {
        return PaymentNoUpdate.paymentNoUpdateWith()
            .id(payment.getId())
            .userId(payment.getUserId())
            .externalReference(payment.getExternalReference())
            .dateCreated(payment.getDateCreated())
            .dateUpdated(payment.getDateUpdated())
            .email(payment.getEmail())
            .amount(payment.getAmount())
            .description(payment.getDescription())
            .siteId(payment.getSiteId())
            .giroSlipNo(payment.getGiroSlipNo())
            .status(payment.getStatus())
            .finished(payment.getFinished())
            .returnUrl(payment.getReturnUrl())
            .nextUrl(payment.getNextUrl())
            .cancelUrl(payment.getCancelUrl())
            .refundsUrl(payment.getRefundsUrl())
            .currency(payment.getCurrency())
            .ccdCaseNumber(payment.getCcdCaseNumber())
            .caseReference(payment.getCaseReference())
            .serviceType(payment.getServiceType())
            .s2sServiceName(payment.getS2sServiceName())
            .paymentChannel(payment.getPaymentChannel())
            .paymentMethod(payment.getPaymentMethod())
            .paymentProvider(payment.getPaymentProvider())
            .paymentStatus(payment.getPaymentStatus())
            .organisationName(payment.getOrganisationName())
            .pbaNumber(payment.getPbaNumber())
            .customerReference(payment.getCustomerReference())
            .reference(payment.getReference())
            .reportedDateOffline(payment.getReportedDateOffline())
            .paymentLink(payment.getPaymentLink())
            .statusHistories(payment.getStatusHistories() != null ? StatusHistoryNoUpdate.fromStatusHistoryUpdateList(payment.getStatusHistories()) : null)
            .serviceCallbackUrl(payment.getServiceCallbackUrl())
            .build();
    }
}
