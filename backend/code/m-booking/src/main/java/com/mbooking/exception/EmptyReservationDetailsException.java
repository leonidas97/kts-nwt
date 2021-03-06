package com.mbooking.exception;

import org.springframework.http.HttpStatus;

public class EmptyReservationDetailsException extends ApiException {

	private static final long serialVersionUID = 1L;
	
	public EmptyReservationDetailsException() {
		super("Nothing reservable", HttpStatus.BAD_REQUEST);
	}

}
