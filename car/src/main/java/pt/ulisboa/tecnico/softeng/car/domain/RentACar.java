package pt.ulisboa.tecnico.softeng.car.domain;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.joda.time.LocalDate;

import pt.ist.fenixframework.FenixFramework;
import pt.ulisboa.tecnico.softeng.car.dataobjects.RentingData;
import pt.ulisboa.tecnico.softeng.car.exception.CarException;

public class RentACar extends RentACar_Base {
	private static int counter;

	public int getNextCounter() {
		return ++counter;
	}

	public RentACar(String name, String nif, String iban) {
		checkArguments(name, nif, iban);
		super.setName(name);
		super.setNIF(nif);
		super.setIBAN(iban);
		super.setCode(nif + Integer.toString(getNextCounter()));
		setProcessor(new Processor());

		FenixFramework.getDomainRoot().addRentACar(this);
	}

	private void checkArguments(String name, String nif, String iban) {
		if (name == null || name.isEmpty() || nif == null || nif.isEmpty() || iban == null || iban.isEmpty()) {

			throw new CarException();
		}

		for (final RentACar rental : FenixFramework.getDomainRoot().getRentACarSet()) {
			if (rental.getNIF().equals(nif)) {
				throw new CarException();
			}
		}
	}

	public void delete() {
		setRoot(null);
		
		for (Vehicle vehicle : getVehicleSet()) {
			vehicle.delete();
		}

		getProcessor().delete();

		deleteDomainObject();
	}

	public boolean hasVehicle(String plate) {
		for (Vehicle vehicle : this.getVehicleSet()) {
			if (vehicle.getPlate().equals(plate))
				return true;
		}
		return false;
	}

	public Set<Vehicle> getAvailableVehicles(Class<?> cls, LocalDate begin, LocalDate end) {
		final Set<Vehicle> availableVehicles = new HashSet<>();
		for (final Vehicle vehicle : this.getVehicleSet()) {
			if (cls == vehicle.getClass() && vehicle.isFree(begin, end)) {
				availableVehicles.add(vehicle);
			}
		}
		return availableVehicles;
	}

	private static Set<Vehicle> getAllAvailableVehicles(Class<?> cls, LocalDate begin, LocalDate end) {
		final Set<Vehicle> vehicles = new HashSet<>();
		for (final RentACar rentACar : FenixFramework.getDomainRoot().getRentACarSet()) {
			vehicles.addAll(rentACar.getAvailableVehicles(cls, begin, end));
		}
		return vehicles;
	}

	public static Set<Vehicle> getAllAvailableMotorcycles(LocalDate begin, LocalDate end) {
		return getAllAvailableVehicles(Motorcycle.class, begin, end);
	}

	public static Set<Vehicle> getAllAvailableCars(LocalDate begin, LocalDate end) {
		return getAllAvailableVehicles(Car.class, begin, end);
	}

	public static String rent(Class<? extends Vehicle> vehicleType, String drivingLicense, String buyerNIF,
			String buyerIBAN, LocalDate begin, LocalDate end) {
		Set<Vehicle> availableVehicles;

		if (vehicleType == Car.class) {
			availableVehicles = getAllAvailableCars(begin, end);
		} else {
			availableVehicles = getAllAvailableMotorcycles(begin, end);
		}

		return availableVehicles.stream().findFirst().map(v -> v.rent(drivingLicense, begin, end, buyerNIF, buyerIBAN))
				.orElseThrow(CarException::new).getReference();
	}

	public static String cancelRenting(String reference) {
		final Renting renting = getRenting(reference);

		if (renting != null) {
			return renting.cancel();
		}

		throw new CarException();
	}

	/**
	 * Lookup for a renting using its reference.
	 *
	 * @param reference
	 * @return the renting with the given reference.
	 */
	protected static Renting getRenting(String reference) {
		for (final RentACar rentACar : FenixFramework.getDomainRoot().getRentACarSet()) {
			for (final Vehicle vehicle : rentACar.getVehicleSet()) {
				final Renting renting = vehicle.getRenting(reference);
				if (renting != null) {
					return renting;
				}
			}
		}
		return null;
	}

	public static RentingData getRentingData(String reference) {
		final Renting renting = getRenting(reference);
		if (renting == null) {
			throw new CarException();
		}
		return new RentingData(renting);
	}

	@Override
	public void setName(String name) {

	}


	@Override
	public void setCode(String code) {

	}
	
	@Override
	public void setNIF(String nif) {

	}
	
	@Override
	public void setIBAN(String iban) {

	}	

}
