package br.com.dio;

import br.com.dio.persistence.ConnectionUtil;
//import br.com.dio.persistence.EmployeeAuditDAO;
import br.com.dio.persistence.EmployeeDAO;
import br.com.dio.persistence.entity.EmployeeEntity;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class Main {

    private final static EmployeeDAO employeeDAO = new EmployeeDAO();
    //private final static EmployeeAuditDAO employeeAuditDAO = new EmployeeAuditDAO();

    public static void main(String[] args){

        var flyway = Flyway.configure()
                .dataSource("jdbc:mysql://localhost/jdbc-sample", "root", "root")
                .load();
        flyway.migrate();

        /*
        //INSERT
        var insert = new EmployeeEntity();
        insert.setName("João");
        insert.setSalary(new BigDecimal("10000"));
        insert.setBirthday(OffsetDateTime.now().minusYears(20));
        System.out.println(insert);
        employeeDAO.insert(insert);
        System.out.println(insert);
         */



        //employeeDAO.delete(10);

        /*
        SELECT
        employeeDAO.findAll().forEach(System.out::println);
         */

        /*
        SELECT
        System.out.println(employeeDAO.findById(1));
         */

        /*
        //UPDATE
        var update = new EmployeeEntity();
        update.setId(insert.getId());
        update.setName("Gabriel");
        update.setSalary(new BigDecimal("9000"));
        update.setBirthday(OffsetDateTime.now().minusYears(18).minusDays(22));
        employeeDAO.update(update);

         */

        //employeeDAO.delete(insert.getId());






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
