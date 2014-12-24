package com.spartango.loc;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Author: spartango
 * Date: 12/5/14
 * Time: 19:12.
 */
public class StateUtils {
    private static final Map<String, String> states = new HashMap<String, String>() {
        {
            put("AL", "Alabama");
            put("AK", "Alaska");
            put("AS", "American Samoa");
            put("AZ", "Arizona");
            put("AR", "Arkansas");
            put("CA", "California");
            put("CO", "Colorado");
            put("CT", "Connecticut");
            put("DE", "Delaware");
            put("DC", "District Of Columbia");
            put("FM", "Federated States Of Micronesia");
            put("FL", "Florida");
            put("GA", "Georgia");
            put("GU", "Guam");
            put("HI", "Hawaii");
            put("ID", "Idaho");
            put("IL", "Illinois");
            put("IN", "Indiana");
            put("IA", "Iowa");
            put("KS", "Kansas");
            put("KY", "Kentucky");
            put("LA", "Louisiana");
            put("ME", "Maine");
            put("MH", "Marshall Islands");
            put("MD", "Maryland");
            put("MA", "Massachusetts");
            put("MI", "Michigan");
            put("MN", "Minnesota");
            put("MS", "Mississippi");
            put("MO", "Missouri");
            put("MT", "Montana");
            put("NE", "Nebraska");
            put("NV", "Nevada");
            put("NH", "New Hampshire");
            put("NJ", "New Jersey");
            put("NM", "New Mexico");
            put("NY", "New York");
            put("NC", "North Carolina");
            put("ND", "North Dakota");
            put("MP", "Northern Mariana Islands");
            put("OH", "Ohio");
            put("OK", "Oklahoma");
            put("OR", "Oregon");
            put("PW", "Palau");
            put("PA", "Pennsylvania");
            put("PR", "Puerto Rico");
            put("RI", "Rhode Island");
            put("SC", "South Carolina");
            put("SD", "South Dakota");
            put("TN", "Tennessee");
            put("TX", "Texas");
            put("UT", "Utah");
            put("VT", "Vermont");
            put("VI", "Virgin Islands");
            put("VA", "Virginia");
            put("WA", "Washington");
            put("WV", "West Virginia");
            put("WI", "Wisconsin");
            put("WY", "Wyoming");
        }
    };

    private static final BiMap<String, String> stateCodes = HashBiMap.create(states);

    public static String stateForCode(String code) {
        return stateCodes.get(code);
    }

    public static String codeForState(String state) {
        return stateCodes.inverse().get(state);
    }

    public static Set<String> stateCodes() {
        return stateCodes.keySet();
    }

    public static Set<String> states() {
        return stateCodes.values();
    }
}
