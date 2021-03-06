package edu.duke.ece568.mock;

import edu.duke.ece568.proto.UpsAmazon;
import edu.duke.ece568.utils.SeqNumGenerator;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Scanner;

import static edu.duke.ece568.utils.GPBHelper.recvMsgFrom;
import static edu.duke.ece568.utils.GPBHelper.sendMsgTo;

public class MockAmazon {

    private String HOST;
    private Integer PORT;
    private InputStream in;

    public InputStream getIn() {
        return in;
    }

    public OutputStream getOut() {
        return out;
    }

    private OutputStream out;

    public Socket getSocket() {
        return socket;
    }

    private Socket socket;

    public MockAmazon(int portNum, String host) throws IOException {
        this.PORT = portNum;
        this.HOST = host;
        this.socket = new Socket(this.HOST, this.PORT);
        this.in = this.socket.getInputStream();
        this.out = this.socket.getOutputStream();
    }



    public static void main(String[] args) throws IOException {
        // Connect to UPS
        MockAmazon mockAmazon = new MockAmazon( 11111, "localhost");


        // Recv WorldID and send ack
        UpsAmazon.USendWorldID.Builder recvWorldID = UpsAmazon.USendWorldID.newBuilder();
        recvMsgFrom(recvWorldID, mockAmazon.getIn());

        UpsAmazon.AURequest.Builder ACKResponse = UpsAmazon.AURequest.newBuilder();
        ACKResponse.addAcks(recvWorldID.getSeqnum());
        sendMsgTo(ACKResponse.build(),mockAmazon.getOut());

        // Throw a new thread to receive
        MockAmazonRecvHandler mockAmazonRecvHandler = new MockAmazonRecvHandler(mockAmazon.getSocket());
        new Thread(mockAmazonRecvHandler).start();

        while(true){
            Scanner input = new Scanner(System.in);
            UpsAmazon.AURequest.Builder response = chooseAction(input.nextInt());

            // Send and print the response
            sendMsgTo(response.build(), mockAmazon.getOut());
            System.out.println("MockAmazon Send Message: \n" + response.build());
        }

    }


    // Choose an action and return
    public static UpsAmazon.AURequest.Builder chooseAction(Integer input){
        UpsAmazon.AURequest.Builder response = UpsAmazon.AURequest.newBuilder();
        switch (input){
            // Create AShippingRequest
            //            message Product {
            //                required string description = 1;
            //                required int32 count = 2;
            //            }
            //
            //            message AShipment {
            //                required int64 package_id = 1;
            //                required int64 dest_x = 2;
            //                required int64 dest_y = 3;
            //                required string emailaddress = 4;
            //                repeated Product product = 5;
            //            }
            case 1:{
                UpsAmazon.AShippingRequest.Builder aShippingRequest = UpsAmazon.AShippingRequest.newBuilder();
                // Add AWareHouseLocation
                aShippingRequest.setLocation(UpsAmazon.AWareHouseLocation.newBuilder().setWarehouseid(1).setX(1).setY(1));

                // Gen new product
                UpsAmazon.Product.Builder apples = UpsAmazon.Product.newBuilder().setDescription("Apple").setCount(2);
//                UpsAmazon.Product.Builder bananas = UpsAmazon.Product.newBuilder().setDescription("Banananana").setCount(1);

                // Add Shipment List
                aShippingRequest.addShipment(UpsAmazon.AShipment.newBuilder().setPackageId(1).setDestX(3).setDestY(4).setEmailaddress("xg73@duke.edu").addProduct(apples));
               // aShippingRequest.addShipment(UpsAmazon.AShipment.newBuilder().setPackageId(1).setDestX(1).setDestY(1).setEmailaddress("pad128g@icloud.com").addProduct(apples));

                // Gen seqNum
                aShippingRequest.setSeqnum(SeqNumGenerator.getInstance().getCurrent_id());
                response.addShippingRequest(aShippingRequest);
                break;
            }

            // Create ATruckLoadedNotification
            case 2:{
                UpsAmazon.ATruckLoadedNotification.Builder aTruckLoaded = UpsAmazon.ATruckLoadedNotification.newBuilder();
                aTruckLoaded.setTruckId(1);
                aTruckLoaded.setSeqnum(SeqNumGenerator.getInstance().getCurrent_id());
                response.addLoaded(aTruckLoaded);
                break;
            }

            // Create AShipmentStatusUpdate
            case 3:{
                UpsAmazon.AShipmentStatusUpdate.Builder aShipUpdate = UpsAmazon.AShipmentStatusUpdate.newBuilder();

                // define arr of AUShipmentUpdate
                aShipUpdate.addAuShipmentUpdate(UpsAmazon.AUShipmentUpdate.newBuilder().setPackageId(1).setStatus("Packed"));
                aShipUpdate.addAuShipmentUpdate(UpsAmazon.AUShipmentUpdate.newBuilder().setPackageId(2).setStatus("Packed"));

                aShipUpdate.setSeqnum(SeqNumGenerator.getInstance().getCurrent_id());
                response.addShipmentStatusUpdate(aShipUpdate);
                break;
            }

        }

        return response;
    }


}
