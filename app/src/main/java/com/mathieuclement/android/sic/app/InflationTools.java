package com.mathieuclement.android.sic.app;

import android.content.Context;
import android.util.Log;

import java.io.*;
import java.util.Arrays;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.concurrent.Semaphore;

// should be thread safe, but that is not guaranteed
class InflationTools {
    private InflationTools() {
        throw new UnsupportedOperationException("Not instantiable class");
    }

    private static final int FIRST_KNOWN_YEAR = 1914;
    private static final int LAST_KNOWN_YEAR = 2013;
    private static final int NB_YEARS = LAST_KNOWN_YEAR - FIRST_KNOWN_YEAR + 1;

    private static final Integer[] YEARS_BOXED_ARR = new Integer[NB_YEARS];
    static {
        for (int year = FIRST_KNOWN_YEAR, index = 0; year <= LAST_KNOWN_YEAR; year++, index++)
            YEARS_BOXED_ARR[index] = year;
    }

    public static Integer[] getYearsBoxedArray() {
        return YEARS_BOXED_ARR.clone();
    }

    public static int arrayIndex(int year) {
        assert year >= FIRST_KNOWN_YEAR && year <= LAST_KNOWN_YEAR;
        return year - FIRST_KNOWN_YEAR;
    }

    public static int getFirstKnownYear() {
        return FIRST_KNOWN_YEAR;
    }

    public static int getLastKnownYear() {
        return LAST_KNOWN_YEAR;
    }

    public static int nbYears() {
        return NB_YEARS;
    }

    private final static float[] rates = new float[LAST_KNOWN_YEAR - FIRST_KNOWN_YEAR + 1];
    private static boolean ratesIsInitialized = false;

    private static final Semaphore RATES_SEMA = new Semaphore(1);

    private static final String TAG = "INFLATION_TOOLS";

    public static float inflationRate(Context context, int year) throws IOException {
        try {
            RATES_SEMA.acquire();
            if(!ratesIsInitialized) {
                initializeRates(context);
            }
        } catch (InterruptedException e) {
            Log.e(TAG, "semaphore interruption", e);
        } finally {
            RATES_SEMA.release();
        }
        return rates[arrayIndex(year)];
    }

    private static void initializeRates(Context context) throws IOException {
        InputStream inputStream = context.getResources().openRawResource(R.raw.annualmeans);
        InputStreamReader unbufferedReader =  new InputStreamReader(inputStream);
        BufferedReader reader = new BufferedReader(unbufferedReader, 8192);

        Scanner scanner = new Scanner(reader);
        scanner.useDelimiter(";|\\n|\\r\\n");
        int firstYearRead = scanner.nextInt();
        assert firstYearRead == FIRST_KNOWN_YEAR;
        rates[0] = scanner.nextFloat();
        for (int i = 1; i < nbYears(); i++) {
            scanner.nextInt(); // skip
            rates[i] = scanner.nextFloat();
        }
    }

    public static float newPrice(Context context, float startValue, int startYear, int endYear) throws IOException {
        return startValue / inflationRate(context, startYear) * inflationRate(context, endYear);
    }
}