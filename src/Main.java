import java.util.ArrayList;
import java.util.Scanner;

// *Notes: - Sender does not have a storage buffer, packets must be remade and retransmitted


public class Main {

    private static int masterTimer;

    private static int numberOfPackets;

    private static Packet[] masterPktList;
    private static int currSeqNumber;
    private static ArrayList<Integer> indicesOfUnconfirmedPacketsInPipeline = new ArrayList<>();
    private static ArrayList<Integer> indicesToBeRemovedFromPipelineList = new ArrayList<>();

    private static int endToEndTime;
    private static int timeOutAfter;
    private static int timeBetweenSends;

    private static int senderWindowSize;

    private static Scanner keyboardInput = new Scanner(System.in);


    public static void main(String[] args) {

        // get user mode selection
        int mode = userModeSelection();

        switch (mode) {
            case 1:
                // get parameter inputs from user
                getSimulationParametersFromUser();
                break;
            case 2:
                // load preset parameters
                assignPresetParameters();
                break;
            default:
                break;
        }

        masterPktList = createSenderPacketList(numberOfPackets);

        try {
            startSimulation();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private static int userModeSelection() {
        int userSelection;
        System.out.println("Select a simulation mode: ");
        System.out.println("1) Custom Parameters");
        System.out.println("2) Preset Parameters");
        userSelection = keyboardInput.nextInt();
        return userSelection;
    }

    private static void assignPresetParameters() {
        numberOfPackets = 20;
        System.out.println("Packets to be sent: " + numberOfPackets);
        senderWindowSize = 5;
        System.out.println("Sender's window size: " + senderWindowSize);
        endToEndTime = 20;
        System.out.println("End-to-End packet travel time: " + endToEndTime);
        timeBetweenSends = 2;
        System.out.println("Time between packet sends: " + timeBetweenSends);
        timeOutAfter = 40;
        System.out.println("Timeout limit: " + timeOutAfter);
        System.out.println("----------------------------------------------------");
    }

    private static void getSimulationParametersFromUser() {
        System.out.print("Enter the total number of Packets to be sent: ");
        numberOfPackets = keyboardInput.nextInt();
        System.out.print("Enter the Sender's window size: ");
        senderWindowSize = keyboardInput.nextInt();
        System.out.print("Enter the End-to-End packet travel time: ");
        endToEndTime = keyboardInput.nextInt();
        System.out.print("Enter the time between packet sends: ");
        timeBetweenSends = keyboardInput.nextInt();
        System.out.print("Enter packet timeout time limit: ");
        timeOutAfter = keyboardInput.nextInt();
        System.out.println("----------------------------------------------------");
    }

    private static void startSimulation() throws InterruptedException {
        System.out.println("Simulation started...");

        masterTimer = 0;
        currSeqNumber = -1;
        int arrivedPacketIndex;

        // send initial packets - send enough to fill sender window to start (assume that packets are sent
        // and processed much faster than round trip time)
        for(int i = 0; i<senderWindowSize; i++) {
            // point to current packet
            currSeqNumber++;
            // send packet
            sendPacket(currSeqNumber, masterTimer);
            // put packet index in unconfirmed list
            indicesOfUnconfirmedPacketsInPipeline.add(masterPktList[currSeqNumber].getSequenceNum());

            // "wait" until next packet is ready to send and adjust timer
            masterTimer += timeBetweenSends;
        }

        // *** MAIN LOOP *** ----------------------------------------------------------------------

        while(!masterPktList[masterPktList.length-1].isConfirmed() || masterTimer < 500000) {
            System.out.println("------------");
            System.out.println("Timer: " + masterTimer);

            // check all packets in pipeline and check if one has arrived at sender

            // check to see if any packet confirmations have arrived back to the sender.
            updatePipelineList();
            System.out.println("Packets in pipeline: " + indicesOfUnconfirmedPacketsInPipeline);

            // delay loop to examine
            Thread.sleep(500);

            // run send script at appropriate time and if window is not full
            if(masterPktList[currSeqNumber].isSent() && masterTimer%timeBetweenSends == 0 &&
                    masterPktList[currSeqNumber-senderWindowSize+1].isConfirmed() &&
                    masterPktList[currSeqNumber-senderWindowSize+1].isConfirmedAtSender()) {

                if(currSeqNumber < numberOfPackets-1) {
                    currSeqNumber++;
                    sendPacket(currSeqNumber, masterTimer);
                    indicesOfUnconfirmedPacketsInPipeline.add(masterPktList[currSeqNumber].getSequenceNum());
                }

                // hardcode lost packets here.....
                // *****&&&&&& HARD CODED PACKET LOSS on packet 7
                if(currSeqNumber == 7 && !(masterPktList[7].getNumberOfTimesLost() > 0) ) {
                    masterPktList[7].setLost(true);
                }
                // ***********************************************



            }

            // check timeout timer on all packets currently in pipeline, if timed out setup pipeline and point current
            // sequence number to packet before lost packet
            checkTimeOutOnPipelinedPackets();

            // update status based on timers for arrived packet and get that packet
            arrivedPacketIndex = getArrivedPacketIndex();
            // trigger receiver script
            if(arrivedPacketIndex == 0) {
                masterPktList[arrivedPacketIndex].setConfirmed(true);
                masterPktList[arrivedPacketIndex].setTimeConfirmed(masterTimer);
                System.out.println("Packet " + masterPktList[arrivedPacketIndex].getSequenceNum() + " has been confirmed" +
                        " by receiver.");
            }
            else if(arrivedPacketIndex != -1 && masterPktList[arrivedPacketIndex-1].isArrived()) {
                masterPktList[arrivedPacketIndex].setConfirmed(true);
                masterPktList[arrivedPacketIndex].setTimeConfirmed(masterTimer);
                System.out.println("Packet " + masterPktList[arrivedPacketIndex].getSequenceNum() + " has been confirmed" +
                        " by receiver.");

            }







            // clock masterTimer
            masterTimer++;

        // *** MAIN LOOP *** ----------------------------------------------------------------------
        }
    }

    private static Packet[] createSenderPacketList(int numPackets) {

        Packet[] masterPktLst = new Packet[numPackets];

        for(int i = 0; i < masterPktLst.length; i++) {
            Packet newPacket = new Packet(i, false, false, false, false, false, 0);
            masterPktLst[i] = newPacket;
        }
        // print all packets for status check
//        for( Packet pkt : masterPktLst) {
//            pkt.printInfoStatus();
//        }
        return masterPktLst;
    }

    private static int getArrivedPacketIndex() {
        int arrivedPktIndex = -1;
        for( int pktIndex : indicesOfUnconfirmedPacketsInPipeline ) {
            // if packet hasn't arrived at receiver yet and it has traversed the path to receiver without getting lost,
            // update packet status to "arrived" and record time arrived at receiver
            if(!masterPktList[pktIndex].isArrived() && !masterPktList[pktIndex].isLost() &&
                    masterTimer - masterPktList[pktIndex].getTimeSent() >= endToEndTime) {
                masterPktList[pktIndex].setArrived(true);
                masterPktList[pktIndex].setTimeArrived(masterTimer);
                arrivedPktIndex = masterPktList[pktIndex].getSequenceNum();
                System.out.println("Packet " + masterPktList[pktIndex].getSequenceNum() + " has arrived at receiver.");
            }
        }
        return arrivedPktIndex;
    }

    private static void sendPacket(int pktIndex, int sendTime) {
        masterPktList[pktIndex].resetStatus();
        masterPktList[pktIndex].setSent(true);
        masterPktList[pktIndex].setTimeSent(sendTime);
        System.out.println("Packet " + masterPktList[pktIndex].getSequenceNum() + " has been sent.");
    }

    // takes care of confirmation at sender and updates pipeline list
    private static void updatePipelineList() {

        indicesToBeRemovedFromPipelineList.clear();

        // collect all indices that must be removed from pipeline list
        for( Integer pktIndex : indicesOfUnconfirmedPacketsInPipeline ) {
            if(masterPktList[pktIndex].isConfirmed() && masterTimer - masterPktList[pktIndex].getTimeConfirmed() >=
                    endToEndTime) {
                masterPktList[pktIndex].setConfirmedAtSender(true);
                indicesToBeRemovedFromPipelineList.add(pktIndex);
                System.out.println("Packet " + masterPktList[pktIndex].getSequenceNum() + " has been confirmed back at Sender.");
            }
        }
        for( Integer pIndex : indicesToBeRemovedFromPipelineList ) {
            indicesOfUnconfirmedPacketsInPipeline.remove(pIndex);
        }
    }


    private static void checkTimeOutOnPipelinedPackets() {
        for( Integer pktIndex : indicesOfUnconfirmedPacketsInPipeline ) {
            if(masterTimer - masterPktList[pktIndex].getTimeSent() >= timeOutAfter) {
                System.out.println("Packet " + masterPktList[pktIndex].getSequenceNum() + " has timed out");
                masterPktList[pktIndex].setNumberOfTimesLost(masterPktList[pktIndex].getNumberOfTimesLost()+1);
                currSeqNumber = masterPktList[pktIndex].getSequenceNum() - 1;

                // remove all packets from pipeline that come after lost packet that are not confirmed
                indicesOfUnconfirmedPacketsInPipeline.subList(indicesOfUnconfirmedPacketsInPipeline.indexOf(
                        masterPktList[currSeqNumber+1].getSequenceNum()), indicesOfUnconfirmedPacketsInPipeline.size()).clear();
                break;
            }
        }

    }

}


