package io.accounts;

public interface AdminManager {
    boolean authenticateAsAdmin();
    boolean checkIsAuthenticated();
    boolean createUser();
}
