package tools;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class DB_Manager {
    public static void main (String[] args) throws Exception {
        String URL = "jdbc:sqlserver://localhost:1433;" + "database=WONKA_BACKOFFICE_TEST_18547;" + "encrypt=false;" + "trustServerCertificate=true;";
        String User = "sa";
        String Password = "Password_01";
        Connection con = java.sql.DriverManager.getConnection(URL, User, Password);
        Statement st = con.createStatement();
        ResultSet rs = st.executeQuery("SELECT * FROM dbo.Taxes_TaxGroup WHERE Name = 'Saul Test 03MARZO2026';");
        while (rs.next()) {
            System.out.println("Tabla: " + rs.getString(1));
        }
        con.close();
    }

}
