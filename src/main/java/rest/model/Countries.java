package rest.model;

import java.io.*;
import java.util.*;

/**
 * Set of iso3166 country codes
 */
public final class Countries {
    private final Set<String> countryCodes = new HashSet<>();

    public Countries() throws IOException {
        InputStream input = getClass().getResourceAsStream("/iso3166_codes");
        BufferedReader countriesReader = new BufferedReader(new InputStreamReader(input));
        String countryCode;
        while ((countryCode = countriesReader.readLine()) != null) {
            countryCodes.add(countryCode);
        }
    }

    public boolean containsCountry(String code) {
        return countryCodes.contains(code);
    }
}