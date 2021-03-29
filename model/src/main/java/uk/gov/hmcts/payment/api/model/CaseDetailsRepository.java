package uk.gov.hmcts.payment.api.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

public interface CaseDetailsRepository extends CrudRepository<CaseDetails,Integer> {
}
