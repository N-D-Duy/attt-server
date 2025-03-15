package org.duynguyen.constants;

public class CMD {
    public static final byte SERVER_DIALOG = -1;
    public static final byte SERVER_MESSAGE = 1;
    public static final byte ALERT_MESSAGE = 0;
    public static final byte GET_SESSION_ID = -2;
    public static final byte FULL_SIZE = 2;


    public static final byte AUTH = -3;
    public static final byte CLIENT_OK = 0;
    public static final byte LOGIN = -1;
    public static final byte REGISTER = 1;
    public static final byte LOGOUT = 2;
    public static final byte LOGIN_OK =-2;
    public static final byte REGISTER_OK =-3;
    public static final byte UPDATE_USER_LIST = 3;

    public static final byte NOT_AUTH = 3;
    public static final byte HANDSHAKE_REQUEST = 10;
    public static final byte HANDSHAKE_ACCEPT = 11;
    public static final byte HANDSHAKE_REJECT = 12;
    public static final byte HANDSHAKE_SUCCESS = 13;

    public static final byte FILE_INFO = 20;
    public static final byte FILE_INFO_RECEIVED = 21;

    public static final byte FILE_CHUNK = 30;
    public static final byte CHUNK_ACK = 31;
    public static final byte CHUNK_ERROR = 32;

    public static final byte FILE_TRANSFER_END = 40;
    public static final byte FILE_TRANSFER_COMPLETE = 41;
    public static final byte FILE_TRANSFER_CANCEL = 42;



}
