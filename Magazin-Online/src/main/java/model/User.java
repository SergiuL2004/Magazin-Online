package model;

public class User {
    public enum Role { ADMIN, SELLER, BUYER }

    private String email;
    private String password;
    private Role role;
    private boolean approved;
    private boolean active;

    public User() {}

    public User(String email, String password, Role role, boolean approved, boolean active) {
        this.email = email;
        this.password = password;
        this.role = role;
        this.approved = approved;
        this.active = active;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
