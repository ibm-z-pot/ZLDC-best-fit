package com.ibmzpot.wcaz.bestfit;

import java.util.Random;

public class EmployeeNumber {
    private static final EmployeeNumber INSTANCE = new EmployeeNumber();

    private EmployeeNumber() {
        random = new Random();
        employeeNumbers = new String[] {
                "000010",
                "000020",
                "000030",
                "000050",
                "000060",
                "000070",
                "000090",
                "000100",
                "000110",
                "000120",
                "000130",
                "000140",
                "000150",
                "000160",
                "000170",
                "000180",
                "000190",
                "000200",
                "000210",
                "000220",
                "000230",
                "000240",
                "000250",
                "000260",
                "000270",
                "000280",
                "000290",
                "000300",
                "000310",
                "000320",
                "000330",
                "000340",
                "200010",
                "200120",
                "200140",
                "200170",
                "200220",
                "200240",
                "200280",
                "200310",
                "200330",
                "200340"
        };
    }

    public static EmployeeNumber getInstance() {
        return INSTANCE;
    }

    private final Random random;
    private final String[] employeeNumbers;

    public String getRandomEmployee() {
        int index = random.nextInt(employeeNumbers.length); 
        return employeeNumbers[index];
    }
}
