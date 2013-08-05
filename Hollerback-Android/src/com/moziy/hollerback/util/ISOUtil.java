package com.moziy.hollerback.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.moziy.hollerback.comparator.CountryComparator;
import com.moziy.hollerback.debug.LogUtil;
import com.moziy.hollerback.model.Country;

public class ISOUtil {

	public static List<Country> getCountries(String[] regionCodes) {
		PhoneNumberUtil util = PhoneNumberUtil.getInstance();
		Set<String> set = util.getSupportedRegions();
		// Outputs "Zurich"
		for (String number : set) {

		}

		List<Country> countries = new ArrayList<Country>();

		Locale[] locales = Locale.getAvailableLocales();
		for (Locale locale : locales) {
			String iso = locale.getISO3Country();
			String code = locale.getCountry();
			String name = locale.getDisplayCountry();

			if (!"".equals(iso) && !"".equals(code) && !"".equals(name)
					&& set.contains(code)) {
				Country country = new Country(iso, code, name);
				if (!countries.contains(country)) {
					countries.add(new Country(iso, code, name));
				}
			}
		}

		Collections.sort(countries, new CountryComparator());
		for (Country country : countries) {
			LogUtil.i(country.toString());
		}

		return countries;
	}

}
