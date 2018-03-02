package com.pens.afdolash.bytan_direct.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * Created by afdol on 2/26/2018.
 */

public class WifiDirectUtils {

    private final static String p2pInt = "p2p-p2p0";

    /*
     * Method for get IP Address from MAC Address
     */
    public static String getIPFromMac(String MAC) {
        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new FileReader("/proc/net/arp"));
            String line;

            while ((line = bufferedReader.readLine()) != null) {
                String[] splitted = line.split(" +");

                if (splitted != null && splitted.length >= 4) {
                    // Basic sanity check
                    String device = splitted[5];

                    if (device.matches(".*" +p2pInt+ ".*")) {
                        String mac = splitted[3];

                        if (mac.matches(MAC)) {
                            return splitted[0];
                        }
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            try {
                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /*
     * Method for get local IP Address
     */
    public static String getLocalIPAddress() {
        try {
            for (Enumeration<NetworkInterface> interfaceEnumeration = NetworkInterface.getNetworkInterfaces(); interfaceEnumeration.hasMoreElements();) {
                NetworkInterface networkInterface = interfaceEnumeration.nextElement();

                for (Enumeration<InetAddress> addressEnumeration = networkInterface.getInetAddresses(); addressEnumeration.hasMoreElements();) {
                    InetAddress inetAddress = addressEnumeration.nextElement();

                    String iface = networkInterface.getName();
                    if(iface.matches(".*" +p2pInt+ ".*")){
                        if (inetAddress instanceof Inet4Address) { // fix for Galaxy Nexus. IPv4 is easy to use :-)
                            return getDottedDecimalIP(inetAddress.getAddress());
                        }
                    }
                }
            }
        }
        catch (SocketException e) {
            Log.e("NetworkAddressFactory", "getLocalIPAddress()", e);
        }
        catch (NullPointerException e) {
            Log.e("NetworkAddressFactory", "getLocalIPAddress()", e);
        }
        return null;
    }

    /*
     * Method for get each device IP Address in wifi direct
     */
    private static String getDottedDecimalIP(byte[] ipAddr) {
        String ipAddrStr = "";

        for (int i = 0; i < ipAddr.length; i++) {
            if (i > 0) {
                ipAddrStr += ".";
            }
            ipAddrStr += ipAddr[i] & 0xFF;
        }
        return ipAddrStr;
    }
}
