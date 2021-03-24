package uk.gov.hmcts.payment.api.service;

import org.springframework.util.MultiValueMap;
import uk.gov.hmcts.payment.api.dto.OrganisationalServiceDto;

import java.util.List;

public interface ReferenceDataService<T> {
    List<T> getSiteIDs();

    OrganisationalServiceDto getOrgDetail(String caseType, MultiValueMap<String, String> headers);
}
