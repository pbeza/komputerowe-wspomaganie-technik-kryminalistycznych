package backend.db;

import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.opencv.core.Core;
import org.opencv.core.Mat;

import backend.CsvParser;
import backend.Log;

public class DatabaseConnectionManager {

    private final static DatabaseConnectionManager singleton = new DatabaseConnectionManager();
    private final static Log log = Log.getLogger();
    private final static String HIBERNATE_XML_CFG = "hibernate.cfg.xml";
    private final SessionFactory sessionFactory;
    private final Configuration configuration;

    /**
     * Add predefined images from predefined location defined in CSV
     * configuration file. Run this to feed database with images.
     */
    public static void main(String[] args) {
        addAllFacesFromPredefinedCsv();
        // Simple usage:
        //
        // final int labelId = 100;
        // DatabaseConnectionManager m =
        // DatabaseConnectionManager.getInstance();
        //
        // // Load face from database by label ID
        //
        // FaceEntity fe = m.getSingleFaceFromDatabaseByLabelId(labelId);
        // if (fe == null) {
        // System.out.println("Face with labelID = " + labelId + " wasn't
        // found");
        // }
        //
        // // Load face from database by primary key ID
        //
        // final int primaryKeyId = 169;
        // m.saveFaceFromDatabaseToFileByPrimaryKeyId(primaryKeyId,
        // "face_with_ID_" + primaryKeyId + ".gif");
        //
        // // Close connection and log handlers
        //
        // log.closeHandlers();
        // m.closeConnection();
    }

    private DatabaseConnectionManager() {
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
        configuration = new Configuration();
        configuration.addAnnotatedClass(FaceEntity.class);
        configuration.configure(HIBERNATE_XML_CFG);
        sessionFactory = configuration.buildSessionFactory();
    }

    public static DatabaseConnectionManager getInstance() {
        return singleton;
    }

    public static void addAllFacesFromPredefinedCsv() {
        DatabaseConnectionManager m = new DatabaseConnectionManager();
        boolean isSuccess = m.addFacesToDatabaseFromCsv(
                CsvParser.FACES_LEARNING_SET_CSV_PATH);
        String msg = "Loading files from "
                + CsvParser.FACES_LEARNING_SET_CSV_PATH
                + (isSuccess ? " ended successfully" : " failed");
        Level logLevel = isSuccess ? Level.INFO : Level.SEVERE;
        log.log(logLevel, msg);
        m.closeConnection();
    }

    public void closeConnection() {
        sessionFactory.close();
    }

    public boolean saveFaceFromDatabaseToFileByPrimaryKeyId(int id,
            String outputPath) {
        FaceEntity fe = getSingleFaceFromDatabaseByPrimaryKeyId(id);
        boolean wasFaceFound = fe != null;
        if (wasFaceFound) {
            fe.saveFaceImageToFile(outputPath);
        } else {
            log.info("Face with primary key ID = " + id + " wasn't found");
        }
        return wasFaceFound;
    }

    public boolean addFacesToDatabaseFromCsv(String facesCsvPath) {
        CsvParser parser = new CsvParser(facesCsvPath);
        List<Mat> mats = new ArrayList<>();
        List<Integer> labels = new ArrayList<>(),
                imagesLabels = new ArrayList<>();
        List<String> canonicalPaths = new ArrayList<>();
        try {
            parser.readCsv(mats, labels, imagesLabels, canonicalPaths);
        } catch (IOException | URISyntaxException e) {
            log.severe("Adding images to database has failed. Details: "
                    + e.getMessage());
            return false;
        }
        if (mats.size() != labels.size()
                || labels.size() != canonicalPaths.size()) {
            log.severe("Reading CSV has failed. Arrays length mismatch.");
            return false;
        }
        for (int i = 0; i < labels.size(); i++) {
            int label = labels.get(i), imageLabel = imagesLabels.get(i);
            Path path = Paths.get(canonicalPaths.get(i));
            byte[] img;
            try {
                img = Files.readAllBytes(path);
            } catch (IOException e) {
                log.severe("Failed to readAllBytes from image " + path
                        + ". Details: " + e.getMessage());
                return false;
            }
            String filetype = "unknown";
            try {
                filetype = Files.probeContentType(path);
            } catch (IOException e) {
            }
            int faceId = addFaceToDatabase(label, imageLabel, img,
                    path.getFileName().toString(), filetype);
            log.info("Face with label ID = " + faceId + " added successfully");
        }
        return true;
    }

    public Integer addFaceToDatabase(int label, int imageLabel,
            byte[] faceImage, String filename, String filetype) {
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        Integer faceID = null;
        try {
            tx = session.beginTransaction();
            FaceEntity face = new FaceEntity();
            face.setPersonId(label);
            face.setImageId(imageLabel);
            face.setImage(faceImage);
            face.setFilename(filename);
            face.setFiletype(filetype);
            faceID = (Integer) session.save(face);
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.severe("Hibernate exception. Details: " + e.getMessage()
                    + e.getCause());
            throw e;
        } finally {
            session.close();
        }
        return faceID;
    }

    public FaceEntity getSingleFaceFromDatabaseByLabelId(int labelId) {
        String query = "FROM FaceEntity FE WHERE FE.label = " + labelId;
        return getSingleFaceFromDatabase(query);
    }

    public FaceEntity getSingleFaceFromDatabaseByPrimaryKeyId(
            int primaryKeyId) {
        String query = "FROM FaceEntity FE WHERE FE.id = " + primaryKeyId;
        return getSingleFaceFromDatabase(query);
    }

    public FaceEntity getSingleFaceFromDatabase(String query) {
        List<FaceEntity> l = getFacesFromDatabase(query);
        if (l.size() > 1) {
            throw new AssertionError("Expected at most one face with id");
        }
        return l.size() == 0 ? null : l.get(0);
    }

    public List<FaceEntity> getAllFaces() {
        return getFacesFromDatabase("FROM FaceEntity");
    }

    public List<FaceEntity> getFacesFromDatabase(String query) {
        List<FaceEntity> l = new ArrayList<>();
        Session session = sessionFactory.openSession();
        Transaction tx = null;
        try {
            tx = session.beginTransaction();
            l = session.createQuery(query).list();
            for (FaceEntity face : l) {
                log.fine("Loaded image: face ID: " + face.getId()
                        + ", person ID: " + face.getPersonId() + ", image ID "
                        + face.getImageId());
            }
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            log.severe(e.getMessage() + e.getCause());
            e.printStackTrace();
        } finally {
            session.close();
        }
        return l;
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
