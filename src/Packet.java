
public class Packet {

    private int sequenceNum;

    private boolean sent;
    private boolean arrived;
    private boolean confirmed;
    private boolean confirmedAtSender;
    private boolean lost;

    private int timeSent;
    private int timeArrived;
    private int timeConfirmed;

    private int numberOfTimesLost;

    Packet(int sequenceNum, boolean snt, boolean arrvd, boolean conf, boolean senderConf, boolean lostCond, int numTimesLost) {
        this.sequenceNum = sequenceNum;
        this.sent = snt;
        this.arrived = arrvd;
        this.confirmed = conf;
        this.confirmedAtSender = senderConf;
        this.lost = lostCond;
        this.numberOfTimesLost = numTimesLost;
    }

    public int getSequenceNum() {
        return sequenceNum;
    }

    public void setSequenceNum(int sequenceNum) {
        this.sequenceNum = sequenceNum;
    }

    public boolean isSent() {
        return sent;
    }

    public void setSent(boolean sent) {
        this.sent = sent;
    }

    public boolean isArrived() {
        return arrived;
    }

    public void setArrived(boolean arrived) {
        this.arrived = arrived;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }

    public boolean isConfirmedAtSender() {
        return confirmedAtSender;
    }

    public void setConfirmedAtSender(boolean confirmedAtSender) {
        this.confirmedAtSender = confirmedAtSender;
    }

    public boolean isLost() {
        return lost;
    }

    public void setLost(boolean lost) {
        this.lost = lost;
    }

    public int getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(int timeSent) {
        this.timeSent = timeSent;
    }

    public int getTimeArrived() {
        return timeArrived;
    }

    public void setTimeArrived(int timeArrived) {
        this.timeArrived = timeArrived;
    }

    public int getTimeConfirmed() {
        return timeConfirmed;
    }

    public void setTimeConfirmed(int timeConfirmed) {
        this.timeConfirmed = timeConfirmed;
    }

    public void resetStatus() {
        this.sent = false;
        this.arrived = false;
        this.confirmed = false;
        this.confirmedAtSender = false;
        this.lost = false;

        this.timeSent = 0;
        this.timeArrived = 0;
        this.timeConfirmed = 0;
    }

    public int getNumberOfTimesLost() {
        return numberOfTimesLost;
    }

    public void setNumberOfTimesLost(int numberOfTimesLost) {
        this.numberOfTimesLost = numberOfTimesLost;
    }

    public void printInfoStatus() {
        System.out.println("Packet sequence number: " + this.getSequenceNum() + "\n" +
                            "Sent: " + this.isSent() + "\n" +
                            "Arrived: " + this.isArrived() + "\n" +
                            "Confirmed: " + this.isConfirmed() + "\n" +
                            "Confirmed by Sender: " + this.isConfirmedAtSender() + "\n");
    }
}
