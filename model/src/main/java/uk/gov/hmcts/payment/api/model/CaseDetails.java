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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder(builderMethodName = "caseDetailsWith")
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "case_details")
@EqualsAndHashCode
public class CaseDetails{

    @ToString.Exclude
//    @ManyToMany(cascade = CascadeType.ALL)
//    @JoinTable(
//        name = "order_cases",
//        joinColumns = {
//            @JoinColumn(name = "order_id", referencedColumnName = "id",
//                nullable = false, updatable = false)},
//        inverseJoinColumns = {
//            @JoinColumn(name = "case_details_id", referencedColumnName = "id",
//                nullable = false, updatable = false)}
//    )
    @OneToMany(mappedBy = "caseDetails", cascade = CascadeType.ALL)
    Set<PaymentFeeLink> orders = new HashSet<>();

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ToString.Exclude
    @Column(name = "ccd_case_number")
    private String ccdCaseNumber;

    @ToString.Exclude
    @Column(name = "case_reference")
    private String caseReference;

    @CreationTimestamp
    @Column(name = "date_created")
    private Date dateCreated;

    @UpdateTimestamp
    @Column(name = "date_updated")
    private Date dateUpdated;

    @Override
    public int hashCode() {
        return super.hashCode();
    }
}
