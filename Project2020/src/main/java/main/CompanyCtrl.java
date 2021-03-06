package main;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompanyCtrl extends DBConnector {

    static public class CompanyCount {
        public String company;
        public int count;

        public CompanyCount(String company, int count) {
            this.company = company;
            this.count = count;
        }

        public String toString() {
            return this.company + ": " + this.count;
        }
    };

    static public class Company {
        public String name;
        public String url;
        public String country;

        public Company(String name, String url, String country) {
            this.name = name;
            this.url = url;
            this.country = country;
        }

        public String toString() { return this.name + ", " + this.url + ", " + this.country; }
    }


    /**
     * Usecase 3
     * @return Map with key genre and list of CompanyCounts (several companies in case of ties)
     * @throws SQLException
     */
    public Map<String, List<CompanyCount>> getCompaniesWithMostMoviesInCategory() throws SQLException {
        Map<String, List<CompanyCount>> result = new HashMap<>();
        PreparedStatement prep = conn.prepareStatement(
    "SELECT cat.CategoryID, cat.name AS CategoryName, c.CompanyID, c.name AS CompanyName, count(*) AS count FROM company c JOIN companyTitle ct ON c.CompanyID = ct.CompanyID JOIN title t ON ct.TitleID = t.TitleID JOIN categoryInTitle cit ON cit.TitleID = t.TitleID " +
            "JOIN category cat ON cit.CategoryID = cat.CategoryID "+
            "WHERE t.TitleID NOT IN (SELECT SeriesID FROM episodeInSeries)" +
            "GROUP BY cat.CategoryID, c.CompanyID " +
            "HAVING count >= ALL (" +
            "SELECT count(*) as count FROM company c2 JOIN companyTitle ct2 ON c2.CompanyID = ct2.CompanyID JOIN title t2 ON ct2.TitleID = t2.TitleID JOIN categoryInTitle cit2 ON cit2.TitleID = t2.TitleID " +
            "JOIN category cat2 ON cit2.CategoryID = cat2.CategoryID " +
            "WHERE cat2.CategoryID = cat.CategoryID AND t2.TitleID NOT IN (" +
            "SELECT SeriesID FROM episodeInSeries ) " +
            "GROUP BY cat2.CategoryID, c2.CompanyID "+
            ");");
        ResultSet rs = prep.executeQuery();
        while (rs.next()) {
            String category = rs.getString("CategoryName");
            String company = rs.getString("CompanyName");
            int count = rs.getInt("count");
            if (!result.containsKey(category)) {
                result.put(category, new ArrayList<>());
            }
            result.get(category).add(new CompanyCount(company, count));
        }
        System.out.println(rs);
        return result;
    }

    public int insertCompany(String name, String url, String country) {
        try {
        PreparedStatement prep = conn.prepareStatement("INSERT INTO company (URL, Country, Name) VALUES (?, ?, ?);", Statement.RETURN_GENERATED_KEYS);
        prep.setString(1, url);
        prep.setString(2, country);
        prep.setString(3, name);
        int updatedColumns = prep.executeUpdate();
        ResultSet rs = prep.getGeneratedKeys();

        if (rs.next()){
            return rs.getInt(1);
        }
        return -1;}
        catch (SQLException e){
                try {
                    return getID(name);
                }
                catch (SQLException ex){
                    System.out.println("Something went wrong");
                    return -1;
                }

        }
    }
    private int getID(String name) throws SQLException{
        PreparedStatement prep = conn.prepareStatement("SELECT CompanyID from company where Name = ?");
        prep.setString(1, name);
        ResultSet rs = prep.executeQuery();

        if (rs.next()){
            return rs.getInt(1);
        }
        return -1;
    }

    public void linkCompanyTitle(int companyID, int titleID, String role) throws SQLException{
        PreparedStatement prep = conn.prepareStatement("INSERT INTO companyTitle (titleid, companyid, role) VALUES (?, ?, ?);");
        prep.setInt(1, titleID);
        prep.setInt(2, companyID);
        prep.setString(3, role);
        int updatedColumns = prep.executeUpdate();
    }

    public List<Company> getCompanies() throws SQLException {
        PreparedStatement prep = conn.prepareStatement("SELECT * FROM company");
        ResultSet rs = prep.executeQuery();
        ArrayList<Company> result = new ArrayList<>();
        while (rs.next()) {
            result.add(new Company(rs.getString("Name"), rs.getString("URL"), rs.getString("Country")));
        }
        return result;
    }

    public static void main(String[] args) {
        CompanyCtrl c = new CompanyCtrl();
        c.connect();
        try {
            System.out.println(c.getCompaniesWithMostMoviesInCategory());

            System.out.println(c.getCompanies());
        } catch (Exception e) {
            System.out.println(e); }
    }
};
