package br.com.dio;

import br.com.dio.persistence.ConnectionUtil;
//import br.com.dio.persistence.EmployeeAuditDAO;
import br.com.dio.persistence.EmployeeAuditDAO;
import br.com.dio.persistence.EmployeeDAO;
import br.com.dio.persistence.EmployeeParamDAO;
import br.com.dio.persistence.entity.EmployeeEntity;
import org.flywaydb.core.Flyway;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.time.OffsetDateTime;

public class Main {

    private final static EmployeeParamDAO employeeDAO = new EmployeeParamDAO();
    private final static EmployeeAuditDAO employeeAuditDAO = new EmployeeAuditDAO();

    public static void main(String[] args){

        var flyway = Flyway.configure()
                .dataSource("jdbc:mysql://localhost/jdbc-sample", "root", "root")
                .load();
        flyway.migrate();

        //INSERT
        var insert = new EmployeeEntity();
        insert.setName("João'");
        insert.setSalary(new BigDecimal("50000"));
        insert.setBirthday(OffsetDateTime.now().minusYears(20));
        System.out.println(insert);
        employeeDAO.insertWithProcedure(insert);
        System.out.println(insert);


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
