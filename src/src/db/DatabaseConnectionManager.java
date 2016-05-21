package db;

import java.util.List;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

public class DatabaseConnectionManager {

    private static SessionFactory factory;

    public static void main(String[] args) {
        try {
            // factory = new Configuration().configure().addPackage("db") // add
            // // package
            // // if
            // // used.
            // .addAnnotatedClass(FaceEntity.class).buildSessionFactory();
            Configuration c = new Configuration();
            c.addPackage("db");
            c.addAnnotatedClass(FaceEntity.class);
            c.configure("hibernate.cfg.xml");
            factory = c.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Failed to create sessionFactory object." + ex);
            throw new ExceptionInInitializerError(ex);
        }
        DatabaseConnectionManager cm = new DatabaseConnectionManager();

        /* Add few employee records in database */
        // Integer empID1 = cm.addEmployee("Zara", "Ali", 1000);
        // Integer empID2 = cm.addEmployee("Daisy", "Das", 5000);
        // Integer empID3 = cm.addEmployee("John", "Paul", 10000);

        /* List down all the employees */
        cm.listAllFaces();

        /* Update employee's records */
        // cm.updateEmployee(empID1, 5000);

        /* Delete an employee from the database */
        // cm.deleteEmployee(empID2);

        /* List down new list of the employees */
        // cm.listAllFaces();

        factory.close();
    }

    // /* Method to CREATE an employee in the database */
    // public Integer addEmployee(String fname, String lname, int salary) {
    // Session session = factory.openSession();
    // Transaction tx = null;
    // Integer employeeID = null;
    // try {
    // tx = session.beginTransaction();
    // Employee employee = new Employee();
    // employee.setFirstName(fname);
    // employee.setLastName(lname);
    // employee.setSalary(salary);
    // employeeID = (Integer) session.save(employee);
    // tx.commit();
    // } catch (HibernateException e) {
    // if (tx != null)
    // tx.rollback();
    // e.printStackTrace();
    // } finally {
    // session.close();
    // }
    // return employeeID;
    // }

    public void listAllFaces() {
        Session session = factory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            List<FaceEntity> faces = session.createQuery("FROM FaceEntity").list();
            for (FaceEntity face : faces) {
                System.out.print("Face ID: " + face.getId());
                System.out.print("Label ID: " + face.getLabel());
                // System.out.println("Image: " + face.getImage());
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            session.close();
        }
    }

    // /* Method to UPDATE salary for an employee */
    // public void updateEmployee(Integer EmployeeID, int salary) {
    // Session session = factory.openSession();
    // Transaction tx = null;
    // try {
    // tx = session.beginTransaction();
    // Employee employee = (Employee) session.get(Employee.class, EmployeeID);
    // employee.setSalary(salary);
    // session.update(employee);
    // tx.commit();
    // } catch (HibernateException e) {
    // if (tx != null)
    // tx.rollback();
    // e.printStackTrace();
    // } finally {
    // session.close();
    // }
    // }
    //
    // /* Method to DELETE an employee from the records */
    // public void deleteEmployee(Integer EmployeeID) {
    // Session session = factory.openSession();
    // Transaction tx = null;
    // try {
    // tx = session.beginTransaction();
    // Employee employee = (Employee) session.get(Employee.class, EmployeeID);
    // session.delete(employee);
    // tx.commit();
    // } catch (HibernateException e) {
    // if (tx != null)
    // tx.rollback();
    // e.printStackTrace();
    // } finally {
    // session.close();
    // }
    // }
}
