package com.smartparking.bootstrap.db;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.sql.SQLTransientException;
import java.util.Objects;
import java.util.logging.Logger;

/**
 * A DataSource decorator that retries obtaining a JDBC Connection on transient failures.
 *
 * <p>Design notes:
 * <ul>
 *   <li>Connection retry is intentionally scoped to {@link #getConnection()} only.</li>
 *   <li>SQLState starting with "08" generally indicates connection errors per SQL standard.</li>
 *   <li>Backoff uses a simple exponential strategy with an upper bound.</li>
 * </ul>
 */
public final class RetryingDataSource implements DataSource {
    private final DataSource delegate;
    private final int maxAttempts;
    private final long initialBackoffMs;
    private final long maxBackoffMs;

    public RetryingDataSource(DataSource delegate, int maxAttempts, long initialBackoffMs, long maxBackoffMs) {
        this.delegate = Objects.requireNonNull(delegate, "delegate");
        this.maxAttempts = Math.max(1, maxAttempts);
        this.initialBackoffMs = Math.max(0, initialBackoffMs);
        this.maxBackoffMs = Math.max(this.initialBackoffMs, maxBackoffMs);
    }

    @Override
    public Connection getConnection() throws SQLException {
        return retry(delegate::getConnection);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return retry(() -> delegate.getConnection(username, password));
    }

    private Connection retry(SqlSupplier<Connection> supplier) throws SQLException {
        SQLException last = null;
        long backoff = initialBackoffMs;

        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            try {
                return supplier.get();
            } catch (SQLException e) {
                last = e;
                if (!isRetryable(e) || attempt == maxAttempts) {
                    throw e;
                }
                sleep(backoff);
                backoff = Math.min(maxBackoffMs, Math.max(1, backoff) * 2);
            }
        }

        throw last == null ? new SQLException("Failed to get connection") : last;
    }

    private static boolean isRetryable(SQLException e) {
        if (e instanceof SQLTransientConnectionException || e instanceof SQLTransientException) {
            return true;
        }
        String sqlState = e.getSQLState();
        return sqlState != null && sqlState.startsWith("08");
    }

    private static void sleep(long ms) {
        if (ms <= 0) {
            return;
        }
        try {
            Thread.sleep(ms);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return delegate.unwrap(iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return delegate.isWrapperFor(iface);
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return delegate.getLogWriter();
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        delegate.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        delegate.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return delegate.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        try {
            return delegate.getParentLogger();
        } catch (Exception e) {
            return Logger.getLogger(RetryingDataSource.class.getName());
        }
    }

    @FunctionalInterface
    private interface SqlSupplier<T> {
        T get() throws SQLException;
    }
}

