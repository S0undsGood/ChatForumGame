package combinedprojects;

public class User {

    private String login;
    private String email;
    private String first_name;
    private String last_name;
    private String user_id;
    private String session_id;
    private String registrationResult;
    private String enteringResult;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }

    public String getEnteringResult() {
        return enteringResult;
    }

    public void setEnteringResult(String enteringResult) {
        this.enteringResult = enteringResult;
    }

    public String getRegistrationResult() {
        return registrationResult;
    }

    public void setRegistrationResult(String registrationResult) {
        this.registrationResult = registrationResult;
    }

    public String getSession_id() {
        return session_id;
    }

    public void setSession_id(String session_id) {
        this.session_id = session_id;
    }

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    /*public void fillUserByID(Connection connection, String user_id) {
        log.info("filling user parameters for user_id = " + user_id);
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM users WHERE user_id=" + user_id + ";");

            while (resultSet.next()) {
                this.login = resultSet.getString("login");
                this.email = resultSet.getString("email");
                this.first_name = resultSet.getString("first_name");
                this.last_name = resultSet.getString("last_name");
            }
            log.info("user parameters are filled");

            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            log.error("SQLException during filling user parameters");
        }
    }*/
}
