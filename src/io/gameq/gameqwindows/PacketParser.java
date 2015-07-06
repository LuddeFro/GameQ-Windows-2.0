package io.gameq.gameqwindows; /**
 * Created by fabianwikstrom on 7/6/2015.
 */

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapBpfProgram;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;
import org.jnetpcap.protocol.tcpip.Tcp;
import org.jnetpcap.protocol.tcpip.Udp;

/**
 * Here is the output generated by this example :
 *
 *  Network devices found:
 *  #0: \Device\NPF_{BC81C4FC-242F-4F1C-9DAD-EA9523CC992D} [Intel(R) PRO/100 VE]
 *  #1: \Device\NPF_{E048DA7F-D007-4EEF-909D-4238F6344971} [VMware Virtual Ethernet Adapter]
 *  #2: \Device\NPF_{5B62B373-3EC1-460D-8C71-54AA0BF761C7} [VMware Virtual Ethernet Adapter]
 *  #3: \Device\NPF_GenericDialupAdapter [Adapter for generic dialup and VPN capture]
 */

public class PacketParser {

    private static PacketParser instance = null;
    protected PacketParser() {
        // Exists only to defeat instantiation.
    }
    public static PacketParser getInstance() {
        if(instance == null) {
            instance = new PacketParser();
        }
        return instance;
    }

    private Pcap pcap = null;

    public void start(String filterString, final PacketDetector detector) {
        List<PcapIf> alldevs = new ArrayList<PcapIf>(); // Will be filled with NICs
        StringBuilder errbuf = new StringBuilder(); // For any error msgs

        /***************************************************************************
         * First get a list of devices on this system
         **************************************************************************/
        int r = Pcap.findAllDevs(alldevs, errbuf);
        if (r == Pcap.NOT_OK || alldevs.isEmpty()) {
            System.err.printf("Can't read list of devices, error is %s", errbuf
                    .toString());
            return;
        }

        System.out.println("Network devices found:");

        int i = 0;
        for (PcapIf device : alldevs) {
            String description =
                    (device.getDescription() != null) ? device.getDescription()
                            : "No description available";
            System.out.printf("#%d: %s [%s]\n", i++, device.getName(), description);
        }

        PcapIf device = alldevs.get(0); // We know we have atleast 1 device
        System.out
                .printf("\nChoosing '%s' on your behalf:\n",
                        (device.getDescription() != null) ? device.getDescription()
                                : device.getName());

        /***************************************************************************
         * Second we open up the selected device
         **************************************************************************/
        int snaplen = 64 * 1024;           // Capture all packets, no trucation
        int flags = Pcap.MODE_PROMISCUOUS; // capture all packets
        int timeout = 100;           // 10 seconds in millis
        pcap = Pcap.openLive(device.getName(), snaplen, flags, timeout, errbuf);

        if (pcap == null) {
            System.err.printf("Error while opening device for capture: "
                    + errbuf.toString());
            return;
        }

        /*Set Filter*/

        PcapBpfProgram filter = new PcapBpfProgram();
        int optimize = 0; // 1 means true, 0 means false
        int netmask = 0;

        int k = pcap.compile(filter, filterString, optimize, netmask);
        if (k != Pcap.OK) {
            System.out.println("Filter error: " + pcap.getErr());
        }
        pcap.setFilter(filter);


        /***************************************************************************
         * Third we create a packet handler which will receive packets from the
         * libpcap loop.
         **************************************************************************/
        PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {

            Tcp tcp = new Tcp(); // Preallocate a Tcp header
            Udp udp = new Udp(); // Preallocate a UDP header

            public void nextPacket(PcapPacket packet, String user) {

                if (packet.hasHeader(tcp)) {
                    System.out.print("Found tcp packet");
                    System.out.print("src: " +  tcp.source());
                    System.out.print("dst: " +  tcp.destination());
                    System.out.print("len: " + tcp.getLength()); //PROBABLY WRONG
                    System.out.println("time: " + new Date(packet.getCaptureHeader().timestampInMillis()));
                }

                else if(packet.hasHeader(udp)){
                    System.out.print("Found udp packet");
                    System.out.print(" src: " + udp.source());
                    System.out.print(" dst: " + udp.destination());
                    System.out.print(" caplen : " + packet.getCaptureHeader().caplen());
                    System.out.println(" time: " + new Date(packet.getCaptureHeader().timestampInMillis()));
                    detector.handle(new Packet(udp.source(), udp.destination(), packet.getCaptureHeader().caplen(), packet.getCaptureHeader().timestampInMillis()/1000));
                }
            }
        };

        /***************************************************************************
         * Fourth we enter the loop and tell it to capture 10 packets. The loop
         * method does a mapping of pcap.datalink() DLT value to JProtocol ID, which
         * is needed by JScanner. The scanner scans the packet buffer and decodes
         * the headers. The mapping is done automatically, although a variation on
         * the loop method exists that allows the programmer to sepecify exactly
         * which protocol ID to use as the data link type for this pcap interface.
         **************************************************************************/
        pcap.loop(Pcap.LOOP_INFINITE, jpacketHandler, "jNetPcap rocks!");

        /***************************************************************************
         * Last thing to do is close the pcap handle
         **************************************************************************/
    }

    public void terminate(){
        if (pcap != null) {
            pcap.close();
        }
    }
}