package com.moziy.hollerback.comparator;

import java.text.Collator;
import java.util.Comparator;

import com.moziy.hollerback.model.Country;

public class CountryComparator implements Comparator<Country> {
	private Comparator comparator;

	public CountryComparator() {
		comparator = Collator.getInstance();
	}

	public int compare(Country o1, Country o2) {
		return comparator.compare(o1.name, o2.name);
	}
}