package com.codewizards.meshifyanalyzer;

public interface Constants {

    int HELLO_PACKET_INTERVAL_SND = 15000;
    int HELLO_PACKET_INTERVAL = HELLO_PACKET_INTERVAL_SND / 3;
    int NORMAL = 0;
    int WARNING = 1;
    int ERROR = 2;
    String RT_TEST_KEY = "rtt";
    String RT_TEST_REP_KEY = "rtt_rep";
    int RTT_PACKET_INTERVAL_SND = 15000;
    int RTT_PACKET_INTERVAL = HELLO_PACKET_INTERVAL_SND / 3;
}
