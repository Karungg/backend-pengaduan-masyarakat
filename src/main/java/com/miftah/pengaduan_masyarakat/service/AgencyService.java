package com.miftah.pengaduan_masyarakat.service;

import java.util.List;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.dto.AgencyRequest;
import com.miftah.pengaduan_masyarakat.dto.AgencyResponse;

public interface AgencyService {

    AgencyResponse createAgency(AgencyRequest request);

    AgencyResponse getAgencyById(UUID id);

    List<AgencyResponse> getAllAgencies();

    AgencyResponse updateAgencies(UUID id, AgencyRequest request);

    void deleteAgency(UUID id);
}
