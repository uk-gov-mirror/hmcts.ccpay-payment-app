package uk.gov.hmcts.payment.api.model;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface PaymentFeeRelRepository extends CrudRepository<PaymentFeeApportion, Integer>, JpaSpecificationExecutor<PaymentFeeApportion> {

    <S extends PaymentFeeApportion> S save(S entity);

    Optional<PaymentFeeApportion> findByPaymentId(String id);

    Optional<PaymentFeeApportion> findByFeeId(String id);

}
