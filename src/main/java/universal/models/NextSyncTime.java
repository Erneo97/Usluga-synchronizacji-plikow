package universal.models;

public enum NextSyncTime {
    sec_5(5_000),
    sec_30(30_000),
    m_5(5 * 60_000),
    h_1(60 * 60_000);

    private final long milliseconds;

    NextSyncTime(long milliseconds) {
        this.milliseconds = milliseconds;
    }

    public long getMilliseconds() {
        return milliseconds;
    }
}