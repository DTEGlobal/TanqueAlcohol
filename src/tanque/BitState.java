package tanque;

/**
 * Created by Cesar on 7/9/13.
 */
public class BitState {

    public boolean getBitState(byte RxByte, int BitNum){
        int mask = 0;
        switch (BitNum) {
            case 0:
                mask = 128;
                return ((RxByte & mask) != 0);
            case 1:
                mask = 64;
                return ((RxByte & mask) != 0);
            case 2:
                mask = 32;
                return ((RxByte & mask) != 0);
            case 3:
                mask = 16;
                return ((RxByte & mask) != 0);
            case 4:
                mask = 8;
                return ((RxByte & mask) != 0);
            case 5:
                mask = 4;
                return ((RxByte & mask) != 0);
            case 6:
                mask = 2;
                return ((RxByte & mask) != 0);
            case 7:
                mask = 1;
                return ((RxByte & mask) != 0);
            default:
                return false;
        }
    }
}