package com.mbooking.service;

import com.mbooking.dto.CancelReservationStatusDTO;
import com.mbooking.dto.MakeReservationResponseDTO;
import com.mbooking.dto.ReservationDTO;
import com.mbooking.dto.ReservationDetailsDTO;
import com.mbooking.dto.ReservationDetailsRequestDTO;
import com.mbooking.dto.ViewReservationDTO;

import java.util.List;

public interface ReservationService {
	List<ViewReservationDTO> findAllReservations();
	List<ViewReservationDTO> findAllByUserEmail(String email);
	CancelReservationStatusDTO cancelReservation(Long id);
	MakeReservationResponseDTO makeReservation(ReservationDTO dto);
	double getExpectedTotalPriceForManifestation(Long id);	//Gets price for reservations that have status CONFIRMED and CREATED
	double getCurrentTotalPriceForManifestation(Long id);	//Gets price for reservations that have status CONFIRMED
	double getExpectedTotalPriceForManifestationDay(Long id);
	double getCurrentTotalPriceForManifestationDay(Long id);
	List<ReservationDetailsDTO> getTotalCustomerReservationDetailsByManifestationAndManifestationDay(ReservationDetailsRequestDTO rdr);
	List<ReservationDetailsDTO> getAllReservationsDetailsByManifestationAndManifestationDay(ReservationDetailsRequestDTO rdr);
}
