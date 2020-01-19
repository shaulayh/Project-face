package application;

import java.sql.*;
import java.util.ArrayList;
import java.util.Properties;

class Database {

    private Person person;

    private Connection connection;

    private TerminalMonitor terminalMonitor = TerminalMonitor.getInstance();

    public boolean init() throws SQLException {
        try {
            Class.forName("com.mysql.jdbc.Driver");

            Properties properties = new Properties();
            properties.setProperty("user", "root");
            properties.setProperty("password", "root");
            properties.setProperty("useSSL", "false");
            properties.setProperty("autoReconnect", "true");
            try {
                this.connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/ghosteye"
                        , properties);
                terminalMonitor.addNewMessage("connection successsful");
            } catch (SQLException e) {
                terminalMonitor.addNewMessage("Error: Database Connection Failed ! Please check the connection Setting");
                e.printStackTrace();
                return false;

            }

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    public void insert(Person person) {
        String sql = "INSERT INTO face_bio (code, first_name, last_name, reg, age , section) VALUES (?, ?, ?, ?,?,?)";

        PreparedStatement statement = null;
        try {
            statement = connection.prepareStatement(sql);
        } catch (SQLException ex) {
            terminalMonitor.addNewMessage("Error: error connection" + ex.getMessage());
        }

        try {

            assert statement != null;
            statement.setInt(1, person.getCode());
            statement.setString(2, person.getFirstName());
            statement.setString(3, person.getLastName());
            statement.setInt(4, person.getReg());
            statement.setInt(5, person.getAge());
            statement.setString(6, person.getOccupation());

            int rowsInserted = statement.executeUpdate();
            if (rowsInserted > 0) {
                terminalMonitor.addNewMessage("A new face data was inserted successfully!");
            }
        } catch (SQLException e) {
            terminalMonitor.addNewMessage("Error: error connection");
        }
    }

    public ArrayList<String> getUser(int inCode) throws SQLException {

        ArrayList<String> user = new ArrayList<String>();

        try {

            String sql = "select * from face_bio where code=" + inCode + " limit 1";

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
            connection.close(); // closing connection
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