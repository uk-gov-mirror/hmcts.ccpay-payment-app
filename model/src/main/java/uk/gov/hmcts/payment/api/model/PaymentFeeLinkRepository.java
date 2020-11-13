package uk.gov.hmcts.payment.api.model;


import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.Optional;

public interface PaymentFeeLinkRepository extends PagingAndSortingRepository<PaymentFeeLink, Integer> , CrudRepository<PaymentFeeLink, Integer>, JpaSpecificationExecutor<PaymentFeeLink> {

    <S extends PaymentFeeLink> S save(S entity);

    Optional<PaymentFeeLink> findByPaymentReference(String id);

}
