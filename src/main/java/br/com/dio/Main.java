package br.com.dio;

import br.com.dio.persistence.ConnectionUtil;
//import br.com.dio.persistence.EmployeeAuditDAO;
import br.com.dio.persistence.EmployeeAuditDAO;
import br.com.dio.persistence.EmployeeDAO;
import br.com.dio.persistence.EmployeeParamDAO;
import br.com.dio.persistence.entity.EmployeeEntity;
import net.datafaker.Faker;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.Locale;
import java.util.stream.Stream;

import static java.time.ZoneOffset.UTC;

public class Main {

    private final static EmployeeParamDAO employeeDAO = new EmployeeParamDAO();
    private final static EmployeeAuditDAO employeeAuditDAO = new EmployeeAuditDAO();
    private final static Faker faker = new Faker(Locale.of("pt", "BR"));

    public static void main(String[] args){

        var flyway = Flyway.configure()
                .dataSource("jdbc:mysql://localhost/jdbc-sample", "root", "root")
                .load();
        flyway.migrate();

        /*
        //INSERT
        var insert = new EmployeeEntity();
        insert.setName("João");
        insert.setSalary(new BigDecimal("50000"));
        insert.setBirthday(OffsetDateTime.now().minusYears(20));
        System.out.println(insert);
        employeeDAO.insertWithProcedure(insert);
        System.out.println(insert);
         */


        /*
        SELECT
        employeeDAO.findAll().forEach(System.out::println);
         */

        /*
        SELECT
        System.out.println(employeeDAO.findById(1));
         */


        //UPDATE
        /*var update = new EmployeeEntity();
        //update.setId(insert.getId());
        update.setId(1L);
        update.setName("João");
        update.setSalary(new BigDecimal("60000"));
        update.setBirthday(OffsetDateTime.now().minusYears(20).minusDays(1));
        employeeDAO.update(update);

         */


        //employeeDAO.delete(10);
        //employeeDAO.delete(insert.getId());

        //employeeAuditDAO.findAll().forEach(System.out::println);

        var entities = Stream.generate(() -> {
            var employee = new EmployeeEntity();
            employee.setName(faker.name().fullName());
            employee.setSalary(new BigDecimal(faker.number().digits(4)));
            employee.setBirthday(OffsetDateTime.of(LocalDate.now().minusYears(faker.number().numberBetween(40, 20)), LocalTime.MIN, UTC));
            return employee;
        }).limit(10000).toList();

        employeeDAO.insert(entities);



        /*
        Só para test
        try (var connection = ConnectionUtil.getConnection()) {
            System.out.println("Conectou!");
        }catch (SQLException ex){
            ex.printStackTrace();
        }
         */
    }
}
