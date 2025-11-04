package com.miftah.pengaduan_masyarakat.service;

import java.util.List;
import java.util.UUID;

import com.miftah.pengaduan_masyarakat.dto.ComplaintRequest;
import com.miftah.pengaduan_masyarakat.dto.ComplaintResponse;

public interface ComplaintService {
    ComplaintResponse createComplaint(ComplaintRequest request);

    ComplaintResponse getComplaintById(UUID id);

    List<ComplaintResponse> getAllComplaints();

    ComplaintResponse updateComplaint(UUID id, ComplaintRequest request);

    void deleteComplaint(UUID id);
}
