package pt.ulisboa.tecnico.softeng.activity.domain;

import org.joda.time.LocalDate;
import java.util.Random;

import pt.ulisboa.tecnico.softeng.activity.exception.ActivityException;

public class ActivityOffer extends ActivityOffer_Base {

	public ActivityOffer(Activity activity, LocalDate begin, LocalDate end, double amount) {
		checkArguments(activity, begin, end, amount);

		setBegin(begin);
		setEnd(end);
		setCode(activity.getCode() + getRandom());
		setCapacity(activity.getCapacity());
		setAmount(amount);
		setActivity(activity);
	}

	private int getRandom() {
		Random r = new Random();
		return r.nextInt();
	}

	public void delete() {
		setActivity(null);

		for (Booking booking : getBookingSet()) {
			booking.delete();
		}

		deleteDomainObject();
	}

	private void checkArguments(Activity activity, LocalDate begin, LocalDate end, double amount) {
		if (activity == null || begin == null || end == null) {
			throw new ActivityException();
		}

		if (end.isBefore(begin)) {
			throw new ActivityException();
		}

		if (amount < 1) {
			throw new ActivityException();
		}
	}

	int getNumberActiveOfBookings() {
		int count = 0;
		for (Booking booking : getBookingSet()) {
			if (!booking.isCancelled()) {
				count++;
			}
		}
		return count;
	}

	public double getPrice() {
		return getAmount();
	}

	boolean available(LocalDate begin, LocalDate end) {
		return hasVacancy() && matchDate(begin, end);
	}

	boolean matchDate(LocalDate begin, LocalDate end) {
		if (begin == null || end == null) {
			throw new ActivityException();
		}

		return begin.equals(getBegin()) && end.equals(getEnd());
	}

	boolean hasVacancy() {
		return getCapacity() > getNumberActiveOfBookings();
	}

	public Booking getBooking(String reference) {
		for (Booking booking : getBookingSet()) {
			if (booking.getReference().equals(reference)
					|| booking.isCancelled() && booking.getCancel().equals(reference)) {
				return booking;
			}
		}
		return null;
	}

}
