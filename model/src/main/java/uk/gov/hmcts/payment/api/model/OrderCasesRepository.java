package uk.gov.hmcts.payment.api.model;

import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;

public interface OrderCasesRepository extends CrudRepository<OrderCases, Integer>, JpaSpecificationExecutor<OrderCases> {
}
