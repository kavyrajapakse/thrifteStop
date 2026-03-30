package hibernate;

import javax.persistence.*;

@Entity
@Table(name = "admin")
public class ThriftAdmin {

    @Id
    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "fname", nullable = false, length = 45)
    private String fname;

    @Column(name = "username", nullable = false, length = 20)
    private String username;

    @Column(name = "password", nullable = false, length = 10)
    private String password;

    @Column(name = "verify_code", length = 50)
    private String verifyCode;

    // Getters and Setters

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String fname) {
        this.fname = fname;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }
}
