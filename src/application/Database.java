package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;


/**
 * @author Azeez G. Shola
 * @version 1.0
 */
class Database {
    /**
     * person field to access
     */
    private Person person;
    /**
     * connection to database
     */
    private Connection connection;

    private TerminalMonitor terminalMonitor = TerminalMonitor.getInstance();

    public boolean init() {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Properties properties = new Properties();
            properties.setProperty("user", "root");
            properties.setProperty("password", "root");
            properties.setProperty("useSSL", "false");
            properties.setProperty("autoReconnect", "true");
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/project_face"
                        , properties);
                terminalMonitor.addNewMessage("connection successsful");
            } catch (SQLException e) {
                terminalMonitor.addNewMessage("Error: Database Connection Failed ! Please check the connection Setting");
                return false;

            }

        } catch (ClassNotFoundException e) {
            System.out.println(e.getMessage());
            return false;
        }

        return true;
    }

    public void insert(Person person) {


        String sql = "INSERT INTO faces(code, first_name, last_name,age,reg,section) VALUES (?, ?, ?, ?,?,?)";

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
        } catch (SQLException ex) {
            terminalMonitor.addNewMessage("Error: error connection" + ex.getMessage());
        }

        try {
            statement.setInt(1, person.getCode());
            statement.setString(2, person.getFirstName());
            statement.setString(3, person.getLastName());
            statement.setInt(4, person.getReg());
            statement.setInt(5, 1);
            statement.setString(6, person.getOccupation());
            System.out.println(person.getLastName());
            int rowsInserted = statement.executeUpdate();
            System.out.println(rowsInserted);
            System.out.println(person.getLastName());
            if (rowsInserted > 0) {
                System.out.println("A new face data was inserted successfully!");
            }
        } catch (SQLException e) {
            terminalMonitor.addNewMessage("Error: error connection");
        }
    }

    public ArrayList<String> getUser(int inCode) {

        ArrayList<String> user = new ArrayList<String>();

        try {

            String sql = "select * from faces where code=" + inCode + " limit 1";

            Statement statement = connection.createStatement();

            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {
                user.add(0, Integer.toString(resultSet.getInt(2)));
                user.add(1, resultSet.getString(3));
                user.add(2, resultSet.getString(4));
                user.add(3, Integer.toString(resultSet.getInt(5)));
                user.add(4, Integer.toString(resultSet.getInt(6)));
                user.add(5, resultSet.getString(7));
            }
            connection.close();
        } catch (Exception e) {
            terminalMonitor.addNewMessage(e.getMessage());

        }
        return user;
    }

    public void db_close() {
        try {
            connection.close();
        } catch (SQLException | NullPointerException e) {
            terminalMonitor.addNewMessage("error with database closing" + e.getMessage());
        }
    }


    public Person getPerson() {
        return person;
    }

    public void setPerson(Person person) {
        this.person = person;
    }
}