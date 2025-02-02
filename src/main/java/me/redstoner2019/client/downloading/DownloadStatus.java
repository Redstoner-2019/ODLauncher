package me.redstoner2019.client.downloading;

public class DownloadStatus {
    private long bytesTotal = 0;
    private long bytesRead = 0;
    private boolean complete = false;
    private int status = 200;
    private String message = null;

    public DownloadStatus() {

    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getBytesTotal() {
        return bytesTotal;
    }

    public void setBytesTotal(long bytesTotal) {
        this.bytesTotal = bytesTotal;
    }

    public long getBytesRead() {
        return bytesRead;
    }

    public void setBytesRead(long bytesRead) {
        this.bytesRead = bytesRead;
    }

    public boolean isComplete() {
        return complete;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public double getPercent(){
        if(bytesTotal == 0) return 0;
        return ((double) bytesRead / bytesTotal) * 100;
    }

    public void reset(){
        setBytesTotal(0);
        setBytesRead(0);
        setStatus(200);
        setComplete(false);
    }
}
