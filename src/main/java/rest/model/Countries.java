package rest.model;

import java.io.*;
import java.util.*;

/**
 * Set of iso3166 country codes
 */
public final class Countries {
    private final Set<String> countries = new HashSet<>();

    public Countries() throws IOException {
        InputStream input = getClass().getResourceAsStream("/iso3166_codes");
        BufferedReader countriesReader = new BufferedReader(new InputStreamReader(input));
        String line;
        while ((line = countriesReader.readLine()) != null) {
            countries.add(line);
        }
    }

    public boolean containsCountry(String code) {
        return countries.contains(code);
    }
}