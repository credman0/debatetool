package io.accounts;

public interface DBLock {
    boolean tryLock(byte[] hash);
    void unlock(byte[] hash);
    void unlockAll();
    void unlockAllExcept(byte[] hash);
}
