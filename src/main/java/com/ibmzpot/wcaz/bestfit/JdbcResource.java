package com.ibmzpot.wcaz.bestfit;

import jakarta.annotation.Resource;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;
import java.util.logging.Logger;

@Path("/jdbc")
public class JdbcResource {

    @Resource(name = "jdbc/defaultDataSource")
    private DataSource dataSource;

    private static final String SQL_READ = "SELECT EMPNO, FIRSTNME, LASTNAME, SALARY FROM EMP WHERE EMPNO = ?";
    private static final String SQL_UPDATE = "UPDATE EMP SET BONUS = ? WHERE EMPNO = ?";
    private static final Logger LOGGER = Logger.getLogger(JdbcResource.class.getName());

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response executeSql(
            @QueryParam("reads") @DefaultValue("0") int readCount,
            @QueryParam("updates") @DefaultValue("0") int updateCount) {

        Connection connection = null;
        PreparedStatement selectStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet resultSet = null;
        int higherCount = Math.max(readCount, updateCount);
        int rowsUpdated = 0;
        int i = 0;

        EmployeeNumber employeeNumber = EmployeeNumber.getInstance();
        String randomEmployee = "";
        BigDecimal salary;
        BigDecimal totalSalary = new BigDecimal("0.00");
        BigDecimal bonus;
        BigDecimal totalBonus = new BigDecimal("0.00");

        long startTime = System.nanoTime();
        long endTime;
        long durationMs;
        int readsPerformed = 0;
        int updatesPerformed = 0;
        String message = "Operations completed.";

        JsonObject jsonResponse;

        try {
            connection = dataSource.getConnection();
            selectStmt = connection.prepareStatement(SQL_READ);
            updateStmt = connection.prepareStatement(SQL_UPDATE);

            for (i = 0; i < higherCount; i++) {
                randomEmployee = employeeNumber.getRandomEmployee();
                if (i < readCount) {
                    selectStmt.setString(1, randomEmployee);
                    resultSet = selectStmt.executeQuery();
                    if (resultSet.next()) {
                        //LOGGER.info("Read successful for ID: " + randomEmployee + ".");
                        salary = resultSet.getBigDecimal("SALARY");
                        totalSalary = totalSalary.add(salary);
                        readsPerformed++;
                    } else {
                        LOGGER.warning("Read failed for ID: " + randomEmployee + " (ID not found).");
                    }
                }
                if (i < updateCount) {
                    bonus = randomBonus();
                    totalBonus = totalBonus.add(bonus);
                    updateStmt.setBigDecimal(1, bonus);
                    updateStmt.setString(2, randomEmployee);
                    rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        updatesPerformed++;
                        //LOGGER.info("Update successful for ID: " + randomEmployee + "; bonus paid: " + bonus);
                    } else {
                        LOGGER.warning("Update failed for ID: " + randomEmployee + " (ID not found or no change).");
                    }
                }
            }

            connection.commit();

        } catch (SQLException e) {
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Database Error: " + e.getMessage())
                    .build();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (updateStmt != null)
                    updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (selectStmt != null)
                    selectStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            try {
                if (connection != null)
                    connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        endTime = System.nanoTime();
        durationMs = (endTime - startTime) / 1_000_000;

        jsonResponse = Json.createObjectBuilder()
                .add("message", message)
                .add("readsPerformed", readsPerformed)
                .add("updatesPerformed", updatesPerformed)
                .add("durationMs", durationMs)
                .add("totalEmployeeSalary", totalSalary)
                .add("totalEmployeeBonus", totalBonus)
                .build();

        return Response.ok()
                .entity(jsonResponse)
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    // Return a random decimal between 100.00 and 900.00
    public static BigDecimal randomBonus() {
        final Random random = new Random();
        int minMultiplier = 1;
        int maxMultiplier = 9;
        int randomInt = random.nextInt(maxMultiplier - minMultiplier + 1) + minMultiplier;
        int rawValue = randomInt * 100;
        return new BigDecimal(rawValue).setScale(2, RoundingMode.UNNECESSARY);
    }
}