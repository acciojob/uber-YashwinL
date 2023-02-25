package com.driver.services.impl;

import com.driver.model.*;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public void register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
	}

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function
		Customer customer= customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll(Sort.by(Sort.Direction.ASC,"driverId"));
		Customer customer = customerRepository2.findById(customerId).get();
		TripBooking tripBooking = new TripBooking();


		if(!driverList.isEmpty()){
			for(Driver i : driverList){
				if(i.getCab().getAvailable()){
					tripBooking.setCustomer(customer);
					tripBooking.setDriver(i);
					tripBooking.setStatus(TripStatus.CONFIRMED);
					tripBooking.setFromLocation(fromLocation);
					tripBooking.setToLocation(toLocation);
					tripBooking.setDistanceInKm(distanceInKm);
					tripBooking.setBill(i.getCab().getPerKmRate()*distanceInKm);
					customer.getTripBookingList().add(tripBooking);
					i.getTripBookingList().add(tripBooking);
					tripBookingRepository2.save(tripBooking);
					customerRepository2.save(customer);
					driverRepository2.save(i);
					break;
				}
			}
		}else{
			throw  new Exception("No value present");
		}
		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		Customer customer = tripBooking.getCustomer();
		Driver driver = tripBooking.getDriver();
		customer.getTripBookingList().add(tripBooking);
		driver.getTripBookingList().add(tripBooking);
//		List<TripBooking> tripBookingList = customer.getTripBookingList();
//		for(TripBooking i : tripBookingList){
//			if(i.getTripBookingId()==tripId){
//				i.setStatus(TripStatus.CANCELED);
//				break;
//			}
//		}
//		List<TripBooking> tripBookingList1 = driver.getTripBookingList();
//		for(TripBooking i : tripBookingList1){
//			if(i.getTripBookingId()==tripId){
//				i.setStatus(TripStatus.CANCELED);
//				break;
//			}
//		}
		tripBookingRepository2.save(tripBooking);
		customerRepository2.save(customer);
		driverRepository2.save(driver);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		Customer customer = tripBooking.getCustomer();
		Driver driver = tripBooking.getDriver();
		customer.getTripBookingList().add(tripBooking);
		driver.getTripBookingList().add(tripBooking);
//		for(TripBooking i : tripBookingList){
//			if(i.getTripBookingId()==tripId){
//				i.setStatus(TripStatus.CONFIRMED);
//				break;
//			}
//		}
//		List<TripBooking> tripBookingList1 = driver.getTripBookingList();
//		for(TripBooking i : tripBookingList1){
//			if(i.getTripBookingId()==tripId){
//				i.setStatus(TripStatus.CONFIRMED);
//				break;
//			}
//		}
		tripBookingRepository2.save(tripBooking);
		customerRepository2.save(customer);
		driverRepository2.save(driver);
	}
}
