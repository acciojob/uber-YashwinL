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
		List<TripBooking> tripBookingList = customer.getTripBookingList();
		for(TripBooking tripBooking: tripBookingList){
			if(tripBooking.getStatus()==TripStatus.CONFIRMED){
				tripBooking.setStatus(TripStatus.CANCELED);
			}
		}
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
//		List<Driver> driverList = driverRepository2.findAll(Sort.by(Sort.Direction.ASC,"driverId"));
//		Customer customer = customerRepository2.findById(customerId).get();
//		TripBooking tripBooking = new TripBooking();


//		if(!driverList.isEmpty()){
//			for(Driver i : driverList){
//				if(i.getCab().getAvailable()){
//					tripBooking.setCustomer(customer);
//					tripBooking.setDriver(i);
//					tripBooking.setStatus(TripStatus.CONFIRMED);
//					tripBooking.setFromLocation(fromLocation);
//					tripBooking.setToLocation(toLocation);
//					tripBooking.setDistanceInKm(distanceInKm);
//					tripBooking.setBill(i.getCab().getPerKmRate()*distanceInKm);
//					customer.getTripBookingList().add(tripBooking);
//					i.getTripBookingList().add(tripBooking);
//					tripBookingRepository2.save(tripBooking);
//					customerRepository2.save(customer);
//					driverRepository2.save(i);
//					break;
//				}
//			}
//		}else{
//			throw  new Exception("No value present");
//		}
//		return tripBooking;
		TripBooking tripBooking = new TripBooking();
		Driver driver = null;
		List<Driver> driverList = driverRepository2.findAll();

		for(Driver driver1 : driverList){
			if(driver1.getCab().getAvailable()){
				if((driver == null) || (driver.getDriverId() > driver1.getDriverId())){
					driver = driver1;
				}
			}
		}

		if(driver == null){
			throw new Exception("No cab available!");
		}

		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setFromLocation(fromLocation);
		tripBooking.setToLocation(toLocation);
		driver.getCab().setAvailable(false);
		tripBooking.setDistanceInKm(distanceInKm);

		int rate = driver.getCab().getPerKmRate();
		tripBooking.setBill(distanceInKm * rate);

		customer.getTripBookingList().add(tripBooking);
		customerRepository2.save(customer);

		driver.getTripBookingList().add(tripBooking);
		driverRepository2.save(driver);

		return tripBooking;

	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBooking.setBill(0);
		tripBooking.getDriver().getCab().setAvailable(true);
		Customer customer = tripBooking.getCustomer();
		Driver driver = tripBooking.getDriver();
//		customer.getTripBookingList().add(tripBooking);
//		driver.getTripBookingList().add(tripBooking);
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
//		customerRepository2.save(customer);
//		driverRepository2.save(driver);

	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.setStatus(TripStatus.COMPLETED);
		int distance = tripBooking.getDistanceInKm();

		Customer customer = tripBooking.getCustomer();
		Driver driver = tripBooking.getDriver();
		Cab cab = driver.getCab();
		int rate = cab.getPerKmRate();
		tripBooking.setBill(distance*rate);
		cab.setAvailable(true);
//		customer.getTripBookingList().add(tripBooking);
//		driver.getTripBookingList().add(tripBooking);


		tripBookingRepository2.save(tripBooking);

	}
}
